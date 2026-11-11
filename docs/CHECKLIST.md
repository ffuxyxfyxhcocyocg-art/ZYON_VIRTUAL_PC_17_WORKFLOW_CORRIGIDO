# Checklist vs MASTER PROMPT

## Entregue no código

- [x] App Android + NDK + CMake + módulos C++ (lista §26)
- [x] QemuBackend real (argv + processo)
- [x] QMP client
- [x] CPU/Mem/Machine/Chipset/Firmware/Boot managers (args QEMU)
- [x] Disco RAW/QCOW2/VMDK/VDI/VHDX via qemu-img
- [x] Snapshots + backup
- [x] Rede NAT/SLIRP + port-forward args
- [x] Áudio HDA/AC97/SB16 args
- [x] Display virtio-gpu/qxl/cirrus + VNC
- [x] USB tablet / XHCI
- [x] 9p shared folders args
- [x] Power ACPI via QMP
- [x] Security path + RAM limit
- [x] Performance host sample
- [x] Automation scheduler
- [x] Plugin dlopen
- [x] Config save/load
- [x] ApiServer loopback
- [x] Diagnostics report
- [x] UI: lista, criar, console, hardware
- [x] 16 GB clamp
- [x] Docs + script build QEMU

## Externo (build host / device)

- [ ] Compilar QEMU arm64 → jniLibs
- [ ] OVMF/SeaBIOS → assets
- [ ] Boot real Linux/Windows no aparelho
- [ ] Testes §27 no hardware

Desktop do guest = SO real, nunca fake UI.
