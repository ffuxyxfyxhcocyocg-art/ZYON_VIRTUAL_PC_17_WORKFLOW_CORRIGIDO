#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include "include/zyon/VmConfig.hpp"
#include <string>
#include <vector>
#include <sstream>

namespace zyon {

class NetworkManager {
public:
    static NetworkManager& instance() { static NetworkManager n; return n; }

    // Build -netdev / -device args for a NIC
    static void append_qemu_args(std::vector<std::string>& a, const NicConfig& nic, int idx) {
        if (!nic.enabled) return;
        std::string model = "virtio-net-pci";
        switch (nic.model) {
            case NicModel::E1000: model = "e1000"; break;
            case NicModel::RTL8139: model = "rtl8139"; break;
            default: break;
        }
        std::string netdev = "user,id=net" + std::to_string(idx);
        if (nic.mode == NetworkMode::ISOLATED || nic.mode == NetworkMode::NONE)
            netdev = "none";
        for (const auto& pf : nic.forwards) {
            netdev += ",hostfwd=" + pf.protocol + "::" +
                      std::to_string(pf.host_port) + "-:" + std::to_string(pf.guest_port);
        }
        a.push_back("-netdev"); a.push_back(netdev);
        if (nic.mode != NetworkMode::NONE) {
            std::string dev = model + ",netdev=net" + std::to_string(idx);
            if (!nic.mac.empty()) dev += ",mac=" + nic.mac;
            a.push_back("-device"); a.push_back(dev);
        }
    }

    Result<std::string> diagnose(const std::string& /*guest_ip*/) {
        // Host-side ping / connectivity helper (placeholder shell)
        return Result<std::string>::Ok("NAT/SLIRP active");
    }
};

} // namespace zyon
