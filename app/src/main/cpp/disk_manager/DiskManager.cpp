#include "include/zyon/Log.hpp"
#include <cerrno>
#include "include/zyon/Error.hpp"
#include "include/zyon/VmConfig.hpp"
#include <string>
#include <cstdlib>
#include <sys/stat.h>
#include <unistd.h>
#include <sstream>

namespace zyon {

class DiskManager {
public:
    static DiskManager& instance() { static DiskManager d; return d; }

    Error create_disk(const std::string& path, DiskFormat fmt, u64 size_bytes,
                      const std::string& qemu_img_bin = "qemu-img") {
        std::string format = format_to_string(fmt);
        u64 size_mb = size_bytes / (1024 * 1024);
        if (size_mb < 1) size_mb = 1;
        auto pos = path.find_last_of('/');
        if (pos != std::string::npos) {
            system(("mkdir -p \"" + path.substr(0, pos) + "\"").c_str());
        }
        std::ostringstream cmd;
        cmd << "\"" << qemu_img_bin << "\" create -f " << format
            << " \"" << path << "\" " << size_mb << "M";
        ZLOGI("DiskManager: %s", cmd.str().c_str());
        int rc = system(cmd.str().c_str());
        if (rc != 0) return Error::Make(ErrorCode::DiskError, "qemu-img create failed");
        return Error::Ok();
    }

    Error resize_disk(const std::string& path, u64 new_size_bytes,
                      const std::string& qemu_img_bin = "qemu-img") {
        u64 size_mb = new_size_bytes / (1024 * 1024);
        std::ostringstream cmd;
        cmd << "\"" << qemu_img_bin << "\" resize \"" << path << "\" " << size_mb << "M";
        if (system(cmd.str().c_str()) != 0) return Error::Make(ErrorCode::DiskError, "resize failed");
        return Error::Ok();
    }

    Error convert_disk(const std::string& src, const std::string& dst, DiskFormat out_fmt,
                       const std::string& qemu_img_bin = "qemu-img") {
        std::ostringstream cmd;
        cmd << "\"" << qemu_img_bin << "\" convert -O " << format_to_string(out_fmt)
            << " \"" << src << "\" \"" << dst << "\"";
        if (system(cmd.str().c_str()) != 0) return Error::Make(ErrorCode::DiskError, "convert failed");
        return Error::Ok();
    }

    Result<std::string> info(const std::string& path, const std::string& qemu_img_bin = "qemu-img") {
        std::ostringstream cmd;
        cmd << "\"" << qemu_img_bin << "\" info \"" << path << "\" 2>/dev/null";
        FILE* p = popen(cmd.str().c_str(), "r");
        if (!p) return Result<std::string>::Fail(ErrorCode::DiskError, "popen failed");
        char buf[4096]; std::string out;
        while (fgets(buf, sizeof(buf), p)) out += buf;
        pclose(p);
        return Result<std::string>::Ok(out);
    }

    bool exists(const std::string& path) {
        struct stat st{};
        return stat(path.c_str(), &st) == 0 && S_ISREG(st.st_mode);
    }

    Error delete_disk(const std::string& path) {
        if (unlink(path.c_str()) != 0 && errno != ENOENT)
            return Error::Make(ErrorCode::IoError, "unlink failed");
        return Error::Ok();
    }

    static std::string format_to_string(DiskFormat f) {
        switch (f) {
            case DiskFormat::RAW: return "raw";
            case DiskFormat::QCOW2: return "qcow2";
            case DiskFormat::VMDK: return "vmdk";
            case DiskFormat::VDI: return "vdi";
            case DiskFormat::VHDX: return "vhdx";
            default: return "qcow2";
        }
    }
};

} // namespace zyon
