package com.biubiu.example;


import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.math.BigDecimal;

import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;
/**
 * @author ：张音乐
 * @date ：Created in 2021/4/15 上午9:47
 * @description：人脸检测
 * @email: zhangyule1993@sina.com
 * @version: 1.0
 */
public class FaceDetect {

    static {
        // 加载 动态链接库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {

        String filepath = "/home/yinyue/opencv/test.JPG";
        Mat srcImg = Imgcodecs.imread(filepath);
        // 目标灰色图像
        Mat dstGrayImg = new Mat();
        // 转换灰色
        Imgproc.cvtColor(srcImg, dstGrayImg, Imgproc.COLOR_BGR2GRAY);
        // OpenCv人脸识别分类器
        CascadeClassifier classifier = new CascadeClassifier("/usr/local/share/OpenCV/haarcascades/haarcascade_frontalface_default.xml");
        // 用来存放人脸矩形
        MatOfRect faceRect = new MatOfRect();
        // 特征检测点的最小尺寸
        Size minSize = new Size(32, 32);
        // 图像缩放比例,可以理解为相机的X倍镜
        double scaleFactor = 1.2;
        // 对特征检测点周边多少有效检测点同时检测,这样可以避免选取的特征检测点大小而导致遗漏
        int minNeighbors = 3;
        // 执行人脸检测
        classifier.detectMultiScale(dstGrayImg, faceRect, scaleFactor, minNeighbors, CV_HAAR_DO_CANNY_PRUNING, minSize);
        //遍历矩形,画到原图上面
        // 定义绘制颜色
        Scalar color = new Scalar(0, 0, 255);
        for(Rect rect: faceRect.toArray()) {
            int x = rect.x;
            int y = rect.y;
            int w = rect.width;
            int h = rect.height;
            // 单独框出每一张人脸
            Imgproc.rectangle(srcImg, new Point(x, y), new Point(x + h, y + w), color, 2);
            // 左眼
            Imgproc.circle(srcImg, new Point(x + Math.floor(getDivideDouble(w, 4)), y + Math.floor(getDivideDouble(h, 4)) + 15), Math.min(getDivideInt(h, 8), getDivideInt(w, 8)), color);
            // 右眼
            Imgproc.circle(srcImg, new Point(x + 3 * Math.floor(getDivideDouble(w, 4)), y + Math.floor(getDivideDouble(h, 4)) + 15), Math.min(getDivideInt(h, 8), getDivideInt(w, 8)), color);
            // 嘴巴
            Imgproc.rectangle(srcImg, new Point(x + 3 * Math.floor(getDivideDouble(w, 8)), y + 3 * Math.floor(getDivideDouble(h, 4)) - 5), new Point(x + 5 * Math.floor(getDivideDouble(w, 8)) + 10, y + 7 * Math.floor(getDivideDouble(h, 8))), color, 2);
        }
        HighGui.imshow("预览", srcImg);
        // 显示图像
        HighGui.waitKey(0) ;
        // 释放所有的窗体资源
        HighGui.destroyAllWindows();

    }


    /**
     * 图片转换成灰色（降低为一维的灰度，减低计算强度）
     * @param path
     * @return
     */
    private static Mat transferToGray(String path) {
        // 读取图片
        Mat srcImg = Imgcodecs.imread(path);
        // 目标灰色图像
        Mat dstGrayImg = new Mat();
        // 转换灰色
        Imgproc.cvtColor(srcImg, dstGrayImg, Imgproc.COLOR_BGR2GRAY);

        return dstGrayImg;
    }

    /**
     * 在图片上画矩形
     * @param path
     */
    private static void drawRect(String path) {
        // 读取图片
        Mat srcImg = Imgcodecs.imread(path);
        // 目标灰色图像
        Mat dstGrayImg = new Mat();
        // 转换灰色
        Imgproc.cvtColor(srcImg, dstGrayImg, Imgproc.COLOR_BGR2GRAY);
        // 坐标
        double x = 10, y = 10;
        //  矩形大小（宽、高）
        double w = 100;
        // 定义绘制颜色
        Scalar color = new Scalar(0, 0, 255);
        Imgproc.rectangle(srcImg, new Point(x, y), new Point(x + w, y + w), color, 1);
        HighGui.imshow("预览", srcImg);
        // 显示图像
        HighGui.waitKey(0);
        // 释放所有的窗体资源
        HighGui.destroyAllWindows();

    }


    /**
     * 计算除法
     * @param a
     * @param b
     * @return
     */
    private static double getDivideDouble(int a, int b) {
        return new BigDecimal(a).divide(new BigDecimal(b), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    /**
     * 计算除法
     * @param a
     * @param b
     * @return
     */
    private static int getDivideInt(int a, int b) {
        return new BigDecimal(a).divide(new BigDecimal(b), 2, BigDecimal.ROUND_HALF_UP).intValue();
    }



}
