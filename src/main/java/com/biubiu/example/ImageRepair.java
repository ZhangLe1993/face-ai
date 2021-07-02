package com.biubiu.example;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.photo.Photo;

import static org.opencv.imgproc.Imgproc.threshold;
/**
 * @author ：张音乐
 * @date ：Created in 2021/4/15 上午9:47
 * @description：图像修复
 * @email: zhangyule1993@sina.com
 * @version: 1.0
 */
public class ImageRepair {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {

        String filePath = "D:\\upload\\white.jpg";
        Mat img = Imgcodecs.imread(filePath);
        // 先灰度化下
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGRA2GRAY);
        // 高斯模糊
        Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0);

        int height = img.height();
        int width = img.width();

        // 图片二值化处理，把[240, 240, 240] ~ [255, 255, 255]以外的颜色变成0
        Mat thresh = new Mat();
        // 二值化处理
        // inRange(img, new Scalar(240, 240, 240), new Scalar(255, 255, 255), thresh);

        // 二值化处理 ,两种方式都可以实现二值化操作
        threshold(gray, thresh, 0, 255,  Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);
        // 创建形状和尺寸的结构元素
        Mat hiMask = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));

        // 对Mask膨胀处理, 扩张待修复区域
        Imgproc.dilate(thresh, hiMask, kernel);
        //图像修复
        Mat specular = new Mat();
        Photo.inpaint(img, hiMask, specular, 5, Photo.INPAINT_TELEA);

        // 显示原图
        HighGui.namedWindow("Image", 0);
        HighGui.resizeWindow("Image", width / 2, height / 2);
        HighGui.imshow("Image", img);

        // 显示修复后的图
        HighGui.namedWindow("newImage", 0);
        HighGui.resizeWindow("newImage", width / 2, height / 2);
        HighGui.imshow("newImage", specular);

        HighGui.waitKey(0);
        // 释放所有的窗体资源
        HighGui.destroyAllWindows();
    }
}


