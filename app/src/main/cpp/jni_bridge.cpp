#include <jni.h>
#include <cstring>
#include <string>
#include <android/native_window_jni.h>
#include "include/zyon/Log.hpp"
#include "include/zyon/Error.hpp"
#include "include/zyon/VmConfig.hpp"

// Forward — modules compile as part of libzyon_core
namespace zyon {
class VmManager;
}

extern "C" {

static JavaVM* g_vm = nullptr;

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void*) {
    g_vm = vm;
    ZLOGI("ZYON VIRTUAL PC 17 native loaded");
    return JNI_VERSION_1_6;
}

JNIEXPORT jint JNICALL
Java_com_zyon_virtualpc_nativebridge_ZyonNative_nativeInit(JNIEnv* env, jclass, jstring dataRoot) {
    const char* root = env->GetStringUTFChars(dataRoot, nullptr);
    ZLOGI("nativeInit root=%s", root ? root : "(null)");
    if (root) env->ReleaseStringUTFChars(dataRoot, root);
    return -1; // QEMU runtime is not integrated in this source package.
}

JNIEXPORT jstring JNICALL
Java_com_zyon_virtualpc_nativebridge_ZyonNative_nativeCreateVm(JNIEnv* env, jclass,
        jstring name, jint arch, jint cores, jlong ramMb, jstring workDir) {
    const char* n = env->GetStringUTFChars(name, nullptr);
    const char* wd = env->GetStringUTFChars(workDir, nullptr);
    ZLOGI("createVm name=%s arch=%d cores=%d ramMb=%lld wd=%s",
          n, arch, cores, (long long)ramMb, wd ? wd : "");
    if (n) env->ReleaseStringUTFChars(name, n);
    if (wd) env->ReleaseStringUTFChars(workDir, wd);
    return nullptr; // Never create a fake VM record.
}

JNIEXPORT jint JNICALL
Java_com_zyon_virtualpc_nativebridge_ZyonNative_nativeStartVm(JNIEnv* env, jclass, jstring name) {
    const char* n = env->GetStringUTFChars(name, nullptr);
    ZLOGI("startVm %s", n ? n : "");
    if (n) env->ReleaseStringUTFChars(name, n);
    return -1;
}

JNIEXPORT jint JNICALL
Java_com_zyon_virtualpc_nativebridge_ZyonNative_nativeStopVm(JNIEnv* env, jclass, jstring name, jboolean force) {
    const char* n = env->GetStringUTFChars(name, nullptr);
    ZLOGI("stopVm %s force=%d", n ? n : "", (int)force);
    if (n) env->ReleaseStringUTFChars(name, n);
    return -1;
}

JNIEXPORT jint JNICALL
Java_com_zyon_virtualpc_nativebridge_ZyonNative_nativePauseVm(JNIEnv* env, jclass, jstring name) {
    const char* n = env->GetStringUTFChars(name, nullptr);
    ZLOGI("pauseVm %s", n ? n : "");
    if (n) env->ReleaseStringUTFChars(name, n);
    return -1;
}

JNIEXPORT jint JNICALL
Java_com_zyon_virtualpc_nativebridge_ZyonNative_nativeResumeVm(JNIEnv* env, jclass, jstring name) {
    const char* n = env->GetStringUTFChars(name, nullptr);
    ZLOGI("resumeVm %s", n ? n : "");
    if (n) env->ReleaseStringUTFChars(name, n);
    return -1;
}

JNIEXPORT jobjectArray JNICALL
Java_com_zyon_virtualpc_nativebridge_ZyonNative_nativeListVms(JNIEnv* env, jclass) {
    return env->NewObjectArray(0, env->FindClass("java/lang/String"), nullptr);
}

JNIEXPORT jint JNICALL
Java_com_zyon_virtualpc_nativebridge_ZyonNative_nativeGetState(JNIEnv*, jclass, jstring) {
    return -1; // unavailable, not running
}

JNIEXPORT void JNICALL
Java_com_zyon_virtualpc_nativebridge_ZyonNative_nativeSetSurface(JNIEnv* env, jclass,
        jstring name, jobject surface) {
    const char* n = env->GetStringUTFChars(name, nullptr);
    ANativeWindow* window = ANativeWindow_fromSurface(env, surface);
    ZLOGI("setSurface vm=%s window=%p", n ? n : "", window);
    if (window) ANativeWindow_release(window);
    if (n) env->ReleaseStringUTFChars(name, n);
}

JNIEXPORT jstring JNICALL
Java_com_zyon_virtualpc_nativebridge_ZyonNative_nativeVersion(JNIEnv* env, jclass) {
    return env->NewStringUTF("ZYON VIRTUAL PC 17.0.0");
}

JNIEXPORT jlong JNICALL
Java_com_zyon_virtualpc_nativebridge_ZyonNative_nativeHostFreeRam(JNIEnv*, jclass) {
    // Quick /proc/meminfo Available
    FILE* f = fopen("/proc/meminfo", "r");
    if (!f) return 0;
    char key[64]; unsigned long val; char unit[32];
    jlong avail = 0;
    while (fscanf(f, "%63s %lu %31s", key, &val, unit) == 3) {
        if (strcmp(key, "MemAvailable:") == 0) { avail = (jlong)val * 1024L; break; }
    }
    fclose(f);
    return avail;
}

} // extern "C"
