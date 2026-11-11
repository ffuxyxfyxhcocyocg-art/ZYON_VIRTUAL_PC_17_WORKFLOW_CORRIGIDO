#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include "include/zyon/Types.hpp"
#include <string>
#include <vector>
#include <filesystem>
#include <fstream>
#include <chrono>

namespace fs = std::filesystem;
namespace zyon {

struct SnapshotInfo {
    std::string id, name, path, note;
    u64 created_ms = 0;
};

class SnapshotManager {
public:
    static SnapshotManager& instance() { static SnapshotManager s; return s; }

    Error create(const std::string& vm_work_dir, const std::string& name, const std::string& note = "") {
        std::string snap_dir = vm_work_dir + "/snapshots";
        fs::create_directories(snap_dir);
        auto id = "snap_" + std::to_string(now_ms());
        std::string dir = snap_dir + "/" + id;
        fs::create_directories(dir);
        std::string disks = vm_work_dir + "/disks";
        if (fs::exists(disks)) {
            for (auto& e : fs::directory_iterator(disks)) {
                if (!e.is_regular_file()) continue;
                std::error_code ec;
                fs::copy_file(e.path(), dir + "/" + e.path().filename().string(),
                              fs::copy_options::overwrite_existing, ec);
            }
        }
        std::ofstream meta(dir + "/meta.txt");
        meta << "id=" << id << "\nname=" << name << "\nnote=" << note
             << "\ncreated_ms=" << now_ms() << "\n";
        ZLOGI("Snapshot %s created", id.c_str());
        return Error::Ok();
    }

    Error restore(const std::string& vm_work_dir, const std::string& snap_id) {
        std::string src = vm_work_dir + "/snapshots/" + snap_id;
        if (!fs::exists(src)) return Error::Make(ErrorCode::NotFound, "snapshot not found");
        std::string disks = vm_work_dir + "/disks";
        fs::create_directories(disks);
        for (auto& e : fs::directory_iterator(src)) {
            if (!e.is_regular_file() || e.path().filename() == "meta.txt") continue;
            std::error_code ec;
            fs::copy_file(e.path(), disks + "/" + e.path().filename().string(),
                          fs::copy_options::overwrite_existing, ec);
            if (ec) return Error::Make(ErrorCode::IoError, ec.message());
        }
        return Error::Ok();
    }

    Error remove(const std::string& vm_work_dir, const std::string& snap_id) {
        std::error_code ec;
        fs::remove_all(vm_work_dir + "/snapshots/" + snap_id, ec);
        if (ec) return Error::Make(ErrorCode::IoError, ec.message());
        return Error::Ok();
    }

    std::vector<SnapshotInfo> list(const std::string& vm_work_dir) {
        std::vector<SnapshotInfo> out;
        std::string snap_dir = vm_work_dir + "/snapshots";
        if (!fs::exists(snap_dir)) return out;
        for (auto& e : fs::directory_iterator(snap_dir)) {
            if (!e.is_directory()) continue;
            SnapshotInfo info;
            info.id = e.path().filename().string();
            info.path = e.path().string();
            std::ifstream meta(e.path() / "meta.txt");
            std::string line;
            while (std::getline(meta, line)) {
                auto eq = line.find('=');
                if (eq == std::string::npos) continue;
                auto k = line.substr(0, eq), v = line.substr(eq + 1);
                if (k == "name") info.name = v;
                else if (k == "note") info.note = v;
                else if (k == "created_ms") info.created_ms = std::stoull(v);
            }
            out.push_back(info);
        }
        return out;
    }

private:
    static u64 now_ms() {
        using namespace std::chrono;
        return duration_cast<milliseconds>(system_clock::now().time_since_epoch()).count();
    }
};

} // namespace zyon
