package com.example.vinicius.opencv;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

        Leaf leaf = new Leaf(bitmap);
        imageViewResult.setImageBitmap(leaf.process());

        TextView areaTextView  = (TextView)findViewById(R.id.areaTextView);
        areaTextView.setText(leaf.getLeafArea() + " cm");
    }


}
