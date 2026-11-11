#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include "include/zyon/QmpClient.hpp"

namespace zyon {

class PowerManager {
public:
    static Error acpi_shutdown(QmpClient& qmp) {
        auto r = qmp.system_powerdown();
        return r.ok() ? Error::Ok() : r.error();
    }
    static Error reset(QmpClient& qmp) {
        auto r = qmp.system_reset();
        return r.ok() ? Error::Ok() : r.error();
    }
    static Error pause(QmpClient& qmp) {
        auto r = qmp.stop();
        return r.ok() ? Error::Ok() : r.error();
    }
    static Error resume(QmpClient& qmp) {
        auto r = qmp.cont();
        return r.ok() ? Error::Ok() : r.error();
    }
};

} // namespace zyon
