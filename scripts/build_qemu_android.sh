#!/usr/bin/env bash
# ZYON VIRTUAL PC 16 — cross-compile QEMU for Android arm64-v8a
# Run on a Linux x86_64 host with Android NDK installed.

set -euo pipefail

NDK="${ANDROID_NDK_HOME:-${ANDROID_SDK_ROOT}/ndk/26.1.10909125}"
API=28
QEMU_SRC="${1:-./qemu}"
OUT_DIR="${2:-../app/src/main/jniLibs/arm64-v8a}"
PREFIX="$(pwd)/build-qemu-android-prefix"

if [[ ! -d "$NDK" ]]; then
  echo "NDK not found at $NDK"
  exit 1
fi

TOOLCHAIN="$NDK/toolchains/llvm/prebuilt/linux-x86_64"
export PATH="$TOOLCHAIN/bin:$PATH"
export CC="aarch64-linux-android${API}-clang"
export CXX="aarch64-linux-android${API}-clang++"
export AR=llvm-ar
export RANLIB=llvm-ranlib
export STRIP=llvm-strip

mkdir -p "$PREFIX" "$OUT_DIR"
cd "$QEMU_SRC"

# Minimal config focused on TCG + SLIRP (no KVM on Android userland)
./configure \
  --prefix="$PREFIX" \
  --cross-prefix=aarch64-linux-android- \
  --cc="$CC" \
  --cxx="$CXX" \
  --host-cc=gcc \
  --target-list=x86_64-softmmu,aarch64-softmmu,i386-softmmu,arm-softmmu \
  --disable-werror \
  --disable-gtk \
  --disable-sdl \
  --disable-opengl \
  --disable-virglrenderer \
  --disable-vnc-sasl \
  --disable-vte \
  --disable-xen \
  --disable-brlapi \
  --disable-libusb \
  --disable-smartcard \
  --disable-libnfs \
  --disable-libiscsi \
  --disable-rbd \
  --enable-tcg \
  --enable-tools \
  --enable-slirp \
  --static

make -j"$(nproc)"
make install

# Stage shared/static binaries into jniLibs
cp -v "$PREFIX/bin/qemu-system-x86_64"   "$OUT_DIR/" || true
cp -v "$PREFIX/bin/qemu-system-aarch64" "$OUT_DIR/" || true
cp -v "$PREFIX/bin/qemu-system-i386"    "$OUT_DIR/" || true
cp -v "$PREFIX/bin/qemu-system-arm"     "$OUT_DIR/" || true
cp -v "$PREFIX/bin/qemu-img"            "$OUT_DIR/" || true

echo "Done. Binaries staged in $OUT_DIR"
echo "Remember: on Android the process is launched via execve of these binaries (or .so wrappers)."
