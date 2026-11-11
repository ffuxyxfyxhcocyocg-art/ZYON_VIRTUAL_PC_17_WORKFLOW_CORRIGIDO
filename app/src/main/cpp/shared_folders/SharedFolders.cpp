#include "include/zyon/Log.hpp"
#include "include/zyon/VmConfig.hpp"
#include <vector>
#include <string>

namespace zyon {

class SharedFolders {
public:
    // 9p virtfs args
    static void append_qemu_args(std::vector<std::string>& a, const std::vector<SharedFolder>& folders) {
        int i = 0;
        for (const auto& f : folders) {
            if (f.host_path.empty()) continue;
            std::string id = "virtfs" + std::to_string(i++);
            std::string opts = "local,path=" + f.host_path + ",mount_tag=" +
                               (f.guest_tag.empty() ? id : f.guest_tag) +
                               ",security_model=mapped-xattr,id=" + id;
            if (f.readonly) opts += ",readonly=on";
            a.push_back("-virtfs"); a.push_back(opts);
        }
    }
};

} // namespace zyon
