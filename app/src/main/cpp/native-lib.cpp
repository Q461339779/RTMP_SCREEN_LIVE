#include <jni.h>
#include <string>
#include "librtmp/rtmp.h"

RTMP *rtmp = 0;
extern "C" JNIEXPORT jstring JNICALL
Java_com_g_rtmp_1screen_1live_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    rtmp = RTMP_Alloc();
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_g_rtmp_1screen_1live_ScreenLive_connect(JNIEnv *env, jobject thiz, jstring url) {


}