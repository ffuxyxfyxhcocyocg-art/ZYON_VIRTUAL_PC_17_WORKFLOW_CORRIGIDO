#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include "include/zyon/VmConfig.hpp"
#include <string>
#include <vector>
#include <sstream>
#include <unistd.h>
#include <sys/wait.h>
#include <fcntl.h>
#include <signal.h>
#include <fstream>
#include <thread>
#include <atomic>
#include <mutex>

namespace zyon {

class QemuBackend {
public:
    QemuBackend() = default;
    ~QemuBackend() { stop(); }

    Error start(const VmConfig& cfg, const std::string& qemu_bin) {
        if (running_) return Error::Make(ErrorCode::AlreadyExists, "QEMU already running");

        auto args = build_argv(cfg, qemu_bin);
        if (args.empty()) {
            return Error::Make(ErrorCode::InvalidArgument, "Failed to build QEMU argv");
        }

        // Create pipes for stdout/stderr monitoring
        int stdout_pipe[2];
        int stderr_pipe[2];
        if (pipe(stdout_pipe) < 0 || pipe(stderr_pipe) < 0) {
            return Error::Make(ErrorCode::IoError, "pipe() failed");
        }

        pid_t pid = fork();
        if (pid < 0) {
            close(stdout_pipe[0]); close(stdout_pipe[1]);
            close(stderr_pipe[0]); close(stderr_pipe[1]);
            return Error::Make(ErrorCode::Internal, "fork() failed");
        }

        if (pid == 0) {
            // Child
            close(stdout_pipe[0]);
            close(stderr_pipe[0]);
            dup2(stdout_pipe[1], STDOUT_FILENO);
            dup2(stderr_pipe[1], STDERR_FILENO);
            close(stdout_pipe[1]);
            close(stderr_pipe[1]);

            // Build char* argv
            std::vector<char*> argv_c;
            for (auto& s : args) argv_c.push_back(s.data());
            argv_c.push_back(nullptr);

            execv(qemu_bin.c_str(), argv_c.data());
            _exit(127);
        }

        // Parent
        close(stdout_pipe[1]);
        close(stderr_pipe[1]);
        fcntl(stdout_pipe[0], F_SETFL, O_NONBLOCK);
        fcntl(stderr_pipe[0], F_SETFL, O_NONBLOCK);

        pid_ = pid;
        running_ = true;
        qmp_sock_ = cfg.work_dir + "/qmp.sock";
        monitor_sock_ = cfg.work_dir + "/monitor.sock";

        // Reader thread for logs
        log_thread_ = std::thread([this, out = stdout_pipe[0], err = stderr_pipe[0]]() {
            char buf[4096];
            while (running_) {
                ssize_t n = read(out, buf, sizeof(buf) - 1);
                if (n > 0) {
                    buf[n] = 0;
                    ZLOGI("QEMU: %s", buf);
                }
                n = read(err, buf, sizeof(buf) - 1);
                if (n > 0) {
                    buf[n] = 0;
                    ZLOGW("QEMU-ERR: %s", buf);
                }
                usleep(20000);
            }
            close(out);
            close(err);
        });

        ZLOGI("QEMU started pid=%d bin=%s", (int)pid_, qemu_bin.c_str());
        return Error::Ok();
    }

    Error stop() {
        if (!running_) return Error::Ok();
        running_ = false;
        if (pid_ > 0) {
            kill(pid_, SIGTERM);
            int status = 0;
            for (int i = 0; i < 50; ++i) {
                if (waitpid(pid_, &status, WNOHANG) > 0) break;
                usleep(100000);
            }
            if (waitpid(pid_, &status, WNOHANG) == 0) {
                kill(pid_, SIGKILL);
                waitpid(pid_, &status, 0);
            }
            pid_ = -1;
        }
        if (log_thread_.joinable()) log_thread_.join();
        return Error::Ok();
    }

    bool is_running() const { return running_ && pid_ > 0; }
    pid_t pid() const { return pid_; }
    const std::string& qmp_socket() const { return qmp_sock_; }

    // Build full QEMU command line from VmConfig
    static std::vector<std::string> build_argv(const VmConfig& cfg, const std::string& qemu_bin) {
        std::vector<std::string> a;
        a.push_back(qemu_bin);

        // Machine
        switch (cfg.machine) {
            case MachineType::PC_Q35:
                a.push_back("-machine"); a.push_back("q35,accel=tcg");
                break;
            case MachineType::PC_I440FX:
                a.push_back("-machine"); a.push_back("pc,accel=tcg");
                break;
            case MachineType::VIRT_AARCH64:
                a.push_back("-machine"); a.push_back("virt,accel=tcg,gic-version=3");
                break;
            default:
                a.push_back("-machine"); a.push_back("q35,accel=tcg");
                break;
        }

        // CPU
        a.push_back("-cpu");
        a.push_back(cfg.cpu.model.empty() ? "qemu64" : cfg.cpu.model);
        a.push_back("-smp");
        a.push_back(std::to_string(cfg.cpu.cores) + ",sockets=" +
                    std::to_string(cfg.cpu.sockets) + ",cores=" +
                    std::to_string(cfg.cpu.cores) + ",threads=" +
                    std::to_string(cfg.cpu.threads));

        // Memory
        u64 mb = cfg.memory.ram_bytes / (1024 * 1024);
        if (mb < 128) mb = 128;
        if (mb > 16384) mb = 16384; // hard 16 GiB cap
        a.push_back("-m");
        a.push_back(std::to_string(mb));

        // Firmware / BIOS
        if (cfg.firmware.type == FirmwareType::OVMF || cfg.firmware.type == FirmwareType::OVMF_SECURE) {
            if (!cfg.firmware.ovmf_code.empty()) {
                a.push_back("-drive");
                a.push_back("if=pflash,format=raw,readonly=on,file=" + cfg.firmware.ovmf_code);
            }
            if (!cfg.firmware.ovmf_vars.empty()) {
                a.push_back("-drive");
                a.push_back("if=pflash,format=raw,file=" + cfg.firmware.ovmf_vars);
            }
        } else if (!cfg.firmware.bios_path.empty()) {
            a.push_back("-bios");
            a.push_back(cfg.firmware.bios_path);
        }

        // Disks
        int disk_idx = 0;
        for (const auto& d : cfg.disks) {
            std::string fmt = "qcow2";
            switch (d.format) {
                case DiskFormat::RAW:  fmt = "raw"; break;
                case DiskFormat::QCOW2: fmt = "qcow2"; break;
                case DiskFormat::VMDK: fmt = "vmdk"; break;
                case DiskFormat::VDI:  fmt = "vdi"; break;
                case DiskFormat::VHDX: fmt = "vhdx"; break;
                default: break;
            }
            std::string id = d.id.empty() ? ("disk" + std::to_string(disk_idx)) : d.id;
            a.push_back("-drive");
            a.push_back("file=" + d.path + ",format=" + fmt +
                        ",if=" + d.interface + ",id=" + id +
                        (d.readonly ? ",readonly=on" : ""));
            ++disk_idx;
        }

        // CD-ROMs / ISOs
        for (const auto& c : cfg.cdroms) {
            if (c.path.empty()) continue;
            a.push_back("-cdrom");
            a.push_back(c.path);
        }

        // Boot order
        std::string boot = "order=";
        for (auto b : cfg.boot_order) {
            switch (b) {
                case BootDevice::HDD:     boot += "c"; break;
                case BootDevice::CDROM:   boot += "d"; break;
                case BootDevice::NETWORK: boot += "n"; break;
                case BootDevice::FLOPPY:  boot += "a"; break;
                default: break;
            }
        }
        a.push_back("-boot");
        a.push_back(boot);

        // Network
        int net_idx = 0;
        for (const auto& n : cfg.nics) {
            if (!n.enabled) continue;
            std::string model = "virtio-net-pci";
            switch (n.model) {
                case NicModel::E1000: model = "e1000"; break;
                case NicModel::RTL8139: model = "rtl8139"; break;
                case NicModel::VIRTIO_NET:
                case NicModel::VIRTIO_NET_PCI: model = "virtio-net-pci"; break;
            }
            a.push_back("-netdev");
            std::string netdev = "user,id=net" + std::to_string(net_idx);
            for (const auto& pf : n.forwards) {
                netdev += ",hostfwd=" + pf.protocol + "::" +
                          std::to_string(pf.host_port) + "-:" +
                          std::to_string(pf.guest_port);
            }
            a.push_back(netdev);
            a.push_back("-device");
            a.push_back(model + ",netdev=net" + std::to_string(net_idx) +
                        (n.mac.empty() ? "" : ",mac=" + n.mac));
            ++net_idx;
        }
        if (net_idx == 0) {
            a.push_back("-nic"); a.push_back("none");
        }

        // Display / GPU
        switch (cfg.display.gpu) {
            case GpuModel::VIRTIO_GPU:
                a.push_back("-device"); a.push_back("virtio-gpu-pci");
                break;
            case GpuModel::QXL:
                a.push_back("-device"); a.push_back("qxl-vga");
                break;
            case GpuModel::CIRRUS:
                a.push_back("-vga"); a.push_back("cirrus");
                break;
            default:
                a.push_back("-vga"); a.push_back("std");
                break;
        }
        // On Android we typically run headless + custom surface or VNC
        a.push_back("-display"); a.push_back("none");
        a.push_back("-vnc"); a.push_back("127.0.0.1:1,to=10"); // :5901

        // Audio
        if (cfg.audio.enabled) {
            a.push_back("-device"); a.push_back("intel-hda");
            a.push_back("-device"); a.push_back("hda-duplex");
            a.push_back("-audiodev"); a.push_back("none,id=audio0");
        } else {
            a.push_back("-audio"); a.push_back("none");
        }

        // USB tablet for absolute mouse
        if (cfg.usb.tablet) {
            a.push_back("-device"); a.push_back("qemu-xhci");
            a.push_back("-device"); a.push_back("usb-tablet");
        }

        // QMP + monitor sockets
        std::string qmp = cfg.work_dir + "/qmp.sock";
        std::string mon = cfg.work_dir + "/monitor.sock";
        a.push_back("-qmp");
        a.push_back("unix:" + qmp + ",server,nowait");
        a.push_back("-monitor");
        a.push_back("unix:" + mon + ",server,nowait");

        // Name / UUID
        if (!cfg.name.empty()) {
            a.push_back("-name"); a.push_back(cfg.name);
        }
        if (!cfg.uuid.empty()) {
            a.push_back("-uuid"); a.push_back(cfg.uuid);
        }

        // Extra args
        for (const auto& [k, v] : cfg.extra_args) {
            a.push_back(k);
            if (!v.empty()) a.push_back(v);
        }

        // No daemonize – we manage lifecycle
        a.push_back("-nodefaults");

        return a;
    }

private:
    std::atomic<bool> running_{false};
    pid_t pid_{-1};
    std::string qmp_sock_;
    std::string monitor_sock_;
    std::thread log_thread_;
};

} // namespace zyon
