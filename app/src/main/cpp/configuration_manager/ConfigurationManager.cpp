#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include "include/zyon/VmConfig.hpp"
#include <fstream>
#include <sstream>
#include <map>
#include <string>

namespace zyon {

class ConfigurationManager {
public:
    static Error save_vm(const VmConfig& cfg) {
        std::ofstream f(cfg.work_dir + "/config.zyon");
        if (!f) return Error::Make(ErrorCode::IoError, "cannot write config");
        f << "name=" << cfg.name << "\n";
        f << "uuid=" << cfg.uuid << "\n";
        f << "arch=" << (int)cfg.cpu.arch << "\n";
        f << "cores=" << cfg.cpu.cores << "\n";
        f << "threads=" << cfg.cpu.threads << "\n";
        f << "sockets=" << cfg.cpu.sockets << "\n";
        f << "cpu_model=" << cfg.cpu.model << "\n";
        f << "ram_bytes=" << cfg.memory.ram_bytes << "\n";
        f << "machine=" << (int)cfg.machine << "\n";
        f << "gpu=" << (int)cfg.display.gpu << "\n";
        f << "audio=" << (int)cfg.audio.model << "\n";
        f << "firmware=" << (int)cfg.firmware.type << "\n";
        int i = 0;
        for (auto& d : cfg.disks) {
            f << "disk" << i << "_path=" << d.path << "\n";
            f << "disk" << i << "_fmt=" << (int)d.format << "\n";
            f << "disk" << i << "_size=" << d.size_bytes << "\n";
            ++i;
        }
        i = 0;
        for (auto& c : cfg.cdroms) {
            f << "cdrom" << i << "=" << c.path << "\n";
            ++i;
        }
        return Error::Ok();
    }

    static Result<VmConfig> load_vm(const std::string& work_dir) {
        std::ifstream f(work_dir + "/config.zyon");
        if (!f) return Result<VmConfig>::Fail(ErrorCode::NotFound, "no config");
        VmConfig cfg;
        cfg.work_dir = work_dir;
        std::string line;
        std::map<int, DiskConfig> disks;
        while (std::getline(f, line)) {
            auto eq = line.find('=');
            if (eq == std::string::npos) continue;
            auto k = line.substr(0, eq);
            auto v = line.substr(eq + 1);
            if (k == "name") cfg.name = v;
            else if (k == "uuid") cfg.uuid = v;
            else if (k == "arch") cfg.cpu.arch = (Arch)std::stoi(v);
            else if (k == "cores") cfg.cpu.cores = std::stoi(v);
            else if (k == "threads") cfg.cpu.threads = std::stoi(v);
            else if (k == "sockets") cfg.cpu.sockets = std::stoi(v);
            else if (k == "cpu_model") cfg.cpu.model = v;
            else if (k == "ram_bytes") cfg.memory.ram_bytes = std::stoull(v);
            else if (k == "machine") cfg.machine = (MachineType)std::stoi(v);
            else if (k == "gpu") cfg.display.gpu = (GpuModel)std::stoi(v);
            else if (k == "audio") cfg.audio.model = (AudioModel)std::stoi(v);
            else if (k == "firmware") cfg.firmware.type = (FirmwareType)std::stoi(v);
            else if (k.rfind("disk", 0) == 0 && k.find("_path") != std::string::npos) {
                int idx = std::stoi(k.substr(4));
                disks[idx].path = v;
            } else if (k.rfind("cdrom", 0) == 0) {
                CdromConfig c; c.path = v; cfg.cdroms.push_back(c);
            }
        }
        for (auto& [_, d] : disks) cfg.disks.push_back(d);
        return Result<VmConfig>::Ok(cfg);
    }
};

} // namespace zyon
