#include "include/zyon/Log.hpp"
#include <string>
#include <sstream>
#include <fstream>
#include <sys/utsname.h>

namespace zyon {

class Diagnostics {
public:
    static std::string report() {
        std::ostringstream o;
        o << "=== ZYON VIRTUAL PC 16 Diagnostics ===\n";
        utsname u{};
        if (uname(&u) == 0) {
            o << "kernel: " << u.sysname << " " << u.release << " " << u.machine << "\n";
        }
        std::ifstream mem("/proc/meminfo");
        std::string line;
        int lines = 0;
        while (std::getline(mem, line) && lines < 5) { o << line << "\n"; ++lines; }
        std::ifstream cpu("/proc/cpuinfo");
        lines = 0;
        while (std::getline(cpu, line) && lines < 8) {
            if (line.find("model name") != std::string::npos ||
                line.find("processor") != std::string::npos ||
                line.find("Hardware") != std::string::npos ||
                line.find("BogoMIPS") != std::string::npos)
                o << line << "\n";
            ++lines;
        }
        return o.str();
    }
};

} // namespace zyon
