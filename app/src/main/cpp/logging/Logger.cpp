#include "include/zyon/Log.hpp"
// Logger implementation is header-only (android log). This TU ensures link symbol.
namespace zyon {
void logger_force_link() {}
}
