package com.example.vinicius.opencv;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
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

public class ConfigActivity extends Activity implements OnTouchListener
{
    ArrayList<Integer> leafColors;
    ArrayList<Integer> diseaseColors;
    PointF down = new PointF();
    PointF up = new PointF();
    private static final String TAG = "Touch";
    @SuppressWarnings("unused")
    private static final float MIN_ZOOM = 1f,MAX_ZOOM = 1f;
    ImageView view;
    // These matrices will be used to scale points of the image
    Matrix matrix = new Matrix();
    Matrix savedMatrix = new Matrix();

    Mat img = new Mat();
    Bitmap bitmap;

    // The 3 states (events) which the user is trying to perform
    static final int NONE = 0;
    static final int DRAG = 1;
    static final int ZOOM = 2;
    int mode = NONE;

    // these PointF objects are used to record the point(s) the user is touching
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 1f;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        AplicationData app = (AplicationData) getApplicationContext();
        bitmap = app.getBitmapToProcess();
        Utils.bitmapToMat(bitmap,img);
        Size sz = new Size(img.width()/10,img.height()/10);
        Imgproc.resize(img,img,sz);
        Mat imgHsl = new Mat();
        //Imgproc.cvtColor(img,imgHsl,Imgproc.COLOR_RGB2HLS);
        //Imgproc.cvtColor(imgHsl,img,Imgproc.COLOR_HLS2RGB);
        view = (ImageView)findViewById(R.id.image_view_config);
        diseaseColors = new ArrayList<>();
        leafColors = new ArrayList<>();
        view.setImageBitmap(bitmap);
        view.setOnTouchListener(this);


    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        dumpEvent(event);
        // Handle touch events here...


        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:   // first finger down only
                down.set(event.getX(),event.getY());
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG"); // write to LogCat
                mode = DRAG;


                break;

            case MotionEvent.ACTION_UP: // first finger lifted
                up.set(event.getX(),event.getY());
                if(up.x == down.x && up.y == down.y){
                    view.buildDrawingCache();
                    Bitmap bmap = view.getDrawingCache();
                    int pixel = bmap.getPixel((int)event.getX(),(int)event.getY());

                    //then do what you want with the pixel data, e.g
                    double[] color = new double[3];
                    color[0] = Color.red(pixel);
                    color[1] = Color.green(pixel);
                    color[2] = Color.blue(pixel);

                    System.out.println("rgb("+color[0] + ", " +color[1] + ", " + color[2]+")");

                    img = segmentByColor(img,color);
                    Bitmap btm = Bitmap.createBitmap(img.cols(), img.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(img,btm);
                    this.view.setImageBitmap(btm);


                }
                break;
            case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;

            case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;

            case MotionEvent.ACTION_MOVE:

                if (mode == DRAG)
                {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                }
                else if (mode == ZOOM)
                {
                    // pinch zooming
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 5f)
                    {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist; // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }


        view.setImageMatrix(matrix); // display the transformation on screen

        return true; // indicate event was handled
    }

    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */

    private float spacing(MotionEvent event)
    {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */

    private void midPoint(PointF point, MotionEvent event)
    {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    private static Mat floodFill(Mat img)
    {
        Imgproc.cvtColor(img,img,Imgproc.COLOR_RGB2GRAY );
        Mat floodfilled = Mat.zeros(img.rows() + 2, img.cols() + 2, CvType.CV_8U);
        Imgproc.floodFill(img, floodfilled, new Point(0, 0), new Scalar(255), 4 );
        /*
        Core.subtract(floodfilled, Scalar.all(0), floodfilled);

        Rect roi = new Rect(1, 1, img.cols() - 2, img.rows() - 2);
        Mat temp = new Mat();

        floodfilled.submat(roi).copyTo(temp);

        img = temp;

        //Core.bitwise_not(img, img);
        */

        return floodfilled;
    }
    private static Mat segmentByColor(Mat img,double[] color){
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
        return img;
    }

    /** Show an event in the LogCat view, for debugging */
    private void dumpEvent(MotionEvent event)
    {
        String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE","POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
        StringBuilder sb = new StringBuilder();
        int action = event.getAction();
        int actionCode = action & MotionEvent.ACTION_MASK;
        sb.append("event ACTION_").append(names[actionCode]);

        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP)
        {
            sb.append("(pid ").append(action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
            sb.append(")");
        }

        sb.append("[");
        for (int i = 0; i < event.getPointerCount(); i++)
        {
            sb.append("#").append(i);
            sb.append("(pid ").append(event.getPointerId(i));
            sb.append(")=").append((int) event.getX(i));
            sb.append(",").append((int) event.getY(i));
            if (i + 1 < event.getPointerCount())
                sb.append(";");
        }

        sb.append("]");
        Log.d("Touch Events ---------", sb.toString());
    }
}