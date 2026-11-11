#include "include/zyon/Log.hpp"
#include "include/zyon/VmConfig.hpp"
#include <vector>
#include <string>

namespace zyon {

class ChipsetManager {
public:
    // Extra chipset devices: ICH9, LPC, HPET, RTC already implied by machine type
    static void append_qemu_args(std::vector<std::string>& a, MachineType mt) {
        if (mt == MachineType::PC_Q35) {
            // Q35 already provides ICH9 + AHCI
            a.push_back("-global"); a.push_back("ICH9-LPC.disable_s3=1");
        }
        // SMBIOS type1 identity
        a.push_back("-smbios");
        a.push_back("type=1,manufacturer=ZYON,product=VirtualPC16,version=16.0");
    }
};

} // namespace zyon
