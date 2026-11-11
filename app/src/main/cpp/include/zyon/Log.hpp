#pragma once

#include <string>
#include <cstdarg>
#include <android/log.h>

namespace zyon {

enum class LogLevel { Verbose, Debug, Info, Warn, Error, Fatal };

class Logger {
public:
    static void set_tag(const char* tag) { tag_ = tag; }
    static void set_level(LogLevel lvl) { level_ = lvl; }

    static void v(const char* fmt, ...) {
        va_list ap; va_start(ap, fmt);
        log(LogLevel::Verbose, fmt, ap);
        va_end(ap);
    }
    static void d(const char* fmt, ...) {
        va_list ap; va_start(ap, fmt);
        log(LogLevel::Debug, fmt, ap);
        va_end(ap);
    }
    static void i(const char* fmt, ...) {
        va_list ap; va_start(ap, fmt);
        log(LogLevel::Info, fmt, ap);
        va_end(ap);
    }
    static void w(const char* fmt, ...) {
        va_list ap; va_start(ap, fmt);
        log(LogLevel::Warn, fmt, ap);
        va_end(ap);
    }
    static void e(const char* fmt, ...) {
        va_list ap; va_start(ap, fmt);
        log(LogLevel::Error, fmt, ap);
        va_end(ap);
    }

private:
    static void log(LogLevel lvl, const char* fmt, va_list ap) {
        if (static_cast<int>(lvl) < static_cast<int>(level_)) return;
        android_LogPriority prio = ANDROID_LOG_INFO;
        switch (lvl) {
            case LogLevel::Verbose: prio = ANDROID_LOG_VERBOSE; break;
            case LogLevel::Debug:   prio = ANDROID_LOG_DEBUG; break;
            case LogLevel::Info:    prio = ANDROID_LOG_INFO; break;
            case LogLevel::Warn:    prio = ANDROID_LOG_WARN; break;
            case LogLevel::Error:   prio = ANDROID_LOG_ERROR; break;
            case LogLevel::Fatal:   prio = ANDROID_LOG_FATAL; break;
        }
        __android_log_vprint(prio, tag_, fmt, ap);
    }

    static inline const char* tag_ = "ZyonPC";
    static inline LogLevel level_ = LogLevel::Debug;
};

#define ZLOGV(...) ::zyon::Logger::v(__VA_ARGS__)
#define ZLOGD(...) ::zyon::Logger::d(__VA_ARGS__)
#define ZLOGI(...) ::zyon::Logger::i(__VA_ARGS__)
#define ZLOGW(...) ::zyon::Logger::w(__VA_ARGS__)
#define ZLOGE(...) ::zyon::Logger::e(__VA_ARGS__)

} // namespace zyon
