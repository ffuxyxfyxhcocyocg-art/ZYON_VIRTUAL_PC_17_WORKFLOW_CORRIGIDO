#pragma once

#include <string>
#include <system_error>

namespace zyon {

enum class ErrorCode {
    Ok = 0,
    InvalidArgument,
    NotFound,
    AlreadyExists,
    PermissionDenied,
    OutOfMemory,
    IoError,
    NetworkError,
    QemuError,
    QmpError,
    FirmwareError,
    DiskError,
    SnapshotError,
    ConfigError,
    Unsupported,
    Timeout,
    Internal,
    AndroidBridgeError
};

class Error {
public:
    Error() = default;
    Error(ErrorCode code, std::string message)
        : code_(code), message_(std::move(message)) {}

    ErrorCode code() const { return code_; }
    const std::string& message() const { return message_; }
    bool ok() const { return code_ == ErrorCode::Ok; }
    explicit operator bool() const { return !ok(); }

    static Error Ok() { return Error(ErrorCode::Ok, ""); }
    static Error Make(ErrorCode c, std::string msg) { return Error(c, std::move(msg)); }

private:
    ErrorCode code_ = ErrorCode::Ok;
    std::string message_;
};

template <typename T>
class Result {
public:
    Result(T value) : value_(std::move(value)), error_(Error::Ok()) {}
    Result(Error err) : error_(std::move(err)) {}

    bool ok() const { return error_.ok(); }
    explicit operator bool() const { return ok(); }
    const T& value() const { return value_; }
    T& value() { return value_; }
    const Error& error() const { return error_; }

    static Result Ok(T v) { return Result(std::move(v)); }
    static Result Fail(Error e) { return Result(std::move(e)); }
    static Result Fail(ErrorCode c, std::string msg) {
        return Result(Error::Make(c, std::move(msg)));
    }

private:
    T value_{};
    Error error_;
};

} // namespace zyon
