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
    private float[] leafColor = {60,170,60};


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

    public float[] getLeafColor() {
        return leafColor;
    }

    public void setLeafColor(float[] leafColor) {
        this.leafColor = leafColor;
    }
}
