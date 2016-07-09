package de.beuth.bva.flagspot;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener {

    private static final String TAG = "MainActivity";

    private RelativeLayout layout;
    private CameraBridgeViewBase openCvCameraView;
    private TextView countryNameTextView;
    private ImageView previewImgView;
    private DragRectView dragRectView;

    private FlagComparer flagComparer;
    private Mat currentFrame;

    static {
        System.loadLibrary("opencv_java3");
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Enable camera view to start receiving frames
                    openCvCameraView.enableView();
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
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Initialize comparer (which will load list of flags)
        flagComparer = new FlagComparer(this);

        setupViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (openCvCameraView != null)
            openCvCameraView.disableView();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (openCvCameraView != null)
            openCvCameraView.disableView();
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

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {

        currentFrame = inputFrame;
        return inputFrame;
    }

    private void setupViews() {

        // Base layout
        layout = (RelativeLayout) findViewById(R.id.main_relative_layout);

        // Setup Camera view
        openCvCameraView = new JavaCameraView(this, -1);
        openCvCameraView.setId(R.id.opencv_cameraview);
        openCvCameraView.setVisibility(View.VISIBLE);
        openCvCameraView.setCvCameraViewListener(this);

        RelativeLayout.LayoutParams cameraViewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layout.addView(openCvCameraView, cameraViewParams);

        // Setup Text view
        countryNameTextView = new TextView(this);
        countryNameTextView.setId(R.id.countryname_textview);
        countryNameTextView.setTextColor(Color.RED);

        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(countryNameTextView, textParams);

        // Setup Image view
        previewImgView = new ImageView(this);
        previewImgView.setId(R.id.preview_imageview);

        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(150, 150);
        imageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layout.addView(previewImgView, imageParams);

        // Setup drag rect view
        dragRectView = new DragRectView(this);
        dragRectView.setId(R.id.dragrect_view);

        RelativeLayout.LayoutParams dragParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layout.addView(dragRectView, dragParams);

        dragRectView.setOnUpCallback(new DragRectView.OnUpCallback() {
            @Override
            public void onRectFinished(final Rect rect) {

                if (rect.width > 0 && rect.height > 0) {
                    // Cut out rectangle from input Mat
                    Mat selectedRegion = currentFrame.submat(rect);

                    processCapturedImage(selectedRegion);
                }
            }
        });
    }

    private void processCapturedImage(Mat image) {

        if (currentFrame != null) {

//            Mat resizeimage = new Mat();
//            Size sz = new Size(100,100);
//            Imgproc.resize( selectedRegion, resizeimage, sz );

            // Convert RGBA to RGB
            Mat rgbImage = new Mat(0, 0, CvType.CV_8U, new Scalar(4));
            Imgproc.cvtColor(image, rgbImage, Imgproc.COLOR_RGBA2RGB, 4);

            // Create bitmap
            Bitmap bitmapOfImage = Bitmap.createBitmap(rgbImage.cols(), rgbImage.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgbImage, bitmapOfImage);

            // Set into preview
            previewImgView.setImageBitmap(bitmapOfImage);

            // Calculate flag match
            String nearest = flagComparer.compareFlag(bitmapOfImage);

            // Display nearest match
            if (nearest != null) {
                countryNameTextView.setText(nearest);
            }
        }

    }
}
