package com.example.vinicius.opencv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        AplicationData app = (AplicationData) getApplicationContext();
        Bitmap bitmap = app.getBitmapToProcess();
        ImageView imageViewResult = (ImageView)findViewById(R.id.image_view_result);

        Leaf leaf = new Leaf(bitmap,app.getLeafColor());

        //Processa a folha calcula o tempo e exibe na tela
        long start = System.currentTimeMillis();
        imageViewResult.setImageBitmap(leaf.process());
        long elapsed = System.currentTimeMillis() - start;

        //Mostra tempo em um toast
        Context context = getApplicationContext();
        CharSequence text = "Processamento levou :"+ elapsed+"ms";
        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        TextView areaTextView  = (TextView)findViewById(R.id.areaTextView);
        areaTextView.setText(leaf.getLeafArea() + " cm");
    }


}
