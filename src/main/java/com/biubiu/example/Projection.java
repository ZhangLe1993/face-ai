package com.biubiu.example;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.opencv.core.Core.FONT_HERSHEY_SIMPLEX;
import static org.opencv.imgproc.Imgproc.threshold;

/**
 * @author ：张音乐
 * @date ：Created in 2021/10/23 下午1:43
 * @description：水平投影/垂直投影
 * @email: zhangyule1993@sina.com
 * @version:
 */
public class Projection {

    public static void main(String[] args) {
        String filePath = "D:\\upload\\123.png";
        Mat img = Imgcodecs.imread(filePath);
        // 先灰度化下
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGRA2GRAY);
        // 二值化处理 ,两种方式都可以实现二值化操作
        Mat thresh = new Mat();
        threshold(gray, thresh, 0, 255,  Imgproc.THRESH_BINARY | Imgproc.THRESH_OTSU);

        // 垂直投影
        Mat vProjection = new Mat();
        thresh.copyTo(vProjection);

        int [] temp = new int[thresh.cols()];

        // 遍历一列
        for(int j = 0; j < thresh.cols(); j++) {
            // 遍历一行
            for(int i = 0; i < thresh.rows(); i++) {
                // 如果该点为黑点 BGR = (0, 0, 0)
                // 该列的计数器加1计数
                if(vProjection.get(i, j)[0] == 0 && vProjection.get(i, j)[1] == 0 && vProjection.get(i, j)[2] == 0) {
                    // 该列的计数器加1计数
                    temp[j] += 1;
                    // 将其变为白色
                    vProjection.put(i, j, new double[] {255, 255, 255});
                }
            }
        }

        // 遍历一列
        for(int j = 0; j < thresh.cols(); j++) {
            // 从该列应该变黑的最顶部的点开始向最底部涂黑
            for (int i = 0; i < thresh.rows() - temp[j]; i++) {
                // 涂黑
                vProjection.put(i, j, new double[] {0, 0, 0});

            }
        }

        Imgproc.putText(vProjection, "verticality", new Point(50, 50), FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(100,100,100), 4);


        // 水平投影
        Mat hProjection = new Mat();
        thresh.copyTo(hProjection);
        int [] temp2 = new int[thresh.rows()];

        // 遍历一行
        for(int j = 0; j < thresh.rows(); j++) {
            // 遍历一列
            for(int i = 0; i < thresh.cols(); i++) {
                // 如果该点为黑点 BGR = (0, 0, 0)
                // 该列的计数器加1计数
                if(hProjection.get(j, i)[0] == 0 && hProjection.get(j, i)[1] == 0 && hProjection.get(j, i)[2] == 0) {
                    // 该列的计数器加1计数
                    temp2[j] += 1;
                    // 将其变为白色
                    hProjection.put(j, i, new double[] {255, 255, 255});
                }
            }
        }

        // 遍历一行
        for(int j = 0; j < thresh.rows(); j++) {
            for (int i = 0; i < temp2[j]; i++) {
                // 涂黑
                hProjection.put(i, j, new double[] {0, 0, 0});

            }
        }
        Imgproc.putText(hProjection, "horizontal", new Point(50, 50), FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(100,100,100), 4);

        // 显示原图
        HighGui.imshow("Image", img);

        // 显示垂直投影
        HighGui.imshow("verticalityImage", vProjection);

        // 显示水平投影
        HighGui.imshow("horizontalImage", hProjection);

        HighGui.waitKey(0);
        // 释放所有的窗体资源
        HighGui.destroyAllWindows();

    }
}