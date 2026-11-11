#include "include/zyon/Log.hpp"
#include "include/zyon/Types.hpp"
#include <fstream>
#include <string>
#include <sstream>
#include <thread>
#include <atomic>
#include <chrono>

namespace zyon {

struct PerfSample {
    double host_cpu_pct = 0;
    u64 host_mem_used = 0;
    u64 host_mem_total = 0;
};

class PerformanceMonitor {
public:
    static PerformanceMonitor& instance() { static PerformanceMonitor p; return p; }

    PerfSample sample() {
        PerfSample s;
        std::ifstream f("/proc/meminfo");
        std::string k, u; u64 v;
        while (f >> k >> v >> u) {
            if (k == "MemTotal:") s.host_mem_total = v * 1024ULL;
            if (k == "MemAvailable:") s.host_mem_used = s.host_mem_total - v * 1024ULL;
        }
        // crude cpu: read /proc/stat once
        std::ifstream st("/proc/stat");
        std::string cpu; u64 user, nice, system, idle;
        if (st >> cpu >> user >> nice >> system >> idle) {
            u64 total = user + nice + system + idle;
            static u64 prev_total = 0, prev_idle = 0;
            if (prev_total > 0) {
                u64 dt = total - prev_total;
                u64 di = idle - prev_idle;
                if (dt > 0) s.host_cpu_pct = 100.0 * (1.0 - (double)di / dt);
            }
            prev_total = total; prev_idle = idle;
        }
        return s;
    }
};

} // namespace zyon
