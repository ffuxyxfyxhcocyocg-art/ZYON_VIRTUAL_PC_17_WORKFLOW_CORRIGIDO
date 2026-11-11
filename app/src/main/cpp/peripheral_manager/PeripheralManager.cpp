#include "include/zyon/Log.hpp"
#include <vector>
#include <string>

namespace zyon {

class PeripheralManager {
public:
    // Serial, parallel, TPM stubs as QEMU devices
    static void append_serial(std::vector<std::string>& a) {
        a.push_back("-device"); a.push_back("virtio-serial-pci");
    }
    static void append_tpm(std::vector<std::string>& a, bool enabled) {
        if (!enabled) return;
        // Needs swtpm on host — flag only
        a.push_back("-tpmdev"); a.push_back("emulator,id=tpm0,chardev=chrtpm");
        a.push_back("-chardev"); a.push_back("socket,id=chrtpm,path=/tmp/swtpm-sock");
        a.push_back("-device"); a.push_back("tpm-tis,tpmdev=tpm0");
    }
    static void append_cdrom(std::vector<std::string>& a, const std::string& iso) {
        if (iso.empty()) return;
        a.push_back("-cdrom"); a.push_back(iso);
    }
};

} // namespace zyon
