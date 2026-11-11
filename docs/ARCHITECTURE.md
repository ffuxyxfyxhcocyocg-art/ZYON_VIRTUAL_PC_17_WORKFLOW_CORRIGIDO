# ZYON VIRTUAL PC 17 — Architecture

## Layers

1. **Android Host App** (Java/Kotlin)
   - UI (Main, Create, Console, Hardware Editor)
   - Foreground Service (keeps VM alive)
   - SAF for ISO/disk import
   - SurfaceView → native ANativeWindow

2. **JNI Bridge** (`jni_bridge.cpp` + `ZyonNative.java`)
   - Thin, stable API surface

3. **C++ Core** (C++20 modules)
   - `VmManager` owns lifecycle
   - `QemuBackend` builds argv + fork/exec real QEMU
   - `QmpClient` talks JSON over Unix socket
   - Device managers (CPU/Mem/Disk/Net/Audio/USB/…)

4. **QEMU SoftMMU**
   - Real `qemu-system-x86_64` / `qemu-system-aarch64`
   - TCG (no KVM on stock Android)
   - VirtIO devices, SLIRP, OVMF/SeaBIOS

5. **Guest OS**
   - Real Windows / Linux / FreeBSD / ReactOS / Android-x86 images
   - Desktop belongs to the guest, never faked by the host

## Data flow (start VM)

```
UI → ZyonNative.nativeStartVm
  → VmManager.start
    → QemuBackend.build_argv(VmConfig)
    → fork + execve(qemu-system-*)
    → QmpClient.connect(qmp.sock)
    → DisplayManager.attach(ANativeWindow) or VNC :5901
```

## Persistence

```
/data/data/com.zyon.virtualpc/files/zyon/
├── vms/<name>/
│   ├── config.zyon
│   ├── disks/*.qcow2
│   ├── snapshots/
│   ├── qmp.sock
│   └── monitor.sock
├── isos/
├── firmware/
└── backups/
```

## 16 GB RAM

`MemoryConfig.max_ram_bytes = 16 GiB`.  
The backend clamps to host free memory and never overcommits beyond what the device can allocate. On typical phones the practical limit is 2–4 GiB guest.
