package com.example.vinicius.opencv;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.vinicius.opencv.R;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class ConfigActivity1 extends Activity
{
    Mat img = new Mat();
    Bitmap bitmap;
    ImageView view;
    Button btnNext;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        final AplicationData app = (AplicationData) getApplicationContext();
        bitmap = app.getBitmapToProcess();
        Utils.bitmapToMat(bitmap,img);
        final Leaf leaf = new Leaf(bitmap,app.getLeafColor());

        view = (ImageView)findViewById(R.id.image_view_config);
        btnNext = (Button)findViewById(R.id.btn_next) ;
        view.setImageBitmap(bitmap);
        btnNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent troca = new Intent(ConfigActivity1.this, MainActivity.class);
                ConfigActivity1.this.startActivity(troca);

            }
        });
        view.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                view.buildDrawingCache();
                Bitmap bmap = view.getDrawingCache();
                int pixel = bmap.getPixel((int)event.getX(),(int)event.getY());


                double[] color = new double[3];
                color[0] = Color.red(pixel);
                color[1] = Color.green(pixel);
                color[2] = Color.blue(pixel);
                app.setLeafColor(color);
                System.out.println("rgb("+color[0] + ", " +color[1] + ", " + color[2]+")");


                view.setImageBitmap(leaf.segmentByColor(color,70));

                return false;
            }
        });


    }



}