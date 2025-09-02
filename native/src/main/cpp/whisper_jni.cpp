#include <jni.h>
#include <string>
#include <android/log.h>

#define LOG_TAG "WhisperNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jstring JNICALL
Java_com_whispertflite_native_WhisperNative_getVersionInfo(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "WhisperNative v1.1 - whisper.cpp integration placeholder";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_whispertflite_native_WhisperNative_isSupported(
        JNIEnv* env,
        jobject /* this */) {
    // TODO: Implement whisper.cpp model loading and check
    LOGI("Native whisper support check - placeholder implementation");
    return JNI_FALSE; // Return false until whisper.cpp is integrated
}