package com.biubiu.example;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * @author ：张音乐
 * @date ：Created in 2021/4/26 下午3:52
 * @description：替换图片的背景图片
 * @email: zhangyule1993@sina.com
 * @version: 1.0
 */
public class ReplaceImageBackgroundImage {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {

        String backPath = "D:\\upload\\back.jpg";
        Mat back = Imgcodecs.imread(backPath);
        Mat resized = new Mat(back.height(), back.height(), back.type());
        Imgproc.resize(back, resized, resized.size(), 0.7, 0.7, Imgproc.INTER_CUBIC);

        String imgPath = "D:\\upload\\green_pic.jpg";
        Mat img = Imgcodecs.imread(imgPath);
        Scalar lowerb = new Scalar(new double[] { 0, 0, 0 });
        // RGB 三个值， R和B接近255， 扣到的图越好， 但是 容易产生边缘。
        Scalar upperb = new Scalar(new double[] { 254, 255, 254 });
        // 转换为hsv图像
        Mat hsv = new Mat();
        Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);
        // 高斯模糊
        Mat thresh = new Mat();
        Imgproc.GaussianBlur(hsv, thresh, new Size(1, 1), 0);
        // HighGui.imshow("thresh", thresh);
        // 二值化
        Mat mask = new Mat();
        Core.inRange(thresh, lowerb, upperb, mask);
        // 形态学开， 膨胀处理
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
        Mat hiMask = new Mat();
        Imgproc.dilate(mask, hiMask, kernel);
        // HighGui.imshow("hiMask", hiMask);
        // 遍历， 抠图
        for(int i = 0; i < img.rows(); i++) {
            for(int j = 0; j < img.cols(); j++) {
                // 去除白点的。
                if(hiMask.get(i, j)[0] != 0) {
                    back.put(i, j, img.get(i, j));
                }
            }
        }
        HighGui.imshow("img", back);
        HighGui.waitKey(0);
        // 释放所有的窗体资源
        HighGui.destroyAllWindows();

    }

}