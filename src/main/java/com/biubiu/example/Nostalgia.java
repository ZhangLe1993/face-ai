package com.biubiu.example;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * @author ：张音乐
 * @date ：Created in 2021/10/23 下午2:41
 * @description：怀旧滤镜
 * @email: zhangyule1993@sina.com
 * @version:
 */
public class Nostalgia {
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
        Mat result = toNostalgia(img);
        HighGui.imshow("怀旧图", result);
        HighGui.waitKey(0);
        // 释放所有的窗体资源
        HighGui.destroyAllWindows();
    }

    private static Mat toNostalgia(Mat img) {
        Mat result = new Mat(img.size(), CvType.CV_8UC3);
        for(int i = 0; i < img.rows(); i++) {
            for (int j = 0; j < img.cols(); j++) {
                double b = img.get(i, j)[0];
                double g = img.get(i, j)[1];
                double r = img.get(i, j)[2];

                double bb = 0.272 * r + 0.534 * g + 0.131 * b;
                double gg = 0.349 * r + 0.686 * g + 0.168 * b;
                double rr = 0.393 * r + 0.769 * g + 0.189 * b;

                double []arr = new double[] {bb, gg, rr};
                result.put(i, j, arr);
            }
        }
        return result;
    }
}