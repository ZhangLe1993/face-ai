package com.biubiu.example;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core.*;

import static org.bytedeco.javacpp.opencv_core.CV_8UC1;
import static org.bytedeco.javacpp.opencv_core.CV_8UC3;
import static org.bytedeco.javacpp.opencv_highgui.imshow;
import static org.bytedeco.javacpp.opencv_highgui.waitKey;
import static org.bytedeco.javacpp.opencv_imgcodecs.IMREAD_COLOR;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgproc.medianBlur;


/**
 * @description: 中值滤波去雾
 * @author: 张音乐
 * @date: Created in 2023/5/27 9:22
 * @email: zhangyule1993@sina.com
 * @version: 1.0
 */
public class MedianFilterDefog {

    private static int rows, cols;

    public static void main(String[] args) {
        Mat img = imread("C:\\Users\\zhang\\test\\20230520110621.png", IMREAD_COLOR);
        rows = img.rows();
        cols = img.cols();
        Mat dst = medianFilterFogRemoval(img, 0.95f, 41, 3);
        imshow("origin", img);
        imshow("result", dst);
        waitKey(0);
    }



    /**
     * 获取最小值矩阵
     * @param img
     * @return
     */
    public static int[][] getMinChannel(Mat img) {
        rows = img.rows();
        cols = img.cols();
        if (img.channels() != 3) {
            throw new RuntimeException("Input Error!");
        }
        int[][] imgGray = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int loacalMin = 255;
                for (int k = 0; k < 3; k++) {
                    int value = img.ptr(i, j).get(k);
                    value = value < 0 ? (value + 256) : value;
                    if (value < loacalMin) {
                        loacalMin = value;
                    }
                }
                imgGray[i][j] = loacalMin;
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
    public static int[][] getDarkChannel(int[][] img, int blockSize) {
        if (blockSize % 2 == 0 || blockSize < 3) {
            throw new RuntimeException("blockSize is not odd or too small!");
        }
        //计算 pool Size
        int poolSize = (blockSize - 1) / 2;
        int newHeight = rows + poolSize - 1;
        int newWidth = cols + poolSize - 1;
        int[][] imgMiddle = new int[newHeight][newWidth];
        for (int i = 0; i < newHeight; i++) {
            for (int j = 0; j < newWidth; j++) {
                if (i < rows && j < cols) {
                    imgMiddle[i][j] = img[i][j];
                } else {
                    imgMiddle[i][j] = 255;
                }
            }
        }
        int[][] imgDark = new int[rows][cols];

        int localMin = 255;
        for (int i = poolSize; i < newHeight - poolSize; i++) {
            for (int j = poolSize; j < newWidth - poolSize; j++) {
                for (int k = i - poolSize; k < i + poolSize + 1; k++) {
                    for (int l = j - poolSize; l < j + poolSize + 1; l++) {
                        if (imgMiddle[k][l] < localMin) {
                            localMin = imgMiddle[k][l];
                        }
                    }
                }
                imgDark[i - poolSize][j - poolSize] = localMin;
            }
        }
        return imgDark;
    }

    /**
     *
     * @param src
     * @param p 去雾浓度的系数，取值为 [0,1]
     * @param kernelSize
     * @param blockSize
     * @return
     */
    public static Mat medianFilterFogRemoval(Mat src, float p, int kernelSize, int blockSize) {
        int row = src.rows();
        int col = src.cols();
        int[][] imgGray = getMinChannel(src);
        int[][] imgDark = getDarkChannel(imgGray, blockSize);
        int[] histgram = new int[256];
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                histgram[imgDark[i][j]]++;
            }
        }
        int Sum = 0, atmosphericLight = 0;
        for (int i = 255; i >= 0; i--) {
            Sum += histgram[i];
            if (Sum > row * col * 0.01) {
                atmosphericLight = i;
                break;
            }
        }
        int sumB = 0, sumG = 0, sumR = 0, amount = 0;
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                if (imgDark[i][j] >= atmosphericLight) {
                    BytePointer bytePointer = src.ptr(i, j);
                    int b = bytePointer.get(0) < 0 ? (bytePointer.get(0) + 256) : bytePointer.get(0);
                    int g = bytePointer.get(1) < 0 ? (bytePointer.get(1) + 256) : bytePointer.get(1);
                    int r = bytePointer.get(2) < 0 ? (bytePointer.get(2) + 256) : bytePointer.get(2);
                    sumB += b;
                    sumG += g;
                    sumR += r;
                    amount++;
                }
            }
        }
        sumB /= amount;
        sumG /= amount;
        sumR /= amount;
        Mat filter = new Mat(row, col, CV_8UC1);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                filter.ptr(i, j).put((byte) imgDark[i][j]);
            }
        }
        Mat aMat = new Mat(row, col, CV_8UC1);
        medianBlur(filter, aMat, kernelSize);
        Mat temp = new Mat(row, col, CV_8UC1);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                BytePointer filterPty = filter.ptr(i, j);
                BytePointer aPty = aMat.ptr(i, j);
                int filterValue = filterPty.get() < 0 ? (filterPty.get() + 256) : filterPty.get();
                int aValue = aPty.get() < 0 ? (aPty.get() + 256) : aPty.get();
                int diff = filterValue - aValue;
                if (diff < 0) diff = -diff;
                temp.ptr(i, j).put((byte) diff);
            }
        }
        medianBlur(temp, temp, kernelSize);
        Mat bMat = new Mat(row, col, CV_8UC1);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                BytePointer tempPty = temp.ptr(i, j);
                BytePointer aPty = aMat.ptr(i, j);
                int tempValue = tempPty.get() < 0 ? (tempPty.get() + 256) : tempPty.get();
                int aValue = aPty.get() < 0 ? (aPty.get() + 256) : aPty.get();
                int diff = aValue - tempValue;
                if (diff < 0) diff = 0;
                bMat.ptr(i, j).put((byte) diff);
            }
        }
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                BytePointer bPty = bMat.ptr(i, j);
                int bValue = bPty.get() < 0 ? (bPty.get() + 256) : bPty.get();
                int min = (int) (bValue * p);
                if (imgDark[i][j] > min) {
                    bPty.put((byte) min);
                } else {
                    bPty.put((byte) imgDark[i][j]);
                }
            }
        }
        Mat dst = new Mat(row, col, CV_8UC3);
        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                BytePointer bPty = bMat.ptr(i, j);
                BytePointer srcPty = src.ptr(i, j);
                int bValue = bPty.get() < 0 ? (bPty.get() + 256) : bPty.get();

                int srcB = srcPty.get(0) < 0 ? (srcPty.get(0) + 256) : srcPty.get(0);
                int srcG = srcPty.get(1) < 0 ? (srcPty.get(1) + 256) : srcPty.get(1);
                int srcR = srcPty.get(2) < 0 ? (srcPty.get(2) + 256) : srcPty.get(2);

                int dstB;
                if (sumB != bValue) {

                    dstB = sumB * (srcB - bValue) / (sumB - bValue);
                } else {
                    dstB = srcB;
                }

                int dstG;
                if (sumG != bValue) {
                    dstG = sumG * (srcG - bValue) / (sumG - bValue);
                } else {
                    dstG = srcG;
                }

                int dstR;
                if (sumR != bValue) {
                    dstR = sumR * (srcR - bValue) / (sumR - bValue);
                } else {
                    dstR = srcR;
                }
                dst.ptr(i, j).put((byte) Math.min(Math.max(0, dstB), 255), (byte) Math.min(Math.max(0, dstG), 255), (byte) Math.min(Math.max(0, dstR), 255));
            }
        }
        return dst;
    }
}
