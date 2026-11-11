#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include "include/zyon/VmConfig.hpp"
#include <memory>
#include <string>
#include <atomic>

namespace zyon {

class QemuBackend;
class QmpClient;

class VmInstance {
public:
    explicit VmInstance(VmConfig cfg) : cfg_(std::move(cfg)) {}

    const VmConfig& config() const { return cfg_; }
    VmState state() const { return state_; }
    void set_state(VmState s) { state_ = s; }

    std::string qmp_path() const { return cfg_.work_dir + "/qmp.sock"; }
    std::string monitor_path() const { return cfg_.work_dir + "/monitor.sock"; }

private:
    VmConfig cfg_;
    std::atomic<VmState> state_{VmState::CREATED};
};

} // namespace zyon
