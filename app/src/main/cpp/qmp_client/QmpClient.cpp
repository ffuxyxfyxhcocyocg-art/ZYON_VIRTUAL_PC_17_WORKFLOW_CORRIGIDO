#include "include/zyon/QmpClient.hpp"
#include "include/zyon/Log.hpp"
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <cstring>
#include <sstream>
#include <sys/select.h>

namespace zyon {

Error QmpClient::connect(const std::string& path) {
    if (connected_) return Error::Ok();
    fd_ = socket(AF_UNIX, SOCK_STREAM, 0);
    if (fd_ < 0) return Error::Make(ErrorCode::NetworkError, "socket() failed");

    sockaddr_un addr{};
    addr.sun_family = AF_UNIX;
    strncpy(addr.sun_path, path.c_str(), sizeof(addr.sun_path) - 1);

    for (int i = 0; i < 30; ++i) {
        if (::connect(fd_, reinterpret_cast<sockaddr*>(&addr), sizeof(addr)) == 0) {
            connected_ = true;
            char buf[4096];
            ssize_t n = read(fd_, buf, sizeof(buf) - 1);
            if (n > 0) { buf[n] = 0; ZLOGD("QMP greeting: %s", buf); }
            auto r = raw_command(R"({"execute":"qmp_capabilities"})");
            if (!r.ok()) ZLOGW("qmp_capabilities failed");
            stop_reader_ = false;
            reader_thread_ = std::thread([this]{ reader_loop(); });
            ZLOGI("QMP connected to %s", path.c_str());
            return Error::Ok();
        }
        usleep(100000);
    }
    close(fd_); fd_ = -1;
    return Error::Make(ErrorCode::QmpError, "connect timeout");
}

void QmpClient::disconnect() {
    stop_reader_ = true;
    connected_ = false;
    if (fd_ >= 0) { shutdown(fd_, SHUT_RDWR); close(fd_); fd_ = -1; }
    if (reader_thread_.joinable()) reader_thread_.join();
}

Result<std::string> QmpClient::send_and_recv(const std::string& json) {
    std::lock_guard lock(io_mu_);
    if (!connected_ || fd_ < 0)
        return Result<std::string>::Fail(ErrorCode::QmpError, "not connected");
    std::string msg = json + "\n";
    if (write(fd_, msg.data(), msg.size()) < 0)
        return Result<std::string>::Fail(ErrorCode::QmpError, "write failed");
    char buf[65536];
    ssize_t n = read(fd_, buf, sizeof(buf) - 1);
    if (n <= 0) return Result<std::string>::Fail(ErrorCode::QmpError, "read failed");
    buf[n] = 0;
    return Result<std::string>::Ok(std::string(buf));
}

Result<std::string> QmpClient::raw_command(const std::string& json) { return send_and_recv(json); }
Result<std::string> QmpClient::query_status() { return raw_command(R"({"execute":"query-status"})"); }
Result<std::string> QmpClient::query_cpus() { return raw_command(R"({"execute":"query-cpus"})"); }
Result<std::string> QmpClient::query_machines() { return raw_command(R"({"execute":"query-machines"})"); }
Result<std::string> QmpClient::system_powerdown() { return raw_command(R"({"execute":"system_powerdown"})"); }
Result<std::string> QmpClient::system_reset() { return raw_command(R"({"execute":"system_reset"})"); }
Result<std::string> QmpClient::stop() { return raw_command(R"({"execute":"stop"})"); }
Result<std::string> QmpClient::cont() { return raw_command(R"({"execute":"cont"})"); }
Result<std::string> QmpClient::quit() { return raw_command(R"({"execute":"quit"})"); }
Result<std::string> QmpClient::screendump(const std::string& path) {
    std::ostringstream oss;
    oss << R"({"execute":"screendump","arguments":{"filename":")" << path << R"("}})";
    return raw_command(oss.str());
}
Result<std::string> QmpClient::human_monitor_command(const std::string& cmd) {
    std::ostringstream oss;
    oss << R"({"execute":"human-monitor-command","arguments":{"command-line":")" << cmd << R"("}})";
    return raw_command(oss.str());
}

void QmpClient::reader_loop() {
    char buf[8192];
    while (!stop_reader_ && connected_) {
        fd_set rfds; FD_ZERO(&rfds); FD_SET(fd_, &rfds);
        timeval tv{0, 200000};
        int r = select(fd_ + 1, &rfds, nullptr, nullptr, &tv);
        if (r > 0 && FD_ISSET(fd_, &rfds)) {
            ssize_t n = read(fd_, buf, sizeof(buf) - 1);
            if (n <= 0) break;
            buf[n] = 0;
            std::string s(buf);
            if (s.find("\"event\"") != std::string::npos && event_cb_) event_cb_("event", s);
        }
    }
}

} // namespace zyon
