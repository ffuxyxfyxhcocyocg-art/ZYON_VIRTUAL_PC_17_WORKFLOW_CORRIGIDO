#include "include/zyon/Log.hpp"
#include <algorithm>
#include "include/zyon/Error.hpp"
#include <string>
#include <vector>
#include <functional>
#include <thread>
#include <atomic>
#include <chrono>
#include <mutex>

namespace zyon {

struct ScheduledJob {
    std::string id;
    std::string vm_name;
    std::string action; // start | stop | restart | backup
    int64_t when_ms = 0;
    bool done = false;
};

class Automation {
public:
    static Automation& instance() { static Automation a; return a; }

    void schedule(const ScheduledJob& job) {
        std::lock_guard lock(mu_);
        jobs_.push_back(job);
        ZLOGI("Scheduled %s on %s at %lld", job.action.c_str(), job.vm_name.c_str(), (long long)job.when_ms);
    }

    std::vector<ScheduledJob> due(int64_t now_ms) {
        std::lock_guard lock(mu_);
        std::vector<ScheduledJob> out;
        for (auto& j : jobs_) {
            if (!j.done && j.when_ms <= now_ms) {
                j.done = true;
                out.push_back(j);
            }
        }
        return out;
    }

    void set_auto_start(const std::string& vm, bool on) {
        std::lock_guard lock(mu_);
        if (on) auto_start_.push_back(vm);
        else auto_start_.erase(std::remove(auto_start_.begin(), auto_start_.end(), vm), auto_start_.end());
    }

    const std::vector<std::string>& auto_start_list() const { return auto_start_; }

private:
    std::mutex mu_;
    std::vector<ScheduledJob> jobs_;
    std::vector<std::string> auto_start_;
};

} // namespace zyon
