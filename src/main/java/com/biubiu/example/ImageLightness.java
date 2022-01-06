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
 * @date ：Created in 2021/11/10 下午9:12
 * @description：图像明度
 * @email: zhangyule1993@sina.com
 * @version:
 */
public class ImageLightness {
    public static void main(String[] args) {
        String filepath = "/home/yinyue/opencv/113958-153525479855be.jpg";
        Mat img = imread(filepath, IMREAD_COLOR);
        if(img.empty()) {
            System.out.println("cannot open file");
            return;
        }
        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        // 显示原图
        Mat origin = img.clone();
        CanvasFrame originCanvas = new CanvasFrame("原始图", 1);
        originCanvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Frame originFrame = converter.convert(origin);
        originCanvas.showImage(originFrame);


        Mat target = doLightness(img, 50);
        Frame targetFrame = converter.convert(target);
        CanvasFrame targetCanvas = new CanvasFrame("处理后", 1);
        targetCanvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        targetCanvas.showImage(targetFrame);
    }

    private static Mat doLightness(Mat img, float percent) {
        float alpha = percent / 100;
        alpha = Math.max(-1.f, Math.min(1.f, alpha));
        int height = img.rows();
        int width = img.cols();
        for(int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                BytePointer ptr = img.ptr(i, j);
                int b = ptr.get(0) < 0 ? (ptr.get(0) + 256) : ptr.get(0);
                int g = ptr.get(1) < 0 ? (ptr.get(1) + 256) : ptr.get(1);
                int r = ptr.get(2) < 0 ? (ptr.get(2) + 256) : ptr.get(2);

                if(alpha >= 0) {
                    int B = (int) (b * (1 - alpha) + 255 * alpha);
                    int G = (int) (g * (1 - alpha) + 255 * alpha);
                    int R = (int) (r * (1 - alpha) + 255 * alpha);
                    ptr.put((byte) Math.max(0, Math.min(B, 255)), (byte) Math.max(0, Math.min(G, 255)), (byte) Math.max(0, Math.min(R, 255)));
                } else {
                    int B = (int) (b * (1 + alpha));
                    int G = (int) (g * (1 + alpha));
                    int R = (int) (r * (1 + alpha));
                    ptr.put((byte) Math.max(0, Math.min(B, 255)), (byte) Math.max(0, Math.min(G, 255)), (byte) Math.max(0, Math.min(R, 255)));
                }
            }
        }
        return img;
    }
}