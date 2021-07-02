package com.biubiu.example;

import org.opencv.core.Point;
import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * @author ：张音乐
 * @date ：Created in 2021/4/17 上午8:01
 * @description：图片人脸性别识别
 * @email: zhangyule1993@sina.com
 * @version: 1.0
 */
public class ImageGenderDetect {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * 性别识别模型
     */
    private final static String genderProto = "D:/workspace/opencv/data/models/gender_deploy.prototxt";
    private final static String genderModel = "D:/workspace/opencv/data/models/gender_net.caffemodel";

    /**
     * 性别预测返回的是一个二分类结果 Male, Female
     */
    private final static List<String> genderList = new ArrayList<>(Arrays.asList("男", "女"));

    /**
     * 模型均值
     */
    private final static Scalar MODEL_MEAN_VALUES = new Scalar(78.4263377603, 87.7689143744, 114.895847746);

    public static void main(String[] args) {
        // 加载网络模型
        Net genderNet = Dnn.readNetFromCaffe(genderProto, genderModel);
        if (genderNet.empty()) {
            System.out.println("无法打开网络模型...\n");
            return;
        }
        // 加载图片矩阵
        String filePath = "D:\\upload\\gather.png";
        Mat img = Imgcodecs.imread(filePath);
        // 人脸检测
        MatOfRect faceRects = facePick(img);
        // 定义一个颜色
        Scalar color = new Scalar(0, 0, 255);
        // 遍历检测到的图片
        for(Rect rect : faceRects.toArray()) {
            // 人脸画矩形框
            drawRect(rect, img, color);
            // 检测性别
            String gender = getGender(img, rect, genderNet);
            // 图片上显示中文的性别 ，因为原生的 opencv putText 显示中文会乱码， 所以需要特殊处理一下
            img = putChineseTxt(img, gender, rect.x + rect.width / 2 - 5, rect.y - 10);
            // Imgproc.putText(img, new String(gender.getBytes(StandardCharsets.UTF_8)), new Point(x, y), 2, 2, color);
        }
        // 显示图像
        HighGui.imshow("预览", img);
        HighGui.waitKey(0);
        // 释放所有的窗体资源
        HighGui.destroyAllWindows();
    }

    /**
     * 在图片上的人脸区域画上矩形框
     * @param rect
     * @param img
     * @param color
     */
    private static void drawRect(Rect rect, Mat img, Scalar color) {
        int x = rect.x;
        int y = rect.y;
        int w = rect.width;
        int h = rect.height;
        Imgproc.rectangle(img, new Point(x, y), new Point(x + h, y + w), color, 2);
    }

    /**
     * 性别检测
     * @param img
     * @param rect
     * @param genderNet
     * @return
     */
    private static String getGender(Mat img, Rect rect, Net genderNet) {
        Mat face = new Mat(img, rect);
        // Resizing pictures to resolution of Caffe model
        Imgproc.resize(face, face, new Size(140, 140));
        // 灰度化
        Imgproc.cvtColor(face, face, Imgproc.COLOR_RGBA2BGR);
        // blob输入网络进行性别的检测
        Mat inputBlob = Dnn.blobFromImage(face, 1.0f, new Size(227, 227), MODEL_MEAN_VALUES, false, false);
        genderNet.setInput(inputBlob, "data");
        // 性别检测进行前向传播
        Mat probs = genderNet.forward("prob").reshape(1, 1);
        Core.MinMaxLocResult mm = Core.minMaxLoc(probs);
        // Result of gender recognition prediction. 1 = FEMALE, 0 = MALE
        double index = mm.maxLoc.x;
        return  genderList.get((int) index);
    }

    /**
     * 图片人脸检测
     * @return
     */
    private static MatOfRect facePick(Mat img) {
        // 存放灰度图
        Mat tempImg = new Mat();
        // 摄像头获取的是彩色图像，所以先灰度化下
        Imgproc.cvtColor(img, tempImg, Imgproc.COLOR_BGRA2GRAY);
        // OpenCV人脸识别分类器
        CascadeClassifier classifier = new CascadeClassifier("D:\\workspace\\opencv\\data\\haarcascades\\haarcascade_frontalface_default.xml");
        // # 调用识别人脸
        MatOfRect faceRects = new MatOfRect();
        // 特征检测点的最小尺寸, 根据实际照片尺寸来选择， 不然测量结果可能不准确。
        Size minSize = new Size(140, 140);
        // 图像缩放比例，可理解为相机的X倍镜
        double scaleFactor = 1.2;
        // 对特征检测点周边多少有效点同时检测，这样可避免因选取的特征检测点太小而导致遗漏
        int minNeighbors = 3;
        // 人脸检测
        // CV_HAAR_DO_CANNY_PRUNING
        classifier.detectMultiScale(tempImg, faceRects, scaleFactor, minNeighbors, 0, minSize);
        return faceRects;
    }

    /**
     * Mat二维矩阵转Image
     * @param matrix
     * @param fileExtension
     * @return
     */
    public static BufferedImage matToImg(Mat matrix, String fileExtension) {
        // convert the matrix into a matrix of bytes appropriate for
        // this file extension
        MatOfByte mob = new MatOfByte();
        Imgcodecs.imencode(fileExtension, matrix, mob);
        // convert the "matrix of bytes" into a byte array
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufImage;
    }

    /**
     * BufferedImage转换成 Mat
     * @param original
     * @param imgType
     * @param matType
     * @return
     */
    public static Mat imgToMat(BufferedImage original, int imgType, int matType) {
        if (original == null) {
            throw new IllegalArgumentException("original == null");
        }
        if (original.getType() != imgType){
            // Create a buffered image
            BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), imgType);
            // Draw the image onto the new buffer
            Graphics2D g = image.createGraphics();
            try {
                g.setComposite(AlphaComposite.Src);
                g.drawImage(original, 0, 0, null);
            } finally {
                g.dispose();
            }
        }
        byte[] pixels = ((DataBufferByte) original.getRaster().getDataBuffer()).getData();
        Mat mat = Mat.eye(original.getHeight(), original.getWidth(), matType);
        mat.put(0, 0, pixels);
        return mat;
    }

    /**
     * 在图片上显示中文
     * @param img
     * @param gender
     * @param x
     * @param y
     * @return
     */
    private static Mat putChineseTxt(Mat img, String gender, int x, int y) {
        Font font = new Font("微软雅黑", Font.PLAIN, 20);
        BufferedImage bufImg = matToImg(img,".png");
        Graphics2D g = bufImg.createGraphics();
        g.drawImage(bufImg, 0, 0, bufImg.getWidth(), bufImg.getHeight(), null);
        // 设置字体
        g.setColor(new Color(255, 10, 52));
        g.setFont(font);
        // 设置水印的坐标
        g.drawString(gender, x, y);
        g.dispose();
        // 加完水印再转换回来
        return imgToMat(bufImg, BufferedImage.TYPE_3BYTE_BGR, CvType.CV_8UC3);
    }
}