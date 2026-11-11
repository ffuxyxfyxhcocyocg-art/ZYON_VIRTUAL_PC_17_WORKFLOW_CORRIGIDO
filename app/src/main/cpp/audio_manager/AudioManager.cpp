#include "include/zyon/Log.hpp"
#include "include/zyon/VmConfig.hpp"
#include <vector>
#include <string>

namespace zyon {

class AudioManager {
public:
    static void append_qemu_args(std::vector<std::string>& a, const AudioConfig& cfg) {
        if (!cfg.enabled) {
            a.push_back("-audio"); a.push_back("none");
            return;
        }
        switch (cfg.model) {
            case AudioModel::HDA:
                a.push_back("-device"); a.push_back("intel-hda");
                a.push_back("-device"); a.push_back("hda-duplex");
                break;
            case AudioModel::AC97:
                a.push_back("-device"); a.push_back("AC97");
                break;
            case AudioModel::SB16:
                a.push_back("-device"); a.push_back("sb16");
                break;
            case AudioModel::VIRTIO_SOUND:
                a.push_back("-device"); a.push_back("virtio-sound-pci");
                break;
        }
        a.push_back("-audiodev"); a.push_back("none,id=audio0");
    }
};

} // namespace zyon
