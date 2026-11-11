#include "include/zyon/Log.hpp"
#include <string>
#include <mutex>

namespace zyon {

class ClipboardManager {
public:
    static ClipboardManager& instance() { static ClipboardManager c; return c; }

    void set_host_text(const std::string& t) {
        std::lock_guard lock(mu_);
        host_text_ = t;
        ZLOGD("Clipboard host text len=%zu", t.size());
    }
    std::string get_host_text() {
        std::lock_guard lock(mu_);
        return host_text_;
    }
    // Guest integration requires spice/vdagent or QEMU guest agent — stub for host side
private:
    std::mutex mu_;
    std::string host_text_;
};

} // namespace zyon
