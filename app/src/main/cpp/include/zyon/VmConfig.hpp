#pragma once

#include "Types.hpp"
#include <string>
#include <vector>
#include <map>

namespace zyon {

struct CpuConfig {
    Arch arch = Arch::X86_64;
    u32 cores = 2;
    u32 threads = 1;
    u32 sockets = 1;
    std::string model = "qemu64";       // or host, max, cortex-a57, etc.
    bool kvm = false;                   // hardware accel when available
    bool tcg_mttcg = true;
    u64 max_cpu_mhz = 0;                // 0 = unlimited presentation
};

struct MemoryConfig {
    u64 ram_bytes = 2ULL * 1024 * 1024 * 1024; // 2 GiB default
    u64 max_ram_bytes = 16ULL * 1024 * 1024 * 1024; // 16 GiB hard limit (device dependent)
    bool balloon = true;
    bool hotplug = false;
};

struct DiskConfig {
    std::string path;
    DiskFormat format = DiskFormat::QCOW2;
    u64 size_bytes = 32ULL * 1024 * 1024 * 1024; // 32 GiB
    bool readonly = false;
    bool bootable = true;
    std::string interface = "virtio"; // virtio, sata, ide, nvme, scsi
    std::string id;
};

struct CdromConfig {
    std::string path; // ISO
    bool bootable = false;
};

struct NicConfig {
    NicModel model = NicModel::VIRTIO_NET;
    NetworkMode mode = NetworkMode::NAT;
    std::string mac;
    std::vector<PortForward> forwards;
    bool enabled = true;
};

struct DisplayConfig {
    GpuModel gpu = GpuModel::VIRTIO_GPU;
    DisplayBackend backend = DisplayBackend::ANDROID_SURFACE;
    Resolution resolution{1280, 720, 32};
    bool fullscreen = false;
    bool gl = true;
    u32 vram_mb = 128;
};

struct AudioConfig {
    AudioModel model = AudioModel::HDA;
    bool enabled = true;
    bool input = false;
};

struct FirmwareConfig {
    FirmwareType type = FirmwareType::OVMF;
    std::string bios_path;
    std::string ovmf_code;
    std::string ovmf_vars;
    bool secure_boot = false;
};

struct UsbConfig {
    bool enabled = true;
    u32 version = 3; // 1, 2, 3
    bool tablet = true; // absolute pointer
};

struct SharedFolder {
    std::string host_path;
    std::string guest_tag;
    bool readonly = false;
};

struct VmConfig {
    std::string name;
    std::string uuid;
    std::string description;
    MachineType machine = MachineType::PC_Q35;
    CpuConfig cpu;
    MemoryConfig memory;
    std::vector<DiskConfig> disks;
    std::vector<CdromConfig> cdroms;
    std::vector<NicConfig> nics;
    DisplayConfig display;
    AudioConfig audio;
    FirmwareConfig firmware;
    UsbConfig usb;
    std::vector<SharedFolder> shared_folders;
    std::vector<BootDevice> boot_order = {BootDevice::HDD, BootDevice::CDROM};
    std::map<std::string, std::string> extra_args; // QEMU -device / -global overrides
    bool headless = false;
    std::string work_dir; // persistent storage root for this VM
};

} // namespace zyon
