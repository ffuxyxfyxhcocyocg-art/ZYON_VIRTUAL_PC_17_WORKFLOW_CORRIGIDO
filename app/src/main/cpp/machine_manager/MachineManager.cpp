#include "include/zyon/Log.hpp"
#include "include/zyon/VmConfig.hpp"
#include <vector>
#include <string>

namespace zyon {

class MachineManager {
public:
    static void append_qemu_args(std::vector<std::string>& a, MachineType mt, Arch arch) {
        switch (mt) {
            case MachineType::PC_Q35:
                a.push_back("-machine"); a.push_back("q35,accel=tcg,usb=on");
                break;
            case MachineType::PC_I440FX:
                a.push_back("-machine"); a.push_back("pc,accel=tcg,usb=on");
                break;
            case MachineType::VIRT_AARCH64:
                a.push_back("-machine"); a.push_back("virt,accel=tcg,gic-version=3,usb=on");
                break;
            case MachineType::VIRT_ARM:
                a.push_back("-machine"); a.push_back("virt,accel=tcg,usb=on");
                break;
            default:
                if (arch == Arch::ARM64 || arch == Arch::ARM) {
                    a.push_back("-machine"); a.push_back("virt,accel=tcg,gic-version=3");
                } else {
                    a.push_back("-machine"); a.push_back("q35,accel=tcg");
                }
                break;
        }
    }

    static MachineType default_for(Arch arch) {
        switch (arch) {
            case Arch::ARM64: return MachineType::VIRT_AARCH64;
            case Arch::ARM: return MachineType::VIRT_ARM;
            default: return MachineType::PC_Q35;
        }
    }
};

} // namespace zyon
