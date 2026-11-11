#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include <filesystem>
#include <fstream>
#include <chrono>
#include <string>

namespace fs = std::filesystem;
namespace zyon {

class BackupManager {
public:
    static Error full_backup(const std::string& vm_work_dir, const std::string& backup_root) {
        auto id = "bak_" + std::to_string(
            std::chrono::duration_cast<std::chrono::milliseconds>(
                std::chrono::system_clock::now().time_since_epoch()).count());
        std::string dst = backup_root + "/" + id;
        std::error_code ec;
        fs::create_directories(dst, ec);
        fs::copy(vm_work_dir, dst, fs::copy_options::recursive | fs::copy_options::overwrite_existing, ec);
        if (ec) return Error::Make(ErrorCode::IoError, ec.message());
        ZLOGI("Backup %s done", id.c_str());
        return Error::Ok();
    }

    static Error restore_backup(const std::string& backup_path, const std::string& vm_work_dir) {
        if (!fs::exists(backup_path)) return Error::Make(ErrorCode::NotFound, "backup missing");
        std::error_code ec;
        fs::remove_all(vm_work_dir, ec);
        fs::copy(backup_path, vm_work_dir, fs::copy_options::recursive, ec);
        if (ec) return Error::Make(ErrorCode::IoError, ec.message());
        return Error::Ok();
    }
};

} // namespace zyon
