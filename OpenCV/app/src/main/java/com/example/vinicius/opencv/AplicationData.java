package com.example.vinicius.opencv;

import android.app.Application;
import android.graphics.Bitmap;

import org.opencv.core.Mat;

/**
 * Created by vinicius on 15/08/17.
 */

public class AplicationData extends Application {
    private Bitmap bitmapToProcess;
    private Mat matToProcess;
    private double[] leafColor;


    public Bitmap getBitmapToProcess() {
        return bitmapToProcess;
    }

    public void setBitmapToProcess(Bitmap bitmapToProcess) {
        this.bitmapToProcess = bitmapToProcess;
    }

    public Mat getMatToProcess() {
        return matToProcess;
    }

    public void setMatToProcess(Mat matToProcess) {
        this.matToProcess = matToProcess;
    }

    public double[] getLeafColor() {
        return leafColor;
    }

    public void setLeafColor(double[] leafColor) {
        this.leafColor = leafColor;
    }
}
