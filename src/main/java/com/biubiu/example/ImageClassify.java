package com.biubiu.example;

import org.apache.commons.lang3.StringUtils;
import org.opencv.core.*;
import org.opencv.dnn.Dnn;
import org.opencv.dnn.Net;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.yaml.snakeyaml.reader.UnicodeReader;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ：张音乐
 * @date ：Created in 2021/4/26 下午3:15
 * @description：图片分类
 * @email: zhangyule1993@sina.com
 * @version: 1.0
 */
public class ImageClassify {
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    /**
     * 图片类别识别模型
     */
    private final static String typeProto = "D:/workspace/opencv/data/models/bvlc_googlenet.prototxt";
    private final static String typeModel = "D:/workspace/opencv/data/models/bvlc_googlenet.caffemodel";
    private final static String labelText = "D:/workspace/opencv/data/models/synset_words.txt";

    /**
     * 模型均值
     */
    private final static Scalar MODEL_MEAN_VALUES = new Scalar(104, 117, 123);

    public static void main(String[] args) {
        // 加载网络模型
        Net typeNet = Dnn.readNetFromCaffe(typeProto, typeModel);
        if (typeNet.empty()) {
            System.out.println("无法打开网络模型...\n");
            return;
        }
        // 加载图片矩阵
        String filePath = "D:\\upload\\flower1.png";
        Mat img = Imgcodecs.imread(filePath);
        // 检测类别
        String typeName = getImageType(img, typeNet);
        // 图片上显示中文 ，因为原生的 opencv putText 显示中文会乱码， 所以需要特殊处理一下
        img = putChineseTxt(img, typeName, 40, 40);
        // Imgproc.putText(img, new String(gender.getBytes(StandardCharsets.UTF_8)), new Point(x, y), 2, 2, color);
        // 显示图像
        HighGui.imshow("预览", img);
        HighGui.waitKey(0);
        // 释放所有的窗体资源
        HighGui.destroyAllWindows();
    }

    /**
     * 预测图片类别
     * @param img
     * @param typeNet
     * @return
     */
    private static String getImageType(Mat img, Net typeNet) {
        // blob输入网络进行检测
        Mat inputBlob = Dnn.blobFromImage(img, 1.0f, new Size(224, 224), MODEL_MEAN_VALUES, false, false);
        typeNet.setInput(inputBlob, "data");
        // Mat out = typeNet.forward("prob");
        // out.rowRange(0, 0).colRange(0, 0);
        // Out[0,0,:,:] 取第一维度的 第一列， 取第二维度的第一列, 获取第三维度的所有列， 获取第四维度的所有列。
        // 检测进行前向传播, 将输出数据转化到 1通道 1行 Mat矩阵
        Mat probs = typeNet.forward("prob").reshape(1, 1);
        Core.MinMaxLocResult mm = Core.minMaxLoc(probs);
        // Result of gender recognition prediction.
        double index = mm.maxLoc.x;
        List<String> labels = readFromTxtLine(labelText);
        return labels.get((int) index);
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


    public static List<String> readFromTxtLine(String fileName) {
        List<String> list = new ArrayList<>();

        BufferedReader reader = null;
        try{
            FileInputStream fileInputStream = new FileInputStream(new File(fileName));
            UnicodeReader unicodeReader = new UnicodeReader(fileInputStream);
            reader = new BufferedReader(unicodeReader);
            String tempString = null;
            int line = 1;
            while ((tempString = reader.readLine()) != null) {
                // System.out.println("line " + line + ": " + tempString.trim());
                list.add(StringUtils.substringAfter(tempString.trim(), ","));
                line++;
            }
            reader.close();
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (reader != null){
                try{
                    reader.close();
                }catch (IOException e1){
                    e1.printStackTrace();
                }
            }
        }
        return list;
    }
}