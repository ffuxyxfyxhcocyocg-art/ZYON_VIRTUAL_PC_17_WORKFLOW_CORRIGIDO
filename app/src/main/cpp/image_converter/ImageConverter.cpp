#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include "include/zyon/VmConfig.hpp"
#include <sstream>
#include <cstdlib>

namespace zyon {

class ImageConverter {
public:
    static Error convert(const std::string& src, const std::string& dst, DiskFormat out,
                         const std::string& qemu_img = "qemu-img") {
        const char* fmt = "qcow2";
        switch (out) {
            case DiskFormat::RAW: fmt = "raw"; break;
            case DiskFormat::QCOW2: fmt = "qcow2"; break;
            case DiskFormat::VMDK: fmt = "vmdk"; break;
            case DiskFormat::VDI: fmt = "vdi"; break;
            case DiskFormat::VHDX: fmt = "vhdx"; break;
            default: break;
        }
        std::ostringstream cmd;
        cmd << "\"" << qemu_img << "\" convert -p -O " << fmt
            << " \"" << src << "\" \"" << dst << "\"";
        ZLOGI("%s", cmd.str().c_str());
        if (system(cmd.str().c_str()) != 0)
            return Error::Make(ErrorCode::DiskError, "convert failed");
        return Error::Ok();
    }

    static Error create_backing(const std::string& base, const std::string& overlay,
                                const std::string& qemu_img = "qemu-img") {
        std::ostringstream cmd;
        cmd << "\"" << qemu_img << "\" create -f qcow2 -b \"" << base
            << "\" -F qcow2 \"" << overlay << "\"";
        if (system(cmd.str().c_str()) != 0)
            return Error::Make(ErrorCode::DiskError, "backing create failed");
        return Error::Ok();
    }
};

} // namespace zyon
