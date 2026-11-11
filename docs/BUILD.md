# Build Guide — ZYON VIRTUAL PC 17

## 1. Open in Android Studio

1. File → Open → select the `ZyonVirtualPC16` folder
2. Let Gradle sync
3. Install NDK 26.x and CMake 3.22+ via SDK Manager if missing

## 2. Build QEMU (required for real guests)

See `scripts/build_qemu_android.sh`.  
After success, binaries must live under:

```
app/src/main/jniLibs/arm64-v8a/
  qemu-system-x86_64
  qemu-system-aarch64
  qemu-img
```

Or as shared libraries if you wrap them.

## 3. Firmware

Place in assets:

```
app/src/main/assets/firmware/OVMF_CODE.fd
app/src/main/assets/firmware/OVMF_VARS.fd
app/src/main/assets/bios/bios-256k.bin
```

## 4. Compile APK

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Target size of the **APK** (without guest images) can stay under ~100 MB if QEMU is externalized or heavily stripped. Guest disks and ISOs are stored outside the APK.

## 5. First run checklist

1. Grant storage / all-files access if needed
2. Create VM (x86_64, 2 cores, 2048 MB)
3. Attach an Alpine or Ubuntu ISO via SAF
4. Start → observe QEMU logcat
5. Connect to VNC 127.0.0.1:5901 from a VNC client or implement Surface rendering
