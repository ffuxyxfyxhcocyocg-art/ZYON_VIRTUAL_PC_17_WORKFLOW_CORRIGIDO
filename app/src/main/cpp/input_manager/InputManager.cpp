#include "include/zyon/Log.hpp"
#include <android/input.h>
#include <mutex>
#include <vector>
#include <functional>

namespace zyon {

struct InputEvent {
    enum Type { KEY, MOTION } type;
    int code = 0;
    int action = 0;
    float x = 0, y = 0;
};

class InputManager {
public:
    static InputManager& instance() { static InputManager i; return i; }

    void on_key(int keycode, int action) {
        std::lock_guard lock(mu_);
        InputEvent e{InputEvent::KEY, keycode, action, 0, 0};
        queue_.push_back(e);
        if (sink_) sink_(e);
    }

    void on_motion(float x, float y, int action) {
        std::lock_guard lock(mu_);
        InputEvent e{InputEvent::MOTION, 0, action, x, y};
        queue_.push_back(e);
        if (sink_) sink_(e);
    }

    void set_sink(std::function<void(const InputEvent&)> s) { sink_ = std::move(s); }

    std::vector<InputEvent> drain() {
        std::lock_guard lock(mu_);
        auto out = queue_;
        queue_.clear();
        return out;
    }

private:
    std::mutex mu_;
    std::vector<InputEvent> queue_;
    std::function<void(const InputEvent&)> sink_;
};

} // namespace zyon
