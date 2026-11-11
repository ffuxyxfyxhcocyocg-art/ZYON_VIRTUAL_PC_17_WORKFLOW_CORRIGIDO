#include "include/zyon/Log.hpp"
#include "include/zyon/VmConfig.hpp"
#include <vector>
#include <string>
#include <sys/stat.h>

namespace zyon {

class FirmwareManager {
public:
    static bool file_ok(const std::string& p) {
        struct stat st{}; return !p.empty() && stat(p.c_str(), &st) == 0;
    }

    static void append_qemu_args(std::vector<std::string>& a, const FirmwareConfig& fw) {
        if (fw.type == FirmwareType::OVMF || fw.type == FirmwareType::OVMF_SECURE) {
            if (file_ok(fw.ovmf_code)) {
                a.push_back("-drive");
                a.push_back("if=pflash,format=raw,readonly=on,file=" + fw.ovmf_code);
            }
            if (file_ok(fw.ovmf_vars)) {
                a.push_back("-drive");
                a.push_back("if=pflash,format=raw,file=" + fw.ovmf_vars);
            }
        } else if (fw.type == FirmwareType::SeaBIOS || fw.type == FirmwareType::QEMU_EFI) {
            if (file_ok(fw.bios_path)) {
                a.push_back("-bios"); a.push_back(fw.bios_path);
            }
        }
    }

    static FirmwareConfig default_for_arch(Arch arch, const std::string& assets_root) {
        FirmwareConfig fw;
        if (arch == Arch::X86_64 || arch == Arch::X86) {
            fw.type = FirmwareType::SeaBIOS;
            fw.bios_path = assets_root + "/bios/bios-256k.bin";
            // Prefer OVMF if present
            std::string code = assets_root + "/firmware/OVMF_CODE.fd";
            std::string vars = assets_root + "/firmware/OVMF_VARS.fd";
            if (file_ok(code) && file_ok(vars)) {
                fw.type = FirmwareType::OVMF;
                fw.ovmf_code = code;
                fw.ovmf_vars = vars;
            }
        } else {
            fw.type = FirmwareType::QEMU_EFI;
            fw.bios_path = assets_root + "/firmware/QEMU_EFI.fd";
        }
        return fw;
    }
};

} // namespace zyon
