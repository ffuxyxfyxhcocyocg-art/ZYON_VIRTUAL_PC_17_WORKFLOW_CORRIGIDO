#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include <string>

namespace zyon {

class Application {
public:
    static Application& instance() { static Application a; return a; }

    Error init(const std::string& data_root) {
        data_root_ = data_root;
        ZLOGI("Application init root=%s", data_root.c_str());
        return Error::Ok();
    }

    const std::string& data_root() const { return data_root_; }
    static constexpr const char* version() { return "16.0.0"; }
    static constexpr const char* name() { return "ZYON VIRTUAL PC 16"; }

private:
    std::string data_root_;
};

} // namespace zyon
