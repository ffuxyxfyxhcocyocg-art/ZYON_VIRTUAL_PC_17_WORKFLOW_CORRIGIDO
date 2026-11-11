#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include "include/zyon/VmConfig.hpp"
#include <vector>
#include <string>
#include <sstream>

namespace zyon {

class StorageBackend {
public:
    static void append_disk(std::vector<std::string>& a, const DiskConfig& d, int idx) {
        std::string fmt = "qcow2";
        switch (d.format) {
            case DiskFormat::RAW: fmt = "raw"; break;
            case DiskFormat::QCOW2: fmt = "qcow2"; break;
            case DiskFormat::VMDK: fmt = "vmdk"; break;
            case DiskFormat::VDI: fmt = "vdi"; break;
            case DiskFormat::VHDX: fmt = "vhdx"; break;
            default: break;
        }
        std::string id = d.id.empty() ? ("hd" + std::to_string(idx)) : d.id;
        std::string iface = d.interface.empty() ? "virtio" : d.interface;

        std::ostringstream drive;
        drive << "file=" << d.path << ",format=" << fmt << ",if=none,id=" << id;
        if (d.readonly) drive << ",readonly=on";
        a.push_back("-drive"); a.push_back(drive.str());

        if (iface == "virtio" || iface == "virtio-blk") {
            a.push_back("-device"); a.push_back("virtio-blk-pci,drive=" + id);
        } else if (iface == "nvme") {
            a.push_back("-device"); a.push_back("nvme,drive=" + id + ",serial=zyon" + std::to_string(idx));
        } else if (iface == "sata" || iface == "ide") {
            a.push_back("-device"); a.push_back("ide-hd,drive=" + id);
        } else if (iface == "scsi") {
            a.push_back("-device"); a.push_back("virtio-scsi-pci,id=scsi" + std::to_string(idx));
            a.push_back("-device"); a.push_back("scsi-hd,drive=" + id);
        } else {
            a.push_back("-device"); a.push_back("virtio-blk-pci,drive=" + id);
        }
    }
};

} // namespace zyon
