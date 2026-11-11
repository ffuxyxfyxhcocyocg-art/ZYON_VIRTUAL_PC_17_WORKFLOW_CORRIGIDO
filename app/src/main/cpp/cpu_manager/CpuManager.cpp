#include "include/zyon/Log.hpp"
#include "include/zyon/VmConfig.hpp"
#include <vector>
#include <string>

namespace zyon {

class CpuManager {
public:
    static void append_qemu_args(std::vector<std::string>& a, const CpuConfig& cfg) {
        a.push_back("-cpu");
        a.push_back(cfg.model.empty() ? "qemu64" : cfg.model);
        u32 smp = cfg.cores * cfg.threads * cfg.sockets;
        if (smp < 1) smp = 1;
        a.push_back("-smp");
        a.push_back(std::to_string(smp) + ",sockets=" + std::to_string(cfg.sockets) +
                    ",cores=" + std::to_string(cfg.cores) +
                    ",threads=" + std::to_string(cfg.threads));
        if (cfg.tcg_mttcg) {
            // MTTCG is default on modern QEMU when multi-core
        }
    }

    static std::string default_model(Arch arch) {
        switch (arch) {
            case Arch::X86_64: return "qemu64";
            case Arch::X86: return "qemu32";
            case Arch::ARM64: return "cortex-a57";
            case Arch::ARM: return "cortex-a15";
            default: return "max";
        }
    }
};

} // namespace zyon
