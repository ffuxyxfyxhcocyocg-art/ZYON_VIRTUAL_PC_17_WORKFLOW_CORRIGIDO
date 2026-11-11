#include "include/zyon/Log.hpp"
#include <vector>
#include <string>
#include <map>

namespace zyon {

class KeyboardManager {
public:
    // Android keycode -> QEMU qcode / XT
    static int android_to_qcode(int android_keycode) {
        static const std::map<int,int> m = {
            {67, 0x0e}, // DEL/BACKSPACE
            {66, 0x1c}, // ENTER
            {62, 0x39}, // SPACE
            {59, 0x2a}, // SHIFT_LEFT
            {60, 0x36}, // SHIFT_RIGHT
            {113, 0x1d}, // CTRL_LEFT
            {114, 0x9d},
            {57, 0x38}, // ALT_LEFT
            {58, 0xb8},
            {111, 0x01}, // ESC
            {61, 0x0f}, // TAB
        };
        auto it = m.find(android_keycode);
        if (it != m.end()) return it->second;
        // A-Z roughly
        if (android_keycode >= 29 && android_keycode <= 54)
            return 0x1e + (android_keycode - 29); // rough
        return -1;
    }

    static void append_qemu_args(std::vector<std::string>& a) {
        a.push_back("-device"); a.push_back("usb-kbd");
    }
};

} // namespace zyon
