package com.biubiu.example;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
/**
 * @author ：张音乐
 * @date ：Created in 2021/4/26 下午4:14
 * @description：素描特效
 * @email: zhangyule1993@sina.com
 * @version: 1.0
 */
public class ImageSketch {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        String path = "D:\\upload\\hanfu.jpg";
        Mat img = Imgcodecs.imread(path);
        if(img.empty()) {
            System.out.println("cannot open file");
            return;
        }
        Mat origin = img.clone();

        HighGui.imshow("原始图", origin);
        // 素描特效
        Mat result = toSketch(img);
        HighGui.imshow("素描图", result);
        HighGui.waitKey(0);
        // 释放所有的窗体资源
        HighGui.destroyAllWindows();
    }


    private static Mat toSketch(Mat img) {
        Mat gray = new Mat(img.size(), CvType.CV_8UC3);
        // 去色
        for(int i = 0; i < img.rows(); i++) {
            for(int j = 0; j < img.cols(); j++) {
                double b = img.get(i, j)[0];
                double g = img.get(i, j)[1];
                double r = img.get(i, j)[2];

                double max = Math.max(Math.max(b, g), r);
                double min = Math.min(Math.min(b, g), r);

                double []arr = new double[img.get(i, j).length];

                for(int k = 0; k < 3; k++) {
                    arr[k] = (min + max) / 2;
                }
                gray.put(i, j, arr);
            }
        }

        // 反色
        Mat reverse = new Mat(img.size(), CvType.CV_8UC3);
        for(int i = 0; i < gray.rows(); i++) {
            for (int j = 0; j < gray.cols(); j++) {
                double [] grayArr = gray.get(i, j);
                double []arr = new double[grayArr.length];
                for(int k = 0; k < 3; k++) {
                    arr[k] = 255 - grayArr[k];
                }
                reverse.put(i, j, arr);
            }
        }
        // 高斯模糊
        Mat thresh = new Mat();
        Imgproc.GaussianBlur(reverse, thresh, new Size(7, 7), 3, 3);

        Mat result = new Mat(gray.size(), CvType.CV_8UC3);
        // 模糊后的图像叠加模式选择颜色减淡效果。
        for(int i = 0; i < gray.rows(); i++) {
            for (int j = 0; j < gray.cols(); j++) {
                double []grayArr = gray.get(i, j);
                double []threshArr = thresh.get(i, j);
                double []arr = new double[gray.get(i, j).length];
                for(int k = 0; k < 3; k++) {
                    double a = grayArr[k];
                    double b = threshArr[k];
                    double c = Math.min(a + (a * b) / (255 - b + 1), 255);
                    arr[k] = c;
                }
                result.put(i, j, arr);
            }
        }
        return result;
    }
}