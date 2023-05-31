package com.biubiu.example;


import org.bytedeco.javacpp.opencv_core.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.bytedeco.javacpp.opencv_core.CV_8UC3;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;
import static org.bytedeco.javacpp.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

/**
 * @description: 暗通道去雾算法
 * @author: 张音乐
 * @date: Created in 2023/5/20 9:31
 * @email: zhangyule1993@sina.com
 * @version: 1.0
 */
public class Defog {
    private static int rows, cols;

    public static void main(String[] args) {
        Mat img = imread("C:\\Users\\zhang\\test\\20230520110621.png", IMREAD_COLOR);
        rows = img.rows();
        cols = img.cols();
        Mat dst = getRecoverScene(img, 0.88f, 15, 0.1f);
        imshow("origin", img);
        imshow("result", dst);
        waitKey(0);
    }


    /**
     * 获取最小值矩阵
     * @param img
     * @return
     */
    public static int  [][] getMinChannel(Mat img){
        rows = img.rows();
        cols = img.cols();
        if(img.channels() != 3){
            throw new RuntimeException("Input Error!");
        }
        int [][] imgGray;
        imgGray = new int [rows][cols];
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                int initMin = 255;
                for(int k = 0; k < 3; k++){
                    int value = img.ptr(i, j).get(k);
                    value = value < 0 ? (value + 256) : value;
                    if(value < initMin){
                        initMin = value;
                    }
                }
                imgGray[i][j] = initMin;
            }
        }
        return imgGray;
    }


    /**
     * 求暗通道
     * @param img
     * @param blockSize
     * @return
     */
    public static int [][] getDarkChannel(int [][] img, int blockSize){
        if(blockSize%2 == 0 || blockSize < 3){
            throw new RuntimeException("blockSize is not odd or too small!");
        }
        //计算pool Size
        int poolSize = (blockSize - 1) / 2;
        int newHeight = rows + blockSize - 1;
        int newWidth = cols + blockSize - 1;
        int [][]imgMiddle = new int [newHeight][newWidth];

        for(int i = 0; i < newHeight; i++){
            for(int j = 0; j < newWidth; j++){
                if(i < rows && j < cols){
                    imgMiddle[i][j] = img[i][j];
                }else{
                    imgMiddle[i][j] = 255;
                }
            }
        }
        int [][]imgDark = new int [rows][cols];

        int localMin = 255;
        for(int i = poolSize; i < newHeight - poolSize; i++){
            for(int j = poolSize; j < newWidth - poolSize; j++){
                localMin = 255;
                for(int k = i-poolSize; k < i+poolSize+1; k++){
                    for(int l = j-poolSize; l < j+poolSize+1; l++){
                        if(imgMiddle[k][l] < localMin){
                            localMin = imgMiddle[k][l];
                        }
                    }
                }
                imgDark[i-poolSize][j-poolSize] = localMin;
            }
        }
        return imgDark;
    }

    static class Node implements Comparable<Node> {
        int x, y, val;
        public Node(){}
        public Node(int _x, int _y, int _val) {
            this.x = _x;
            this.y = _y;
            this.val = _val;
        }

        @Override
        public int compareTo(Node o) {
            return val - o.val;
        }
    };

    /**
     * 估算全局大气光值
     * @param darkChannel
     * @param img
     * @param percent
     * @return
     */
    public static int getGlobalAtmosphericLightValue(int [][]darkChannel, Mat img, float percent){
        int size = rows * cols;
        List<Node> nodes = new ArrayList<>();
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < cols; j++){
                Node tmp = new Node(i, j, darkChannel[i][j]);
                nodes.add(tmp);
            }
        }
        Collections.sort(nodes);
        int atmosphericLight = 0;
        float v = percent * size;
        boolean b = v == 0;
        if(b) {
            for(int i = 0; i < 3; i++){
                int value = img.ptr(nodes.get(0).x, nodes.get(0).y).get(i);
                value = value < 0 ? (value + 256) : value;
                if (value > atmosphericLight) {
                    atmosphericLight = value;
                }
            }
        }
        //获取暗通道在前0.1%的位置的像素点在原图像中的最高亮度值
        for(int i = 0; i < (int) v; i++){
            for(int j = 0; j < 3; j++){
                int value = img.ptr(nodes.get(i).x, nodes.get(i).y).get(j);
                value = value < 0 ? (value + 256) : value;
                if(value > atmosphericLight){
                    atmosphericLight = value;
                }
            }
        }
        return atmosphericLight;
    }

    /**
     * 恢复原图像
     * @param img
     * @param omega 去雾比例 参数
     * @param blockSize
     * @param percent
     * @return
     */
    public static Mat getRecoverScene(Mat img, float omega, int blockSize, float percent){
        int[][] imgGray = getMinChannel(img);
        int [][] imgDark = getDarkChannel(imgGray, blockSize);
        int atmosphericLight = getGlobalAtmosphericLightValue(imgDark, img, percent);
        float [][]imgDark2;
        float [][]transmission;
        imgDark2 = new float [rows][cols];
        transmission = new float [rows][cols];
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                int i1 = imgDark[i][j];
                imgDark2[i][j] = i1;
                transmission[i][j] = 1 - omega * imgDark[i][j] / atmosphericLight;
                if(transmission[i][j] < 0.1){
                    transmission[i][j] = 0.1f;
                }
            }
        }
        Mat dst = new Mat(img.rows(), img.cols(), CV_8UC3);
        for(int channel = 0; channel < 3; channel++){
            for(int i = 0; i < rows; i++){
                for(int j = 0; j < cols; j++){
                    int value = img.ptr(i, j).get(channel);
                    value = value < 0 ? (value + 256) : value;
                    int temp = (int) ((value - atmosphericLight) / transmission[i][j] + atmosphericLight);
                    if(temp > 255){
                        temp = 255;
                    }
                    if(temp < 0){
                        temp = 0;
                    }
                    dst.ptr(i, j).position(channel).put((byte) temp);
                }
            }
        }
        return dst;
    }


}
