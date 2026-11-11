#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include "include/zyon/Types.hpp"
#include <string>
#include <fstream>
#include <sys/stat.h>

namespace zyon {

class SecurityManager {
public:
    static bool validate_image_path(const std::string& path) {
        // Reject path traversal
        if (path.find("..") != std::string::npos) return false;
        struct stat st{};
        if (stat(path.c_str(), &st) != 0) return false;
        return S_ISREG(st.st_mode);
    }

    static Error enforce_ram_limit(u64 requested, u64 max_allowed) {
        if (requested > max_allowed)
            return Error::Make(ErrorCode::OutOfMemory, "RAM exceeds device limit");
        return Error::Ok();
    }
};

} // namespace zyon
