package com.biubiu.example;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core.*;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javax.swing.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

/**
 * @author ：张音乐
 * @date ：Created in 2021/11/14 上午9:00
 * @description：调整图像高光
 * @email: zhangyule1993@sina.com
 * @version:
 */
public class ImageHighLight {
    public static void main(String[] args) {
        String filepath = "/home/yinyue/opencv/113958-153525479855be.jpg";
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

        Mat target = doHighLight(img, -50);
        // 显示
        Frame targetFrame = converter.convert(target);
        CanvasFrame targetCanvas = new CanvasFrame("高光处理后", 1);
        targetCanvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        targetCanvas.showImage(targetFrame);
    }

    /**
     * 增减高光
     * @param img
     * @param light
     * @return
     */
    private static Mat doHighLight(Mat img, int light) {
        // 生成灰度图
        Mat gray = new Mat(img.size(), CV_32FC1);
        Mat fs = img.clone();
        fs.convertTo(fs, CV_32FC3);

        MatVector vector = new MatVector();
        split(fs, vector);
        Mat v2 = vector.get(2);
        Mat v0 = vector.get(0);

        MatExpr x1 = multiply(0.299f, v2);

        MatExpr x2 = multiply(0.587, v2);

        MatExpr x3 = multiply(0.114, v0);

        MatExpr add = add(x1, add(x2, x3));

        MatExpr divide = divide(add, 255);

        gray = divide.asMat();

        // 确定高光区
        Mat thresh = new Mat(gray.size(), gray.type());
        // thresh = multiply(gray, gray).asMat();
        thresh = gray.mul(gray).asMat();
        // 取平均值作为阈值
        Scalar t = mean(thresh);
        Mat mask = new Mat(gray.size(), CV_8UC1);
        // threshold(thresh, mask, t.blue(), 255,  THRESH_BINARY | THRESH_OTSU);
        // mask.setTo(new Mat(1, 1, CV_32SC4, t), new Mat(1, 1, CV_32SC4, new Scalar(255, 255, 255, 0)));
        // 取平均值当阈值，进行二值化得到掩膜mask
        inRange(thresh, new Mat(t), new Mat(new Scalar(255, 255, 255, 0)), mask);

        // 参数设置
        int max = 4;
        float bright = light / 100.0f / max;
        float mid = 1.0f + max * bright;

        // 边缘平滑过渡
        Mat midrate = new Mat(img.size(), CV_32FC1);
        Mat brightrate = new Mat(img.size(), CV_32FC1);

        for (int i = 0; i < img.rows(); ++i) {
            for (int j = 0; j < img.cols(); ++j) {
                BytePointer m = mask.ptr(i, j);
                BytePointer th = thresh.ptr(i, j);
                BytePointer mi = midrate.ptr(i, j);
                BytePointer br = brightrate.ptr(i, j);
                int value = m.get(0) < 0 ? (m.get(0) + 256) : m.get(0);
                float thv = th.getFloat(0);
                if(value == 255) {
                    mi.putFloat(mid);
                    br.putFloat(bright);
                } else {
                    float s = (float) ((mid - 1.0f) / t.blue() * thv + 1.0f);
                    mi.putFloat(s);
                    float p = (float) (1.0f / t.blue() * thv) * bright;
                    br.putFloat(p);
                }
            }
        }
        Mat result = new Mat(img.size(), img.type());
        // 高光提亮，获取结果图
        for (int i = 0; i < img.rows(); ++i) {
            for (int j = 0; j < img.cols(); ++j) {
                BytePointer mi = midrate.ptr(i, j);
                BytePointer br = brightrate.ptr(i, j);

                BytePointer in = img.ptr(i, j);
                BytePointer rPtr = result.ptr(i, j);

                // int v = in.get(0) < 0 ? (in.get(0) + 256) : in.get(0);
                float miv = mi.getFloat(0);
                float brv = br.getFloat(0);

                for(int k = 0; k < 3; k++) {
                    int v = in.get(k) < 0 ? (in.get(k) + 256) : in.get(k);
                    float temp = (float) (Math.pow(v / 255.f, 1.0f / miv) * (1.0 / (1 - brv)));
                    temp = Math.min(1.0f, Math.max(0.0f, temp));
                    rPtr.position(k).put((byte) (255 * temp));
                }
            }
        }

        return result;

    }
}