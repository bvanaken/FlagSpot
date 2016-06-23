package de.beuth.bva.flagspot;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener, View.OnTouchListener {

    private CameraBridgeViewBase mOpenCvCameraView;

    FeatureDetector featureDetector;
    DescriptorExtractor featureDescriptor;

    List<Flag> flags;

    //    Mat compareObject;
    Mat currentFrame;

    List<Mat> objectDescriptors = new ArrayList<>();

    private static final String TAG = "MainActivity";

    static {
        // If you use opencv 2.4, System.loadLibrary("opencv_java")
        System.loadLibrary("opencv_java3");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    /* Now enable camera view to start receiving frames */
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        setContentView(R.layout.activity_main);
//        ButterKnife.bind(this);

        Log.d(TAG, "Creating and setting view");
        mOpenCvCameraView = new JavaCameraView(this, -1);
        setContentView(mOpenCvCameraView);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        featureDescriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);

    }

    private void createDescriptors(int objectRessource) {

        try {
            Mat compareObject = Utils.loadResource(this, objectRessource);

            // object feature detection
            MatOfKeyPoint objectKeypoints = new MatOfKeyPoint();
            featureDetector.detect(compareObject, objectKeypoints);

            // object feature description
            Mat objectDescriptorSet = new Mat();
            featureDescriptor.compute(compareObject, objectKeypoints, objectDescriptorSet);

            objectDescriptors.add(objectDescriptorSet);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deserialize() {


    }

    private void compare(Mat inputFrame) {

        // frame feature detection
        MatOfKeyPoint frameKeypoints = new MatOfKeyPoint();
        featureDetector.detect(inputFrame, frameKeypoints);

        // frame feature description
        Mat frameDescriptors = new Mat();
        featureDescriptor.compute(inputFrame, frameKeypoints, frameDescriptors);

        //match frame and object descriptors
        MatOfDMatch matches = new MatOfDMatch();

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        int bestIndex = -1;
        int maxMatches = 0;
        for (Mat objectDescriptorSet : objectDescriptors) {
            if (frameDescriptors.type() == objectDescriptorSet.type() && frameDescriptors.cols() == objectDescriptorSet.cols()) {

                matcher.match(frameDescriptors, objectDescriptorSet, matches);

                int matchesUnderThreshold = matchesUnderThreshold(matches, 60);
                if (matchesUnderThreshold > maxMatches) {
                    bestIndex = objectDescriptors.indexOf(objectDescriptorSet);
                    maxMatches = matchesUnderThreshold;
                }
            }
        }
        Log.d(TAG, "compare: bestIndex: " + bestIndex + " with " + maxMatches + " matches.");

    }

    private int matchesUnderThreshold(MatOfDMatch matches, int threshold) {
        List<DMatch> myList = matches.toList();
        Iterator itr = myList.iterator();
        float sum = 0;
        int underThreshold = 0;
        while (itr.hasNext()) {
            DMatch element = (DMatch) itr.next();
            Log.d(TAG, "compare: matchesUnderThreshold: " + element.distance);

            if (element.distance < threshold) {
//                sum += element.distance;
                underThreshold++;
            }
        }
//        Log.d(TAG, "compare: size: " + myList.size());
//        if(underThreshold != 0){
//            Log.d(TAG, "compare: average: " + (sum/underThreshold));
//        }
        return underThreshold;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        currentFrame = inputFrame;

        Mat resizeimage = new Mat();
//        Size sz = new Size(200,113);
//        Imgproc.resize( inputFrame, resizeimage, sz );

        Imgproc.GaussianBlur(inputFrame, resizeimage, new org.opencv.core.Size (5,5), 2.2, 2);

        return resizeimage;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
//        if (currentFrame != null) {
//
//            // Create empty Mat with
//            Mat tmp = new Mat(0, 0, CvType.CV_8U, new Scalar(4));
//            Imgproc.cvtColor(currentFrame, tmp, Imgproc.COLOR_RGBA2RGB, 4);
//
//            Bitmap bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
//            Utils.matToBitmap(tmp, bmp);
//            FlagComparer.compareFlag(this, bmp);
//        }
        return false;
    }
}
