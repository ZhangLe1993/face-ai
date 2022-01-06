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
 * @date ：Created in 2021/11/10 下午8:33
 * @description：图像饱和度
 * @email: zhangyule1993@sina.com
 * @version:
 */
public class ImageSaturate {

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


        Mat target = doSaturate(img, 50);
        Frame targetFrame = converter.convert(target);
        CanvasFrame targetCanvas = new CanvasFrame("处理后", 1);
        targetCanvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        targetCanvas.showImage(targetFrame);
    }

    private static Mat doSaturate(Mat img, int percent) {
        float increment = percent * 1.0f / 100;
        int height = img.rows();
        int width = img.cols();
        for(int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                BytePointer ptr = img.ptr(i, j);
                int b = ptr.get(0) < 0 ? (ptr.get(0) + 256) : ptr.get(0);
                int g = ptr.get(1) < 0 ? (ptr.get(1) + 256) : ptr.get(1);
                int r = ptr.get(2) < 0 ? (ptr.get(2) + 256) : ptr.get(2);

                float max = Math.max(b, Math.max(g, r));
                float min = Math.min(b, Math.min(g, r));

                float delta = (max - min) / 255;

                if(delta == 0) {
                    continue;
                }
                float value = (max + min) / 255;
                float L = value / 2;
                float S = 0f, alpha;

                if(L < 0.5) {
                    S = delta / value;
                }
                if(increment >= 0) {
                    if((increment + S) >= 1) {
                        alpha = S;
                    } else {
                        alpha = 1 - increment;
                    }
                    alpha = 1 / alpha - 1;
                    int B = (int) (b + (b - L * 255) * alpha);
                    int G = (int) (g + (g - L * 255) * alpha);
                    int R = (int) (r + (r - L * 255) * alpha);
                    ptr.put((byte) Math.max(0, Math.min(B, 255)), (byte) Math.max(0, Math.min(G, 255)), (byte) Math.max(0, Math.min(R, 255)));
                } else {
                    alpha = increment;
                    int B = (int) (b + (b - L * 255) * alpha);
                    int G = (int) (g + (g - L * 255) * alpha);
                    int R = (int) (r + (r - L * 255) * alpha);
                    ptr.put((byte) Math.max(0, Math.min(B, 255)), (byte) Math.max(0, Math.min(G, 255)), (byte) Math.max(0, Math.min(R, 255)));
                }
            }
        }
        return img;
    }

}