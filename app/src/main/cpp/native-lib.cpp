#include <jni.h>
#include <string>
#include "android/bitmap.h"
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include "opencv-utils.h"


void bitmapToMat(JNIEnv *env, jobject bitmap, Mat& dst, jboolean needUnPremultiplyAlpha)
{
    AndroidBitmapInfo  info;
    void*              pixels = 0;

    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        dst.create(info.height, info.width, CV_8UC4);
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(needUnPremultiplyAlpha) cvtColor(tmp, dst, COLOR_mRGBA2RGBA);
            else tmp.copyTo(dst);
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}

void matToBitmap(JNIEnv* env, Mat src, jobject bitmap, jboolean needPremultiplyAlpha)
{
    AndroidBitmapInfo  info;
    void*              pixels = 0;

    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );

        CV_Assert( info.width == (uint32_t)src.cols );
        CV_Assert( src.dims == 2 );
        CV_Assert( info.height == (uint32_t)src.rows);


        CV_Assert( src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, COLOR_GRAY2RGBA);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, COLOR_RGB2RGBA);
            } else if(src.type() == CV_8UC4){
                if(needPremultiplyAlpha) cvtColor(src, tmp, COLOR_RGBA2mRGBA);
                else src.copyTo(tmp);
            }
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, COLOR_GRAY2BGR565);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, COLOR_RGB2BGR565);
            } else if(src.type() == CV_8UC4){
                cvtColor(src, tmp, COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_tech_maryandrew_mangacolorizationai_AddFileActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}


extern "C" JNIEXPORT void JNICALL
Java_tech_maryandrew_mangacolorizationai_AddFileActivity_flip(JNIEnv *env, jobject p_this, jobject bitmapIn, jobject bitmapOut) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    // NOTE bitmapToMat returns Mat in RGBA format, if needed convert to BGRA using cvtColor

    myFlip(src);

    // NOTE matToBitmap expects Mat in GRAY/RGB(A) format, if needed convert using cvtColor
    matToBitmap(env, src, bitmapOut, false);
}


extern "C" JNIEXPORT void JNICALL
    Java_tech_maryandrew_mangacolorizationai_AddFileActivity_resize(JNIEnv *env, jobject p_this, jobject bitmapIn, jobject bitmapOut) {
    Mat src,dest;
    bitmapToMat(env, bitmapIn, src, false);
    bitmapToMat(env, bitmapOut, dest, false);
    // NOTE bitmapToMat returns Mat in RGBA format, if needed convert to BGRA using cvtColor

    myResize(src,dest);

//    src.resize(512,512);

    matToBitmap(env,dest,bitmapOut, false);
//    jintArray jintArray1= matToBitmapArray(env,dest);

//    return bitmapOut;
}



extern "C" JNIEXPORT void JNICALL
Java_tech_maryandrew_mangacolorizationai_AddFileActivity_blur(JNIEnv *env, jobject p_this, jobject bitmapIn, jobject bitmapOut, jfloat sigma) {
    Mat src;
    bitmapToMat(env, bitmapIn, src, false);
    myBlur(src, sigma);
    matToBitmap(env, src, bitmapOut, false);

}

extern "C"
JNIEXPORT jstring JNICALL
Java_tech_maryandrew_mangacolorizationai_AddFileActivity_getfirst10(JNIEnv *env,jobject /* this */) {
    std::string hello = "5703835-999999999-0-6209202-7419188-3400414-3041307-6955588-8755524-4776820";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_tech_maryandrew_mangacolorizationai_TrySampleActivity_getfirst10(JNIEnv *env, jobject thiz) {
        std::string hello = "5703835-999999999-0-6209202-7419188-3400414-3041307-6955588-8755524-4776820";
        return env->NewStringUTF(hello.c_str());
    // TODO: implement getfirst10()
}

extern "C"
JNIEXPORT jstring JNICALL
Java_tech_maryandrew_mangacolorizationai_ViewPagerActivity_getfirst10(JNIEnv *env, jobject thiz) {
    // TODO: implement getfirst10()
    std::string hello = "5703835-999999999-0-6209202-7419188-3400414-3041307-6955588-8755524-4776820";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jstring JNICALL
Java_tech_maryandrew_mangacolorizationai_LoginActivity_getfirst10(JNIEnv *env, jobject thiz) {
    // TODO: implement getfirst10()
    std::string hello = "5703835-999999999-0-6209202-7419188-3400414-3041307-6955588-8755524-4776820";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_grey2color_MainActivity_resize(JNIEnv *env, jobject thiz, jobject bitmapIn,
                                                jobject bitmapOut) {
    Mat src,dest;
    bitmapToMat(env, bitmapIn, src, false);
    bitmapToMat(env, bitmapOut, dest, false);
    // NOTE bitmapToMat returns Mat in RGBA format, if needed convert to BGRA using cvtColor

    myResize(src,dest);

//    src.resize(512,512);

    matToBitmap(env,dest,bitmapOut, false);
}