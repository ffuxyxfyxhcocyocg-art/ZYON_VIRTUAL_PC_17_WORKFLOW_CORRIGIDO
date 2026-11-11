#pragma once

#include <cstdint>
#include <string>
#include <vector>
#include <memory>
#include <functional>
#include <optional>
#include <chrono>
#include <atomic>
#include <mutex>
#include <map>
#include <set>

namespace zyon {

using u8  = uint8_t;
using u16 = uint16_t;
using u32 = uint32_t;
using u64 = uint64_t;
using i32 = int32_t;
using i64 = int64_t;
using f32 = float;
using f64 = double;

enum class Arch : u8 {
    X86,
    X86_64,
    ARM,
    ARM64,
    RISCV64,
    Unknown
};

enum class MachineType : u8 {
    PC_I440FX,
    PC_Q35,
    VIRT_AARCH64,
    VIRT_ARM,
    CUSTOM
};

enum class FirmwareType : u8 {
    SeaBIOS,
    OVMF,
    OVMF_SECURE,
    QEMU_EFI,
    NONE
};

enum class DiskFormat : u8 {
    RAW,
    QCOW2,
    VMDK,
    VDI,
    VHDX,
    ISO,
    UNKNOWN
};

enum class NetworkMode : u8 {
    NAT,
    SLIRP,
    BRIDGE,
    TAP,
    ISOLATED,
    INTERNAL,
    NONE
};

enum class NicModel : u8 {
    E1000,
    VIRTIO_NET,
    RTL8139,
    VIRTIO_NET_PCI
};

enum class DisplayBackend : u8 {
    SDL,
    GTK,
    VNC,
    SPICE,
    ANDROID_SURFACE,
    NONE
};

enum class GpuModel : u8 {
    VGA,
    CIRRUS,
    QXL,
    VIRTIO_GPU,
    VIRTIO_GPU_GL
};

enum class AudioModel : u8 {
    HDA,
    AC97,
    SB16,
    VIRTIO_SOUND
};

enum class VmState : u8 {
    CREATED,
    STARTING,
    RUNNING,
    PAUSED,
    SUSPENDED,
    STOPPING,
    STOPPED,
    ERROR,
    SAVING,
    RESTORING
};

enum class BootDevice : u8 {
    HDD,
    CDROM,
    NETWORK,
    FLOPPY,
    USB
};

struct Resolution {
    u32 width  = 1280;
    u32 height = 720;
    u32 depth  = 32;
};

struct PortForward {
    u16 host_port = 0;
    u16 guest_port = 0;
    std::string protocol = "tcp"; // tcp | udp
};

} // namespace zyon
