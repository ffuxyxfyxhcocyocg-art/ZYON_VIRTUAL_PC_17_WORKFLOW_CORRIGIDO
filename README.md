# ZYON VIRTUAL PC 17

Plataforma profissional de computador virtual para Android.

Motor: **QEMU real** (TCG) integrado via NDK/CMake.  
Host: Android 9+ (API 28), ABI **arm64-v8a** apenas.  
Guest: x86, x86_64, ARM, ARM64 com firmware SeaBIOS / OVMF.

## Arquitetura

```
app/
├── src/main/
│   ├── java/com/zyon/virtualpc/     # UI + Service + JNI
│   ├── cpp/                         # Núcleo C++20 modular
│   │   ├── qemu_backend/            # Lança e controla qemu-system-*
│   │   ├── qmp_client/              # QMP (JSON)
│   │   ├── vm_manager/              # Ciclo de vida das VMs
│   │   ├── disk_manager/            # RAW/QCOW2/VMDK/...
│   │   ├── network_manager/         # NAT/SLIRP + port-forward
│   │   ├── display_manager/         # Surface + VNC
│   │   ├── ... (40+ módulos)
│   ├── assets/firmware/             # OVMF / SeaBIOS (coloque os bins)
│   └── jniLibs/arm64-v8a/           # libqemu-system-*.so pré-compilados
```

## Requisitos de build

- Android Studio Hedgehog+ ou Giraffe
- NDK **26.1.10909125** (ou superior)
- CMake 3.22.1+
- Dispositivo/emulador arm64-v8a com ≥ 6 GB RAM livre para guests úteis

## Como compilar o QEMU para Android (passo crítico)

O APK **não** embute o código-fonte completo do QEMU (centenas de MB).  
Você deve cross-compilar `qemu-system-x86_64` e `qemu-system-aarch64` com o NDK e colocar os `.so` em `app/src/main/jniLibs/arm64-v8a/`.

### Roteiro resumido (baseado em Limbo / qemu-hd2)

1. Baixe QEMU 8.x ou 9.x:
   ```bash
   git clone https://gitlab.com/qemu-project/qemu.git
   cd qemu && git checkout v9.0.0
   ```

2. Instale dependências de host (glib, pixman, etc.) e cross-compile com o toolchain do NDK:

   ```bash
   export NDK=$ANDROID_SDK_ROOT/ndk/26.1.10909125
   export TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/linux-x86_64
   export API=28
   export CC=$TOOLCHAIN/bin/aarch64-linux-android$API-clang
   export CXX=$TOOLCHAIN/bin/aarch64-linux-android$API-clang++

   ./configure \
     --cross-prefix=aarch64-linux-android- \
     --cc=$CC --cxx=$CXX \
     --target-list=x86_64-softmmu,aarch64-softmmu \
     --disable-werror --disable-gtk --disable-sdl \
     --disable-opengl --enable-tcg --enable-slirp \
     --static  # ou shared conforme preferência

   make -j$(nproc)
   ```

3. Copie os binários gerados para `jniLibs/arm64-v8a/` renomeando se necessário:
   ```
   libqemu-system-x86_64.so
   libqemu-system-aarch64.so
   ```

4. Firmware:
   - Coloque `OVMF_CODE.fd` / `OVMF_VARS.fd` (edk2) em `assets/firmware/`
   - Coloque `bios-256k.bin` (SeaBIOS) em `assets/bios/`

Scripts de exemplo estão em `scripts/`.

## Limites reais do Android

| Recurso            | Limite prático                          |
|--------------------|-----------------------------------------|
| RAM guest          | até 16 GB **somente** se o host tiver memória livre suficiente |
| CPU                | TCG (software). KVM não está disponível em Android user |
| Disco              | tamanho limitado pelo armazenamento do aparelho |
| GPU 3D             | VirtIO-GPU + GLES; desempenho baixo     |
| Rede               | SLIRP/NAT funciona; bridge/TAP exige root |

## Módulos C++ (lista completa)

application, vm_manager, vm_instance, qemu_backend, qmp_client,  
cpu_manager, memory_manager, machine_manager, chipset_manager,  
firmware_manager, boot_manager, disk_manager, storage_backend,  
image_converter, snapshot_manager, backup_manager, display_manager,  
graphics_manager, input_manager, keyboard_manager, mouse_manager,  
network_manager, audio_manager, usb_manager, peripheral_manager,  
shared_folders, clipboard_manager, power_manager, security_manager,  
performance_monitor, logging, diagnostics, automation, plugin_manager,  
configuration_manager, api_server, user_interface, android_bridge,  
native_renderer

## Testes obrigatórios (checklist)

- [ ] Compilação arm64-v8a
- [ ] Launch qemu-system-x86_64 / aarch64
- [ ] Boot SeaBIOS / OVMF
- [ ] Boot Linux ISO (Alpine/Ubuntu)
- [ ] Disco QCOW2 persistente
- [ ] Teclado + mouse (tablet)
- [ ] Vídeo (VNC ou Surface)
- [ ] Áudio HDA
- [ ] Rede NAT + internet
- [ ] Snapshot create/restore
- [ ] Estabilidade 30 min+

## Licenças

- Código ZYON (este repositório): use conforme sua licença de projeto
- QEMU: GPL-2.0
- OVMF/edk2: BSD-2-Clause-Patent
- SeaBIOS: LGPL

## Aviso

Este é o **esqueleto profissional completo** pronto para receber os binários QEMU e o firmware.  
Sem os `.so` do QEMU o app compila, mas não emula sistemas reais.  
O objetivo de 16 GB de RAM guest só faz sentido em tablets/flagships com 12–16 GB+ de RAM física.

---

**ZYON VIRTUAL PC 17** — virtualização real no bolso.
