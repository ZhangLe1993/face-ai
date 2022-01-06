package com.biubiu.example;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javax.swing.*;

import static org.bytedeco.javacpp.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

/**
 * @author ：张音乐
 * @date ：Created in 2021/11/20 下午3:29
 * @description：调整色温
 * @email: zhangyule1993@sina.com
 * @version:
 */
public class ImageColorTemperature {

    public static void main(String[] args) {
        String filepath = "/home/yinyue/opencv/colorOrigin.jpg";
        Mat img = imread(filepath, IMREAD_COLOR);
        if(img.empty()) {
            System.out.println("cannot open file");
            return;
        }

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();
        // 显示原图
        /*Mat origin = img.clone();
        CanvasFrame originCanvas = new CanvasFrame("原始图", 1);
        originCanvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Frame originFrame = converter.convert(origin);
        originCanvas.showImage(originFrame);*/

        Mat target = doColorTemperature(img, -50);
        // 显示
        Frame targetFrame = converter.convert(target);
        CanvasFrame targetCanvas = new CanvasFrame("色温处理后", 1);
        targetCanvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        targetCanvas.showImage(targetFrame);
    }

    private static Mat doColorTemperature(Mat img, int percent) {

        Mat result = img.clone();
        int rows = result.rows();
        int cols = result.cols();
        int level = percent / 2;

        for(int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                BytePointer ptr = result.ptr(i, j);
                int b = ptr.get(0) < 0 ? (ptr.get(0) + 256) : ptr.get(0);
                int g = ptr.get(1) < 0 ? (ptr.get(1) + 256) : ptr.get(1);
                int r = ptr.get(2) < 0 ? (ptr.get(2) + 256) : ptr.get(2);
                // 一般情况下，认为暖色偏黄色，冷色偏蓝色，基于此逻辑，在提高色温的时候，对红色和绿色通道进行增强，对蓝色通道进行减弱，这样就能让图像的黄色占比提高，进而达到暖黄色的效果；
                // 反之亦然，降低色温，只需要增强蓝色通道，减少红色和绿色。
                int B = b - level;
                int G = g + level;
                int R = r + level;

                ptr.put((byte) Math.max(0, Math.min(B, 255)), (byte) Math.max(0, Math.min(G, 255)), (byte) Math.max(0, Math.min(R, 255)));

            }
        }
        return result;
    }


}