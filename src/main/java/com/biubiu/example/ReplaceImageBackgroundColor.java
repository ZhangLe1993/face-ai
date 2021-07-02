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
 * @date ：Created in 2021/4/26 下午3:59
 * @description：更换证件照背景色
 * @email: zhangyule1993@sina.com
 * @version: 1.0
 */
public class ReplaceImageBackgroundColor {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        String path = "D:\\upload\\plxjj.jpg";
        Mat img = Imgcodecs.imread(path);
        if(img.empty()) {
            System.out.println("cannot open file");
            return;
        }
        Mat origin = img.clone();

        HighGui.imshow("img-origin", origin);
        // 缩放
        Mat resize = new Mat(img.height(), img.height(), img.type());
        /**
         *  INTER_NEAREST - 最邻近插值
         *  INTER_LINEAR - 双线性插值，如果最后一个参数你不指定，默认使用这种方法
         *  INTER_AREA -区域插值 resampling using pixel area relation.
         *  It may be a preferred method for image decimation, as it gives moire’-free results. But when the image is zoomed, it is similar to
         *  the INTER_NEAREST method.
         *  INTER_CUBIC - 4x4像素邻域内的双立方插值
         *  INTER_LANCZOS4 - 8x8像素邻域内的Lanczos插值
         */
        Imgproc.resize(img, resize, img.size(), 0.5, 0.5);
        // 转换为hsv图像
        Mat hsv = new Mat();
        Imgproc.cvtColor(img, hsv, Imgproc.COLOR_BGR2HSV);

        Scalar lowerb = new Scalar(new double[] { 90, 70, 70 });
        Scalar upperb = new Scalar(new double[] { 110, 255, 255 });

        // 二值化
        Mat mask = new Mat();
        Core.inRange(hsv, lowerb, upperb, mask);
        // 腐蚀膨胀
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat erode = new Mat();
        Imgproc.erode(mask, erode, kernel);
        // 膨胀操作
        Mat dilate = new Mat();
        Imgproc.dilate(erode, dilate, kernel);
        for(int i = 0; i < img.rows(); i++) {
            for(int j = 0; j < img.cols(); j++) {
                // 此处替换颜色，为BGR通道。
                // double [] arr = dilate.get(i, j);
                if(dilate.get(i, j)[0] == 255) {
                    img.put(i, j, new double[] {0, 0, 255});
                }
            }
        }

        HighGui.imshow("img", img);
        HighGui.waitKey(0);
        // 释放所有的窗体资源
        HighGui.destroyAllWindows();
    }

}