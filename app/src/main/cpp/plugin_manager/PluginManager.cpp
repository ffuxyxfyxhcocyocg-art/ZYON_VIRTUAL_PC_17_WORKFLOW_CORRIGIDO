#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include <string>
#include <vector>
#include <map>
#include <dlfcn.h>

namespace zyon {

struct PluginInfo {
    std::string name;
    std::string path;
    void* handle = nullptr;
};

class PluginManager {
public:
    static PluginManager& instance() { static PluginManager p; return p; }

    Error load(const std::string& path) {
        void* h = dlopen(path.c_str(), RTLD_NOW);
        if (!h) {
            ZLOGE("dlopen failed: %s", dlerror());
            return Error::Make(ErrorCode::Internal, "dlopen failed");
        }
        PluginInfo info;
        info.path = path;
        info.name = path.substr(path.find_last_of('/') + 1);
        info.handle = h;
        plugins_[info.name] = info;
        ZLOGI("Plugin loaded %s", info.name.c_str());
        return Error::Ok();
    }

    void unload(const std::string& name) {
        auto it = plugins_.find(name);
        if (it == plugins_.end()) return;
        if (it->second.handle) dlclose(it->second.handle);
        plugins_.erase(it);
    }

    std::vector<std::string> list() const {
        std::vector<std::string> n;
        for (auto& [k, _] : plugins_) n.push_back(k);
        return n;
    }

private:
    std::map<std::string, PluginInfo> plugins_;
};

} // namespace zyon
