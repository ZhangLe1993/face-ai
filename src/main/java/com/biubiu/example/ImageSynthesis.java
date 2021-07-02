package com.biubiu.example;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
/**
 * @author ：张音乐
 * @date ：Created in 2021/4/26 下午3:07
 * @description：图片合成
 * @email: zhangyule1993@sina.com
 * @version: 1.0
 */
public class ImageSynthesis {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // 加载图片矩阵
        String filePath = "D:\\upload\\background.jpg";
        Mat background = Imgcodecs.imread(filePath);

        // Mat resized = new Mat(background.height(), background.height(), background.type());
        // Imgproc.resize(background, resized, resized.size(), 0.7, 0.7, Imgproc.INTER_CUBIC);
        // HighGui.imshow("background", resized);

        String imgPath = "D:\\upload\\dog.jpg";
        Mat img = Imgcodecs.imread(imgPath);
        int imgRows = img.rows();
        int imgCols = img.cols();
        // HighGui.imshow("img", imgResized);
        //
        // Mat imgResized = new Mat(img.height(), img.height(), img.type());
        // Imgproc.resize(img, imgResized, imgResized.size(), 0.4, 0.4, Imgproc.INTER_CUBIC);

        // 灰度
        // Mat gray = new Mat();
        // Imgproc.cvtColor(imgResized, gray, Imgproc.COLOR_BGRA2GRAY);

        // 高斯模糊
        // Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0);

        // 二值化
        // Mat thresh = new Mat();
        // threshold(gray, thresh, 0, 255,  Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        // Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        // 膨胀操作
        // Mat hiMask = new Mat();
        // Imgproc.dilate(thresh, hiMask, kernel);

        for(int i = 0; i < imgRows; i++) {
            for(int j = 0; j < imgCols; j++) {
                background.put(200 + i,400 + j, img.get(i, j));
            }
        }
        HighGui.imshow("结果", background);
        HighGui.waitKey(0);
        // 释放所有的窗体资源
        HighGui.destroyAllWindows();
    }
}