package com.example.vinicius.opencv;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by vinicius on 26/08/17.
 */

public class Leaf {
    private Mat leaf;
    private float[] leafColor;
    private Mat originalImage;
    private double leafArea;
    private double deseaseArea;

    public Leaf(Bitmap image,float[] leafColor){
        this.leaf = new Mat();
        Utils.bitmapToMat(image,this.leaf);
        this.leafColor = leafColor;
        this.leafArea = 0;
        this.deseaseArea =0;
        //.resize(leaf.width()/10,leaf.height()/10);
        originalImage = leaf;
    }


    public double getLeafArea() {
        return leafArea;
    }

    public void setLeafArea(int leafArea) {
        this.leafArea = leafArea;
    }

    public void setLeaf(Mat leaf) {
        this.leaf = leaf;
    }

    public Mat getLeaf(){
        return leaf;
    }

    public Bitmap process(){
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;



        ArrayList<Mat> leafArray= this.splitIn4();

        for(int i = 0 ;i<leafArray.size();i++){
            Mat aux = new Mat();
            aux = leafArray.get(i);
            Imgproc.cvtColor(aux,aux,Imgproc.COLOR_RGB2GRAY);
            Imgproc.threshold(aux, aux, 0, 255, Imgproc.THRESH_OTSU);

            leafArray.set(i,aux);
        }



        leaf = concatSplitedLeaf(leafArray);

        Imgproc.medianBlur(leaf,leaf,3);

        /*
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hieranchy = new Mat();
        Imgproc.findContours(leaf,contours,hieranchy, 1, 2 );
        Imgproc.cvtColor(leaf,leaf,Imgproc.COLOR_GRAY2RGB);
        Iterator<MatOfPoint> each = contours.iterator();
        while (each.hasNext()) {
            MatOfPoint wrapper = each.next();
            double area = Imgproc.contourArea(wrapper);
            System.out.println(area);

        }
        if(contours.size() == 3) {
            Imgproc.drawContours(leaf, contours.subList(0, 1), 0, new Scalar(255, 0, 0), 0);
            Imgproc.drawContours(leaf, contours.subList(1, 2), 0, new Scalar(0, 255, 0), 0);
            Imgproc.drawContours(leaf,contours.subList(2,3), 0, new Scalar(0, 0, 255), 0);

            double areaRef = Imgproc.contourArea(contours.get(0));
            double areaLeaf = Imgproc.contourArea(contours.get(1));
            this.leafArea = areaLeaf/areaRef;
            System.out.println("Area da folha = " + this.leafArea + "cm");
        }else{
            System.out.println("Folha e referencia não indentificados");
        }
        */
        if(leafColor!=null) {
            long start = System.currentTimeMillis();
            originalImage = removeBackground1(originalImage, leaf);
            long elapsed = System.currentTimeMillis() - start;
            System.out.println("tempo passado removebg:" +elapsed);
            segmentByColor1(leafColor, 20);
            //originalImage = segmentLeafAndDesease1(originalImage);
        }else {
            originalImage = leaf;
        }
        Bitmap out =  Bitmap.createBitmap(originalImage.width(), originalImage.height(),conf);
        Utils.matToBitmap(originalImage,out);

            return out;
    }

    public ArrayList<Mat> splitIn4 (){
        ArrayList<Mat> leafSplited = new ArrayList<Mat>();
        Rect[] r = new Rect[4];
        r[0] = new Rect(0,0,leaf.width()/2,leaf.height()/2);
        r[1] = new Rect(leaf.width()/2,0,leaf.width()/2,leaf.height()/2);
        r[2] = new Rect(0,leaf.height()/2,leaf.width()/2,leaf.height()/2);
        r[3] = new Rect(leaf.width()/2,leaf.height()/2,leaf.width()/2,leaf.height()/2);
        for(int i= 0;i<4;i++){
            leafSplited.add(new Mat(leaf, r[i]));
        }

        return leafSplited;
    }

    public Mat concatSplitedLeaf(ArrayList<Mat> leafSplited){
        Mat hc1 = new Mat();
        Mat hc2 = new Mat();
        Core.hconcat(leafSplited.subList(0,2),hc1);
        Core.hconcat(leafSplited.subList(2,4),hc2);
        ArrayList<Mat> vc1 = new ArrayList<>();
        vc1.add(hc1);
        vc1.add(hc2);
        Mat f = new Mat();
        Core.vconcat(vc1,f);
        return f;
    }

    private Mat removeBackground1 (Mat oimg, Mat simg){
        byte[] buff_simg = new byte[(int) (simg.total() * simg.channels())];
        simg.get(0, 0, buff_simg);
        byte[] buff_oimg = new byte[(int) (oimg.total() * oimg.channels())];
        System.out.println(oimg.channels());
        oimg.get(0, 0, buff_oimg);
        int index_oimg=0;
        for(int i =0 ;i<buff_simg.length;i++){
            //System.out.println((buff_simg[i] & 0xFF));
            if(buff_simg[i] == (byte)255){
                buff_oimg[index_oimg]=  (byte) 0;
                buff_oimg[index_oimg+1]=(byte) 0;
                buff_oimg[index_oimg+2]=(byte) 0;
                buff_oimg[index_oimg+3]=(byte) 255;
            }
            index_oimg+=4;
        }
        oimg.put(0, 0, buff_oimg);
        return oimg;
    }

    private Mat removeBackground2 (Mat oimg, Mat simg){
        Mat res = new Mat();
        Core.bitwise_and(oimg,simg,res);
        return oimg;
    }

    private Mat removeBackground (Mat oimg, Mat simg){
        //Codigo usando opencv MAT
        for(int c=0;c<simg.cols();c++){
            for(int r=0;r<simg.rows();r++){
                double[] ps = simg.get(r,c);
                //System.out.println("rgb("+p[0] + ", " +p[1] + ", " + p[2]+")");
                if(ps[0] == 255){
                    double[] p = new double[4];
                    p[0]=0;
                    p[1]=0;
                    p[2]=0;
                    p[3]=1;
                    oimg.put(r,c,p);
                }
            }
        }

        return oimg;
    }

    private int getNumBlackPixels(Mat l){
        int total =0;
        for(int c =0;c<l.cols() -1;c++){
            for(int r =0;r<l.rows() -1;r++){
                double[] pixel= l.get(r,c);
                if(pixel[0] == 0 ){
                    total++;
                }
            }
        }
        return total;
    }

    public Bitmap segmentByColor(float[] color,int t){

        Mat img = originalImage;
        Mat hsv = new Mat();
        Imgproc.cvtColor(img,hsv,Imgproc.COLOR_RGB2HSV_FULL);
        for(int i=0;i<hsv.cols();i++){
            for(int k=0;k<hsv.rows();k++){
                double[] p = hsv.get(k,i);
                //System.out.println("hsl("+p[0] + ", " +p[1] + ", " + p[2]+")");
                if(p[0]> color[0]-t && p[0]<color[0]+t){

                    p[0] = 0;
                    p[1] = 255;
                    p[2] = 0;

                }else if(p[2]==0 && p[1]==0 && p[0]==0){
                    p[0] = 255;
                    p[1] = 255;
                    p[2] = 255;

                }else{
                    p[0] = 255;
                    p[1] = 0;
                    p[2] = 0;
                }
                hsv.put(k,i,p);
            }
        }
        Bitmap btm = Bitmap.createBitmap(hsv.cols(), hsv.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(hsv,btm);
        originalImage = hsv;
        return btm;
    }

    public Bitmap segmentByColor1(float[] color, int t){
        Mat img = originalImage;
        Mat hsv = new Mat();
        Mat mat_leaf = new Mat();
        Mat mat_desease = new Mat();
        Imgproc.cvtColor(img,hsv,Imgproc.COLOR_RGB2HSV_FULL);
        byte[] buff_img = new byte[(int) (hsv.total() * hsv.channels())];
        byte[] buff_img_leaf = new byte[(int)hsv.total()];
        byte[] buff_img_desease = new byte[(int)hsv.total()];
        hsv.get(0, 0, buff_img);
        int area_leaf =0;
        int area_desease =0;
        int aux_i = 0;
        for(int i =0 ;i<buff_img.length;i+=3){
            //System.out.println("hsl("+(buff_img[i] & 0xFF) + ", " +(buff_img[i+1]&0xFF) + ", " + (buff_img[i+2]&0xFF)+")");
            if(buff_img[i] > (byte)color[0]-t && buff_img[i] < (byte)color[0]+t)
            {
               // System.out.println("v");
                buff_img[i]=  (byte) 0;
                buff_img[i+1]=(byte) 255;
                buff_img[i+2]=(byte) 0;

                buff_img_leaf[aux_i] =(byte) 255;
                buff_img_desease[aux_i] = (byte)0;
                area_leaf +=1;
            }else if( buff_img[i] == (byte)0 && buff_img[i+1] ==(byte)0 && buff_img[i+2] ==(byte)0)
            {
               // System.out.println("r");
                buff_img[i]=  (byte) 255;
                buff_img[i+1]=(byte) 255;
                buff_img[i+2]=(byte) 255;

                buff_img_leaf[aux_i] =(byte) 0;
                buff_img_desease[aux_i] = (byte)0;
            }else{
               // System.out.println("r");
                buff_img[i]=  (byte) 255;
                buff_img[i+1]=(byte) 0;
                buff_img[i+2]=(byte) 0;

                buff_img_desease[aux_i] = (byte)255;
                buff_img_leaf[aux_i] =(byte) 0;
                area_desease+=1;
            }
            aux_i +=1;
        }
        leafArea = area_leaf;
        deseaseArea = area_desease;

        hsv.put(0,0,buff_img);
        mat_leaf.put(0,0,buff_img_leaf);
        mat_desease.put(0,0,buff_img_desease);
        Bitmap btm = Bitmap.createBitmap(hsv.cols(), hsv.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(hsv,btm);
        originalImage = hsv;
        return btm;
    }

    public Mat segmentByColor2(float[] color){
        Mat img = originalImage;
        Imgproc.cvtColor(img,img,Imgproc.COLOR_RGB2HSV);
        int sensitivity = 15;
        Core.inRange(img,new Scalar(60 - sensitivity, 100, 100), new Scalar(60 + sensitivity, 255, 255),img);
        /*
        System.out.println("segmentando..");
        for(int i=0;i<img.cols();i++){
            for(int k=0;k<img.rows();k++){
                double[] p = img.get(k,i);
                //System.out.println("rgb("+p[0] + ", " +p[1] + ", " + p[2]+")");
                if(color[0] == p[0] && color[1] == p[1] && color[2] == p[2]){
                    System.out.println("cor==");
                    p[0] = 0;
                    p[1] = 0;
                    p[2] = 0;
                    img.put(k,i,p);
                }
            }
        }
        */
        return img;
    }

    private void resize(int w, int h){
        Size sz = new Size(w,h);
        Imgproc.resize(leaf,leaf,sz);
    }

    public Mat segmentLeafAndDesease(Mat img){
        Imgproc.cvtColor(img,img,Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(img, img, 1, 255, Imgproc.THRESH_OTSU);
        return img;
    }

    public Mat segmentLeafAndDesease1(Mat img){
        Mat hsv = new Mat();
        Mat clusteredHSV = new Mat();
        Imgproc.cvtColor(img,hsv,Imgproc.COLOR_RGB2HSV_FULL);
        TermCriteria criteria = new TermCriteria(TermCriteria.EPS + TermCriteria.MAX_ITER,100,0.1);
        Core.kmeans(hsv, 3, clusteredHSV, criteria, 10, Core.KMEANS_PP_CENTERS);
        return clusteredHSV;
    }

    public double getDeseaseArea() {
        return deseaseArea;
    }

    public void setDeseaseArea(double deseaseArea) {
        this.deseaseArea = deseaseArea;
    }
}
