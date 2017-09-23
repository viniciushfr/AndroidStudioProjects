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
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by vinicius on 26/08/17.
 */

public class Leaf {
    private Mat leaf;
    private double[] leafColor;
    private Mat originalImage;
    private double leafArea;
    private double leafWidth;
    private double leafHeight;

    public Leaf(Bitmap image,double[] leafColor){
        this.leaf = new Mat();
        Utils.bitmapToMat(image,this.leaf);
        this.leafColor = leafColor;
        this.leafArea = 0;
        this.leafHeight =0;
        this.leafWidth =0;
        resize(leaf.width()/10,leaf.height()/10);
        originalImage = leaf;
    }

    public double getLeafHeight() {
        return leafHeight;
    }

    public void setLeafHeight(int leafHeight) {
        this.leafHeight = leafHeight;
    }

    public double getLeafWidth() {
        return leafWidth;
    }

    public void setLeafWidth(int leafWidth) {
        this.leafWidth = leafWidth;
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
        originalImage = removeBackground(originalImage,leaf);

        //segmentByColor(leafColor,80);
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

    private Mat removeBackground (Mat oimg, Mat simg){
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
    public Bitmap segmentByColor(double[] color,int t){

        Mat img = originalImage;

        System.out.println("segmentando..");
        for(int i=0;i<img.cols();i++){
            for(int k=0;k<img.rows();k++){
                double[] p = img.get(k,i);
                //System.out.println("rgb("+p[0] + ", " +p[1] + ", " + p[2]+")");
                if(Math.abs(color[0] - p[0]) <=t && Math.abs(color[1] - p[1]) <=t && Math.abs(color[2] - p[2]) <=t){
                    System.out.println("f");
                    p[0] = 0;
                    p[1] = 255;
                    p[2] = 0;

                }else{
                    System.out.println("d");
                    p[0] = 255;
                    p[1] = 0;
                    p[2] = 0;

                }
                img.put(k,i,p);
            }
        }
        Bitmap btm = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img,btm);
        originalImage = img;
        return btm;
    }
    private void resize(int w, int h){
        Size sz = new Size(w,h);
        Imgproc.resize(leaf,leaf,sz);
    }
}
