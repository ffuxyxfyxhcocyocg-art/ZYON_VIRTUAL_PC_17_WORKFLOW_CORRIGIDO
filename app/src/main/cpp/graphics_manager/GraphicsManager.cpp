#include "include/zyon/Log.hpp"
#include <android/native_window.h>
#include "include/zyon/VmConfig.hpp"
#include <vector>
#include <string>
#include <EGL/egl.h>
#include <GLES3/gl3.h>

namespace zyon {

class GraphicsManager {
public:
    static GraphicsManager& instance() { static GraphicsManager g; return g; }

    bool init_egl(ANativeWindow* /*window*/) {
        // EGL init for host-side overlay / future virtio-gpu render path
        display_ = eglGetDisplay(EGL_DEFAULT_DISPLAY);
        if (display_ == EGL_NO_DISPLAY) return false;
        EGLint maj, min;
        if (!eglInitialize(display_, &maj, &min)) return false;
        ZLOGI("EGL %d.%d", maj, min);
        return true;
    }

    void shutdown() {
        if (display_ != EGL_NO_DISPLAY) {
            eglTerminate(display_);
            display_ = EGL_NO_DISPLAY;
        }
    }

    static void append_qemu_args(std::vector<std::string>& a, const DisplayConfig& cfg) {
        switch (cfg.gpu) {
            case GpuModel::VIRTIO_GPU:
                a.push_back("-device");
                a.push_back(cfg.gl ? "virtio-gpu-gl-pci" : "virtio-gpu-pci");
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
        a.push_back("-display"); a.push_back("none");
        a.push_back("-vnc"); a.push_back("127.0.0.1:1,to=10");
    }

private:
    EGLDisplay display_ = EGL_NO_DISPLAY;
};

} // namespace zyon
