#include "opencv-utils.h"
#include <opencv2/imgproc.hpp>

void myFlip(Mat src) {
    flip(src, src, 0);
}

void myBlur(Mat src, float sigma) {

    GaussianBlur(src, src, Size(), sigma);

}

void myResize(Mat src,Mat dest){
    int i = INTER_AREA;

    resize( src, dest, dest.size() ,0, 0,i);
}

