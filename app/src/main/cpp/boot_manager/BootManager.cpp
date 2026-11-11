#include "include/zyon/VmConfig.hpp"
#include <vector>
#include <string>

namespace zyon {

class BootManager {
public:
    static void append_qemu_args(std::vector<std::string>& a, const std::vector<BootDevice>& order) {
        std::string boot = "order=";
        for (auto b : order) {
            switch (b) {
                case BootDevice::HDD: boot += "c"; break;
                case BootDevice::CDROM: boot += "d"; break;
                case BootDevice::NETWORK: boot += "n"; break;
                case BootDevice::FLOPPY: boot += "a"; break;
                default: break;
            }
        }
        if (boot.size() > 6) { a.push_back("-boot"); a.push_back(boot); }
    }
};

} // namespace zyon
