#include "include/zyon/Log.hpp"
#include "include/zyon/VmConfig.hpp"
#include <string>
#include <fstream>
#include <sstream>

namespace zyon {

class MemoryManager {
public:
    // Host free memory in bytes (Linux /proc)
    static u64 host_free_bytes() {
        std::ifstream f("/proc/meminfo");
        std::string key;
        u64 value = 0;
        std::string unit;
        while (f >> key >> value >> unit) {
            if (key == "MemAvailable:") return value * 1024ULL;
        }
        return 0;
    }

    static u64 clamp_guest_ram(u64 requested) {
        const u64 hard_max = 16ULL * 1024 * 1024 * 1024; // 16 GiB
        if (requested > hard_max) requested = hard_max;
        u64 free = host_free_bytes();
        // Leave at least 1 GiB for Android host
        const u64 reserve = 1ULL * 1024 * 1024 * 1024;
        if (free > reserve && requested > free - reserve)
            requested = free - reserve;
        if (requested < 128ULL * 1024 * 1024) requested = 128ULL * 1024 * 1024;
        return requested;
    }

    static void append_qemu_args(std::vector<std::string>& a, const MemoryConfig& cfg) {
        u64 bytes = clamp_guest_ram(cfg.ram_bytes);
        u64 mb = bytes / (1024 * 1024);
        a.push_back("-m"); a.push_back(std::to_string(mb));
        if (cfg.balloon) {
            a.push_back("-device"); a.push_back("virtio-balloon-pci");
        }
        ZLOGI("Guest RAM clamped to %llu MB", (unsigned long long)mb);
    }
};

} // namespace zyon
