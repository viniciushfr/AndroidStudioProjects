package com.example.vinicius.opencv;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Map;


public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "MainActivity";
    private Button btnCamera, btnStorage;
    private static final int PICK_IMAGE = 100;
    static {
        if(OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV  Loaded");
        }else{
            Log.d(TAG, "OpenCV not Loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCamera = (Button)findViewById(R.id.btn_camera);
        btnStorage = (Button)findViewById(R.id.btn_storage);

        btnCamera.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent troca = new
                Intent(MainActivity.this, CameraActivity.class);
                MainActivity.this.startActivity(troca);

            }
        });
        btnStorage.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                openGalery();
            }
        });
    }

    private void openGalery(){
        Intent galery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(galery, PICK_IMAGE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            Uri imageUri = data.getData();
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                AplicationData app = (AplicationData) getApplicationContext();
                app.setBitmapToProcess(bitmap);

                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                startActivity(intent);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
