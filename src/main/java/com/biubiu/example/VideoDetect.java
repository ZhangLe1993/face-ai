package com.biubiu.example;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import static org.bytedeco.javacpp.opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING;

/**
 * 视频人脸检测
 */
public class VideoDetect {

    static {
        // 加载 动态链接库
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        VideoCapture camera = new VideoCapture();
        // 参数0表示，获取第一个摄像头。
        camera.open(0);
        // 图像帧
        Mat frame = new Mat();
        for(;;) {
            camera.read(frame);
            draw(frame);
            // 等待用户按esc停止检测
            if(HighGui.waitKey(100) == 100) {
                break;
            }
        }
        // 释放摄像头
        camera.release();
        // 释放窗口资源
        HighGui.destroyAllWindows();
    }

    /**
     * 逐帧处理
     * @param frame
     */
    private static void draw(Mat frame) {
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        // OpenCv人脸识别分类器
        CascadeClassifier classifier = new CascadeClassifier("/usr/local/share/OpenCV/haarcascades/haarcascade_frontalface_default.xml");
        // 用来存放人脸矩形
        MatOfRect faceRect = new MatOfRect();
        // 特征检测点的最小尺寸
        Size minSize = new Size(32, 32);
        // 图像缩放比例,可以理解为相机的X倍镜
        double scaleFactor = 1.2;
        // 对特征检测点周边多少有效检测点同时检测,这样可以避免选取的特征检测点大小而导致遗漏
        int minNeighbors = 3;
        // 执行人脸检测
        classifier.detectMultiScale(grayFrame, faceRect, scaleFactor, minNeighbors, CV_HAAR_DO_CANNY_PRUNING, minSize);
        Scalar color = new Scalar(0, 0, 255);
        for(Rect rect: faceRect.toArray()) {
            int x = rect.x;
            int y = rect.y;
            int w = rect.width;
            int h = rect.height;
            // 框出人脸
            Imgproc.rectangle(frame, new Point(x, y), new Point(x + h, y + w), color, 2);
        }
        HighGui.imshow("预览", frame);
    }
}
