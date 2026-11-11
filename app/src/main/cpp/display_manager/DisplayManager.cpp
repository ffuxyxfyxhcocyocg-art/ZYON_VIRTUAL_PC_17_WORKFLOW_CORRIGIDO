#include "include/zyon/Log.hpp"
#include "include/zyon/VmConfig.hpp"
#include <android/native_window.h>
#include <vector>
#include <string>
#include <mutex>

namespace zyon {

class DisplayManager {
public:
    static DisplayManager& instance() { static DisplayManager d; return d; }

    void set_window(ANativeWindow* win) {
        std::lock_guard lock(mu_);
        if (window_) ANativeWindow_release(window_);
        window_ = win;
        if (window_) ANativeWindow_acquire(window_);
        ZLOGI("DisplayManager window=%p", window_);
    }

    ANativeWindow* window() const { return window_; }

    static void append_qemu_args(std::vector<std::string>& a, const DisplayConfig& cfg) {
        switch (cfg.gpu) {
            case GpuModel::VIRTIO_GPU:
                a.push_back("-device"); a.push_back("virtio-gpu-pci");
                break;
            case GpuModel::QXL:
                a.push_back("-vga"); a.push_back("qxl");
                break;
            case GpuModel::CIRRUS:
                a.push_back("-vga"); a.push_back("cirrus");
                break;
            default:
                a.push_back("-vga"); a.push_back("std");
                break;
        }
        // Android: headless + VNC for external viewer, or custom renderer
        a.push_back("-display"); a.push_back("none");
        a.push_back("-vnc"); a.push_back("127.0.0.1:1,to=10");
    }

private:
    std::mutex mu_;
    ANativeWindow* window_ = nullptr;
};

} // namespace zyon
