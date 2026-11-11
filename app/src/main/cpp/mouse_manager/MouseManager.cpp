#include "include/zyon/Log.hpp"
#include <vector>
#include <string>

namespace zyon {

class MouseManager {
public:
    static void append_qemu_args(std::vector<std::string>& a, bool absolute) {
        a.push_back("-device"); a.push_back("qemu-xhci,id=xhci");
        if (absolute) {
            a.push_back("-device"); a.push_back("usb-tablet,bus=xhci.0");
        } else {
            a.push_back("-device"); a.push_back("usb-mouse,bus=xhci.0");
        }
    }
};

} // namespace zyon
