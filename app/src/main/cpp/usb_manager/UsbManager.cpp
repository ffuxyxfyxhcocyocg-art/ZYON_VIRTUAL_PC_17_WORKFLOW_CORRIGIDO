#include "include/zyon/Log.hpp"
#include "include/zyon/VmConfig.hpp"
#include <vector>
#include <string>

namespace zyon {

class UsbManager {
public:
    static void append_qemu_args(std::vector<std::string>& a, const UsbConfig& cfg) {
        if (!cfg.enabled) return;
        if (cfg.version >= 3) {
            a.push_back("-device"); a.push_back("qemu-xhci,id=xhci");
        } else if (cfg.version == 2) {
            a.push_back("-device"); a.push_back("usb-ehci,id=ehci");
        } else {
            a.push_back("-usb");
        }
        if (cfg.tablet) {
            a.push_back("-device"); a.push_back("usb-tablet");
        }
    }
};

} // namespace zyon
