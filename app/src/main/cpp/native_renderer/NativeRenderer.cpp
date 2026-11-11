#include "include/zyon/Log.hpp"
#include <android/native_window.h>
#include <mutex>
#include <cstdint>

namespace zyon {

class NativeRenderer {
public:
    static NativeRenderer& instance() { static NativeRenderer r; return r; }

    void set_window(ANativeWindow* w) {
        std::lock_guard lock(mu_);
        if (window_) ANativeWindow_release(window_);
        window_ = w;
        if (window_) {
            ANativeWindow_acquire(window_);
            ANativeWindow_setBuffersGeometry(window_, 0, 0, WINDOW_FORMAT_RGBA_8888);
        }
    }

    // Fill with solid color (placeholder until VNC/framebuffer path)
    void clear(uint8_t r, uint8_t g, uint8_t b) {
        std::lock_guard lock(mu_);
        if (!window_) return;
        ANativeWindow_Buffer buf;
        if (ANativeWindow_lock(window_, &buf, nullptr) != 0) return;
        auto* bits = static_cast<uint32_t*>(buf.bits);
        uint32_t color = (0xffu << 24) | (r << 16) | (g << 8) | b;
        for (int y = 0; y < buf.height; ++y) {
            for (int x = 0; x < buf.stride; ++x)
                bits[y * buf.stride + x] = color;
        }
        ANativeWindow_unlockAndPost(window_);
    }

private:
    std::mutex mu_;
    ANativeWindow* window_ = nullptr;
};

} // namespace zyon
