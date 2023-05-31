package com.biubiu.example;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;
import static org.bytedeco.javacpp.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.bilateralFilter;
import static org.bytedeco.javacpp.opencv_imgproc.blur;


/**
 * @description: 磨皮美颜
 * @author: 张音乐
 * @date: Created in 2023/5/29 23:13
 * @email: zhangyule1993@sina.com
 * @version: 1.0
 */
public class ChangeFaceColor {

    public static void main(String[] args) {
        Mat img = imread("C:\\Users\\zhang\\test\\R-C-W.jpg", IMREAD_COLOR);
        Mat origin = img.clone();
        Mat dst = changeFaceColor(img);
        imshow("origin", origin);
        imshow("result", dst);
        waitKey(0);
    }

    public static Mat changeFaceColor(Mat img) {
        // 复制原彩图像
        Mat clone = img.clone();
        // 分离三通道
        MatVector vector = new MatVector();
        // vector[0] 是 B 通道的Mat矩阵
        // vector[1] 是 G 通道的Mat矩阵
        // vector[2] 是 R 通道的Mat矩阵
        split(img, vector);

        Mat imgGaussian = new Mat();
        Mat matB = vector.get(0);
        // 对蓝色通道进行均值滤波
        blur(matB, imgGaussian, new Size(8, 8));

        // 对蓝色通道高反差保留
        Mat imgB = add(subtract(matB, imgGaussian), new Scalar(127)).asMat();

        // 对高反差后的蓝色通道做三次强光
        for (int i = 0; i < 3; i++) {
            for (int y = 0; y < img.rows(); y++) {
                for (int x = 0; x < img.cols(); x++) {
                    int value = imgB.ptr(y, x).get();
                    value = value < 0 ? (value + 256) : value;
                    if (value > 127.5) {
                        value = (int) (value + (255 - value) * (value - 127.5) / 127.5);
                    } else {
                        value = (int) (value * value / 127.5);
                    }
                    imgB.ptr(y, x).put((byte) Math.min(Math.max(0, value), 255));
                }
            }
        }

        for (int y = 0; y < img.rows(); y++) {
            for (int x = 0; x < img.cols(); x++) {
                int t = imgB.ptr(y, x).get();
                t = t < 0 ? (t + 256) : t;
                if (t < 220) {
                    BytePointer ptr = img.ptr(y, x);
                    int b = ptr.get(0) < 0 ? (ptr.get(0) + 256) : ptr.get(0);
                    int g = ptr.get(1) < 0 ? (ptr.get(1) + 256) : ptr.get(1);
                    int r = ptr.get(2) < 0 ? (ptr.get(2) + 256) : ptr.get(2);

                    // 把暗色部分用曲线函数调亮
                    int B = cal(b);
                    int G = cal(g);
                    int R = cal(r);
                    ptr.put((byte) Math.min(Math.max(0, B), 255), (byte) Math.min(Math.max(0, G), 255), (byte) Math.min(Math.max(0, R), 255));
                }
            }
        }

        // 对原图进行双边滤波
        Mat out = new Mat();
        bilateralFilter(clone, out, 40, 25 * 2, 25 / 2.0);

        // 对红色通道高反差保留
        Mat matR = vector.get(2);
        blur(matR, imgGaussian, new Size(1, 1));

        Mat imgR = add(subtract(matR, imgGaussian), new Scalar(127)).asMat();

        for (int y = 0; y < img.rows(); y++) {
            for (int x = 0; x < img.cols(); x++) {
                int value = imgR.ptr(y, x).get();
                value = value < 0 ? (value + 256) : value;

                BytePointer ptr = img.ptr(y, x);
                int b = ptr.get(0) < 0 ? (ptr.get(0) + 256) : ptr.get(0);
                int g = ptr.get(1) < 0 ? (ptr.get(1) + 256) : ptr.get(1);
                int r = ptr.get(2) < 0 ? (ptr.get(2) + 256) : ptr.get(2);

                // 线性光混合
                int B = b + 2 * value - 255;
                int G = g + 2 * value - 255;
                int R = r + 2 * value - 255;

                ptr.put((byte) Math.min(Math.max(0, B), 255), (byte) Math.min(Math.max(0, G), 255), (byte) Math.min(Math.max(0, R), 255));
            }
        }

        // 双边滤波图透明度为0.65
        return add(multiply(out, 0.65), multiply(img, 0.35)).asMat();
    }

    /**
     * 曲线蒙版的曲线函数
     *
     * @param x
     * @return
     */
    public static int cal(int x) {
        if (x < 127) {
            return (int) (1.0 * 159 / 127 * x);
        } else {
            return (int) (1.0 * 96 / 128 * (x - 127) + 159);
        }
    }
}
