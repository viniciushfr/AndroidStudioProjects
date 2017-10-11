package com.example.vinicius.opencv;

import android.app.Activity;
import android.content.Context;
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
import android.widget.Toast;

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
    Bitmap oBitmap;
    ImageView view;
    Button btnNext;
    Button btnUndo;
    boolean isColorSelected=false;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        final AplicationData app = (AplicationData) getApplicationContext();
        bitmap = app.getBitmapToProcess();

        oBitmap = bitmap;
        Utils.bitmapToMat(bitmap,img);
        app.setMatToProcess(img);
        final Leaf leaf = new Leaf(bitmap,app.getLeafColor());

        view = (ImageView)findViewById(R.id.image_view_config);
        btnNext = (Button)findViewById(R.id.btn_next) ;
        btnUndo = (Button)findViewById(R.id.btn_undo) ;

        view.setImageBitmap(bitmap);
        btnNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
            Intent troca = new Intent(ConfigActivity1.this, MainActivity.class);
            ConfigActivity1.this.startActivity(troca);

            }
        });
        btnUndo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                isColorSelected =false;
                Utils.matToBitmap(app.getMatToProcess(),bitmap);
                view.setImageBitmap(bitmap);
                img = app.getMatToProcess();

                Context context = getApplicationContext();
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, "selecione novamente", duration);
                toast.show();
            }
        });
        view.setOnTouchListener(new OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
            if(!isColorSelected) {
                view.buildDrawingCache();
                Bitmap bmap = view.getDrawingCache();
                int pixel = bmap.getPixel((int) event.getX(), (int) event.getY());

                float[] hsvColor = new float[3];
                Color.RGBToHSV(Color.red(pixel),Color.green(pixel) , Color.blue(pixel), hsvColor);

                //Colocando no interalo que o opencv utiliza
                //0 - 180 -> hue , 0-255 ->saturation, 0-255 value
                hsvColor[0] = hsvColor[0]/2;
                hsvColor[1] = (hsvColor[1]*255);
                hsvColor[2] = (hsvColor[2]*255) ;

                app.setLeafColor(hsvColor);
                long start = System.currentTimeMillis();
                img = segmentByColor(img, hsvColor);
                long elapsed = System.currentTimeMillis() - start;

                Utils.matToBitmap(img, bitmap);
                view.setImageBitmap(bitmap);
                isColorSelected = true;

                //Mostra tempo em um toast
                Context context = getApplicationContext();
                CharSequence text = "Processamento levou :"+ elapsed+"ms";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

            }
                return false;
            }
        });


    }

    private static Mat segmentByColor(Mat img,float[] color){
        Mat hsvImage = new Mat();
        System.out.println("leaf color = "+color[0]+"," + color[1]+ " "+ color[2]);
        Imgproc.cvtColor(img,img,Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(img,hsvImage,Imgproc.COLOR_RGB2HSV_FULL);
        int sensitivity = 20;
        Mat destino = new Mat();
        Core.inRange(hsvImage,new Scalar(color[0]- sensitivity, color[1]-20, color[2]-30), new Scalar(color[0] + sensitivity, 255, 255),destino);
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
        return destino;
    }

}