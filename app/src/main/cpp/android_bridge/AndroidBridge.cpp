#include "include/zyon/Log.hpp"
#include <android/native_window.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <string>
#include <fstream>
#include <vector>

namespace zyon {

class AndroidBridge {
public:
    static AndroidBridge& instance() { static AndroidBridge b; return b; }

    void set_asset_manager(AAssetManager* am) { assets_ = am; }
    AAssetManager* assets() const { return assets_; }

    // Copy asset file to internal path
    bool extract_asset(const std::string& asset_path, const std::string& dest_path) {
        if (!assets_) return false;
        AAsset* a = AAssetManager_open(assets_, asset_path.c_str(), AASSET_MODE_STREAMING);
        if (!a) return false;
        std::ofstream out(dest_path, std::ios::binary);
        if (!out) { AAsset_close(a); return false; }
        char buf[8192];
        int n;
        while ((n = AAsset_read(a, buf, sizeof(buf))) > 0) out.write(buf, n);
        AAsset_close(a);
        ZLOGI("Extracted asset %s -> %s", asset_path.c_str(), dest_path.c_str());
        return true;
    }

private:
    AAssetManager* assets_ = nullptr;
};

} // namespace zyon
