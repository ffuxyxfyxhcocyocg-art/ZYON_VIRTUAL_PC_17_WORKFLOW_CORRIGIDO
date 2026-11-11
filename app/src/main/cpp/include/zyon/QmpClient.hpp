#pragma once
#include "Error.hpp"
#include "Types.hpp"
#include <string>
#include <functional>
#include <mutex>
#include <atomic>
#include <thread>
#include <queue>

namespace zyon {

class QmpClient {
public:
    using EventCallback = std::function<void(const std::string& event, const std::string& data)>;

    QmpClient() = default;
    ~QmpClient() { disconnect(); }

    Error connect(const std::string& unix_socket_path);
    void disconnect();
    bool is_connected() const { return connected_; }

    // Synchronous QMP commands
    Result<std::string> query_status();
    Result<std::string> query_cpus();
    Result<std::string> query_machines();
    Result<std::string> system_powerdown();
    Result<std::string> system_reset();
    Result<std::string> stop();
    Result<std::string> cont();
    Result<std::string> quit();
    Result<std::string> screendump(const std::string& path);
    Result<std::string> human_monitor_command(const std::string& cmd);
    Result<std::string> raw_command(const std::string& json);

    void set_event_callback(EventCallback cb) { event_cb_ = std::move(cb); }

private:
    Result<std::string> send_and_recv(const std::string& json);
    void reader_loop();

    int fd_ = -1;
    std::atomic<bool> connected_{false};
    std::atomic<bool> stop_reader_{false};
    std::thread reader_thread_;
    std::mutex io_mu_;
    EventCallback event_cb_;
    u64 next_id_ = 1;
};

} // namespace zyon
