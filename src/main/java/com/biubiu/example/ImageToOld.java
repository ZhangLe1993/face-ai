package com.biubiu.example;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * @author ：张音乐
 * @date ：Created in 2021/4/26 下午4:05
 * @description：老照片特效
 * @email: zhangyule1993@sina.com
 * @version: 1.0
 */
public class ImageToOld {
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

        int height = img.height();
        int width = img.width();
        for(int i = 0; i < height; i++) {
            for(int j = 0; j < width; j++) {
                double b = img.get(i, j)[0];
                double g = img.get(i, j)[1];
                double r = img.get(i, j)[2];
                // 计算新的图像中的RGB值
                // 约束图像像素值，防止溢出, 变换后的RGB值要约束在0 ~ 255 之间
                int B = (int) (0.273 * r + 0.535 * g + 0.131 * b);
                int G = (int) (0.347 * r + 0.683 * g + 0.167 * b);
                int R = (int) (0.395 * r + 0.763 * g + 0.188 * b);
                // 照片变旧只需对图片的颜色空间进行处理
                img.put(i, j, Math.max(0, Math.min(B, 255)), Math.max(0, Math.min(G, 255)), Math.max(0, Math.min(R, 255)));
            }
        }
        HighGui.imshow("特效图", img);
        HighGui.waitKey(0);
        // 释放所有的窗体资源
        HighGui.destroyAllWindows();
    }

}