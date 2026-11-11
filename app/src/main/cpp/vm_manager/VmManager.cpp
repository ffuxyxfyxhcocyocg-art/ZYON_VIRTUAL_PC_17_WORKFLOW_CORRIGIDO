#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include "include/zyon/VmConfig.hpp"
#include <map>
#include <mutex>
#include <memory>
#include <filesystem>
#include <fstream>
#include <sstream>
#include <random>
#include <iomanip>

namespace fs = std::filesystem;
namespace zyon {

// Forward declarations of backend pieces
class QemuBackend;
class QmpClient;

struct VmRuntime {
    VmConfig config;
    VmState state = VmState::CREATED;
    // backend owned elsewhere
    // qmp owned elsewhere
    std::string work_dir;
    u64 start_time_ms = 0;
};

class VmManager {
public:
    static VmManager& instance() {
        static VmManager inst;
        return inst;
    }

    Error init(const std::string& data_root) {
        std::lock_guard lock(mu_);
        data_root_ = data_root;
        fs::create_directories(data_root_ + "/vms");
        fs::create_directories(data_root_ + "/isos");
        fs::create_directories(data_root_ + "/firmware");
        fs::create_directories(data_root_ + "/backups");
        load_all();
        ZLOGI("VmManager initialized at %s", data_root_.c_str());
        return Error::Ok();
    }

    Result<std::string> create(const VmConfig& cfg_in) {
        std::lock_guard lock(mu_);
        VmConfig cfg = cfg_in;
        if (cfg.name.empty()) {
            return Result<std::string>::Fail(ErrorCode::InvalidArgument, "VM name required");
        }
        if (vms_.count(cfg.name)) {
            return Result<std::string>::Fail(ErrorCode::AlreadyExists, "VM already exists");
        }

        if (cfg.uuid.empty()) cfg.uuid = generate_uuid();
        cfg.work_dir = data_root_ + "/vms/" + cfg.name;
        fs::create_directories(cfg.work_dir);
        fs::create_directories(cfg.work_dir + "/snapshots");
        fs::create_directories(cfg.work_dir + "/disks");

        // Create empty disk if path empty
        for (auto& d : cfg.disks) {
            if (d.path.empty()) {
                d.path = cfg.work_dir + "/disks/" + (d.id.empty() ? "disk0" : d.id) + ".qcow2";
                d.format = DiskFormat::QCOW2;
                // Actual qemu-img create is deferred to disk_manager
            }
        }

        auto rt = std::make_shared<VmRuntime>();
        rt->config = cfg;
        rt->state = VmState::CREATED;
        rt->work_dir = cfg.work_dir;
        vms_[cfg.name] = rt;
        save_config(cfg);
        ZLOGI("Created VM %s uuid=%s", cfg.name.c_str(), cfg.uuid.c_str());
        return Result<std::string>::Ok(cfg.name);
    }

    Error remove(const std::string& name, bool delete_files) {
        std::lock_guard lock(mu_);
        auto it = vms_.find(name);
        if (it == vms_.end()) return Error::Make(ErrorCode::NotFound, "VM not found");
        if (it->second->state == VmState::RUNNING ||
            it->second->state == VmState::PAUSED) {
            return Error::Make(ErrorCode::InvalidArgument, "Stop VM before delete");
        }
        if (delete_files) {
            std::error_code ec;
            fs::remove_all(it->second->work_dir, ec);
        }
        vms_.erase(it);
        ZLOGI("Removed VM %s", name.c_str());
        return Error::Ok();
    }

    Error start(const std::string& name) {
        std::shared_ptr<VmRuntime> rt;
        {
            std::lock_guard lock(mu_);
            auto it = vms_.find(name);
            if (it == vms_.end()) return Error::Make(ErrorCode::NotFound, "VM not found");
            rt = it->second;
        }
        if (rt->state == VmState::RUNNING) return Error::Ok();

        rt->state = VmState::STARTING;
        // Backend start is handled by QemuBackend (linked separately)
        // Here we only update state machine; real launch is in native bridge
        rt->state = VmState::RUNNING;
        rt->start_time_ms = now_ms();
        ZLOGI("VM %s marked RUNNING", name.c_str());
        return Error::Ok();
    }

    Error stop(const std::string& name, bool force) {
        std::shared_ptr<VmRuntime> rt;
        {
            std::lock_guard lock(mu_);
            auto it = vms_.find(name);
            if (it == vms_.end()) return Error::Make(ErrorCode::NotFound, "VM not found");
            rt = it->second;
        }
        rt->state = force ? VmState::STOPPED : VmState::STOPPING;
        // Real process kill happens in QemuBackend
        rt->state = VmState::STOPPED;
        ZLOGI("VM %s stopped (force=%d)", name.c_str(), (int)force);
        return Error::Ok();
    }

    Error pause(const std::string& name) {
        auto rt = get(name);
        if (!rt) return Error::Make(ErrorCode::NotFound, "VM not found");
        if (rt->state != VmState::RUNNING) return Error::Make(ErrorCode::InvalidArgument, "Not running");
        rt->state = VmState::PAUSED;
        return Error::Ok();
    }

    Error resume(const std::string& name) {
        auto rt = get(name);
        if (!rt) return Error::Make(ErrorCode::NotFound, "VM not found");
        if (rt->state != VmState::PAUSED) return Error::Make(ErrorCode::InvalidArgument, "Not paused");
        rt->state = VmState::RUNNING;
        return Error::Ok();
    }

    std::vector<std::string> list() const {
        std::lock_guard lock(mu_);
        std::vector<std::string> names;
        for (const auto& [n, _] : vms_) names.push_back(n);
        return names;
    }

    std::shared_ptr<VmRuntime> get(const std::string& name) {
        std::lock_guard lock(mu_);
        auto it = vms_.find(name);
        return it == vms_.end() ? nullptr : it->second;
    }

    VmConfig get_config(const std::string& name) {
        auto rt = get(name);
        return rt ? rt->config : VmConfig{};
    }

    VmState get_state(const std::string& name) {
        auto rt = get(name);
        return rt ? rt->state : VmState::ERROR;
    }

private:
    VmManager() = default;

    void load_all() {
        // Minimal: scan work dirs for config.json later
    }

    void save_config(const VmConfig& cfg) {
        // Write minimal JSON-like config for persistence
        std::ofstream f(cfg.work_dir + "/config.zyon");
        if (!f) return;
        f << "name=" << cfg.name << "\n";
        f << "uuid=" << cfg.uuid << "\n";
        f << "arch=" << static_cast<int>(cfg.cpu.arch) << "\n";
        f << "cores=" << cfg.cpu.cores << "\n";
        f << "ram_mb=" << (cfg.memory.ram_bytes / (1024*1024)) << "\n";
        f << "machine=" << static_cast<int>(cfg.machine) << "\n";
    }

    static std::string generate_uuid() {
        static std::mt19937_64 rng{std::random_device{}()};
        std::uniform_int_distribution<u64> dist;
        u64 a = dist(rng), b = dist(rng);
        std::ostringstream oss;
        oss << std::hex << std::setfill('0')
            << std::setw(8) << (a >> 32) << "-"
            << std::setw(4) << ((a >> 16) & 0xffff) << "-"
            << std::setw(4) << (a & 0xffff) << "-"
            << std::setw(4) << (b >> 48) << "-"
            << std::setw(12) << (b & 0xffffffffffffULL);
        return oss.str();
    }

    static u64 now_ms() {
        using namespace std::chrono;
        return duration_cast<milliseconds>(steady_clock::now().time_since_epoch()).count();
    }

    mutable std::mutex mu_;
    std::string data_root_;
    std::map<std::string, std::shared_ptr<VmRuntime>> vms_;
};

} // namespace zyon
