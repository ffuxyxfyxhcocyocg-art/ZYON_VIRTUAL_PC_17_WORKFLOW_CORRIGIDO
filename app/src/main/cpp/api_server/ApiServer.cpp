#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include <string>
#include <thread>
#include <atomic>
#include <sys/socket.h>
#include <netinet/in.h>
#include <unistd.h>
#include <cstring>

namespace zyon {

// Minimal local HTTP-ish control API on loopback (optional)
class ApiServer {
public:
    static ApiServer& instance() { static ApiServer a; return a; }

    Error start(int port = 17616) {
        if (running_) return Error::Ok();
        fd_ = socket(AF_INET, SOCK_STREAM, 0);
        if (fd_ < 0) return Error::Make(ErrorCode::NetworkError, "socket");
        int yes = 1;
        setsockopt(fd_, SOL_SOCKET, SO_REUSEADDR, &yes, sizeof(yes));
        sockaddr_in addr{};
        addr.sin_family = AF_INET;
        addr.sin_addr.s_addr = htonl(INADDR_LOOPBACK);
        addr.sin_port = htons(port);
        if (bind(fd_, (sockaddr*)&addr, sizeof(addr)) < 0) {
            close(fd_); fd_ = -1;
            return Error::Make(ErrorCode::NetworkError, "bind");
        }
        listen(fd_, 4);
        running_ = true;
        th_ = std::thread([this]{ loop(); });
        ZLOGI("ApiServer on 127.0.0.1:%d", port);
        return Error::Ok();
    }

    void stop() {
        running_ = false;
        if (fd_ >= 0) { shutdown(fd_, SHUT_RDWR); close(fd_); fd_ = -1; }
        if (th_.joinable()) th_.join();
    }

private:
    void loop() {
        while (running_) {
            sockaddr_in cli{}; socklen_t cl = sizeof(cli);
            int c = accept(fd_, (sockaddr*)&cli, &cl);
            if (c < 0) continue;
            char buf[1024];
            ssize_t n = read(c, buf, sizeof(buf) - 1);
            if (n > 0) {
                buf[n] = 0;
                const char* resp =
                    "HTTP/1.0 200 OK\r\nContent-Type: application/json\r\n\r\n"
                    "{\"name\":\"ZYON VIRTUAL PC 16\",\"version\":\"16.0.0\"}\n";
                write(c, resp, strlen(resp));
            }
            close(c);
        }
    }

    std::atomic<bool> running_{false};
    int fd_ = -1;
    std::thread th_;
};

} // namespace zyon
