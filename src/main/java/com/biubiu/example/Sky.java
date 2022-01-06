package com.biubiu.example;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javax.swing.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

/**
 * @author ：张音乐
 * @date ：Created in 2021/11/20 下午4:17
 * @description：风景- 天空滤镜
 * @email: zhangyule1993@sina.com
 * @version:
 */
public class Sky {

    public static void main(String[] args) {
        String filepath = "/home/yinyue/opencv/20220103145739.jpg";
        Mat img = imread(filepath, IMREAD_COLOR);
        if(img.empty()) {
            System.out.println("cannot open file");
            return;
        }

        OpenCVFrameConverter.ToMat converter = new OpenCVFrameConverter.ToMat();

        // 饱和度调整
        Mat sat = doSaturate(img, 60);
        // 明度调整
        Mat lig = doLightness(sat, 25);
        // 对比度调整
        Mat con = doContrast(lig, 60);
        // 阴影调整
        Mat sha = doShadow(con, 20);
        // 高光调整
        Mat hig = doHighLight(sha, 25);
        // 色温调整
        Mat target = doHighLight(hig, -30);
        // 显示
        Frame targetFrame = converter.convert(target);
        CanvasFrame targetCanvas = new CanvasFrame("天空滤镜处理后", 1);
        targetCanvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        targetCanvas.showImage(targetFrame);
    }


    /**
     * 饱和度调整
     * @param img
     * @param percent
     * @return
     */
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


    /**
     * 明度
     * @param img
     * @param percent
     * @return
     */
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


    /**
     * 对比度
     * @param img
     * @param percent
     * @return
     */
    private static Mat doContrast(Mat img, float percent) {
        float alpha = percent / 100.f;
        alpha = Math.max(-1.f, Math.min(1.f, alpha));
        int thresh = 127;
        int height = img.rows();
        int width = img.cols();
        for(int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                BytePointer ptr = img.ptr(i, j);
                int b = ptr.get(0) < 0 ? (ptr.get(0) + 256) : ptr.get(0);
                int g = ptr.get(1) < 0 ? (ptr.get(1) + 256) : ptr.get(1);
                int r = ptr.get(2) < 0 ? (ptr.get(2) + 256) : ptr.get(2);
                if(alpha == 0) {
                    int B = b > thresh ? 255 : 0;
                    int G = g > thresh ? 255 : 0;
                    int R = r > thresh ? 255 : 0;
                    ptr.put((byte) B, (byte) G, (byte) R);
                } else if (alpha >= 0) {
                    int B = (int) (thresh + (b - thresh) / (1 - alpha));
                    int G = (int) (thresh + (g - thresh) / (1 - alpha));
                    int R = (int) (thresh + (r - thresh) / (1 - alpha));
                    ptr.put((byte) Math.max(0, Math.min(B, 255)), (byte) Math.max(0, Math.min(G, 255)), (byte) Math.max(0, Math.min(R, 255)));
                } else {
                    int B = (int) (thresh + (b - thresh) * (1 + alpha));
                    int G = (int) (thresh + (g - thresh) * (1 + alpha));
                    int R = (int) (thresh + (r - thresh) * (1 + alpha));
                    ptr.put((byte) Math.max(0, Math.min(B, 255)), (byte) Math.max(0, Math.min(G, 255)), (byte) Math.max(0, Math.min(R, 255)));
                }
            }
        }
        return img;
    }

    /**
     * 阴影
     * @param img
     * @param light
     * @return
     */
    private static Mat doShadow(Mat img, int light) {
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
        // MatExpr sb1 = subtract();
        MatExpr chat = subtract(new Scalar(1.0f, 1.0f, 1.0f, 0), gray);

        thresh = chat.mul(chat).asMat();
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


    /**
     * 高光
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

    /**
     * 色温
     * @param img
     * @param percent
     * @return
     */
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