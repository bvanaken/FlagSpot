package de.beuth.bva.flagspot;

import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import de.beuth.bva.flagspot.flaglogic.FlagComparer;
import de.beuth.bva.flagspot.imgprocessing.GrabCutter;
import de.beuth.bva.flagspot.rest.RestCountryProvider;
import de.beuth.bva.flagspot.views.DrawPathView;
import de.beuth.bva.flagspot.views.DrawRectView;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener, GrabCutter.GrabCutListener {

    private static final String TAG = "MainActivity";

    private RelativeLayout layout;
    private CameraBridgeViewBase openCvCameraView;
    private TextView countryNameTextView;
    private TextView titleTextView;
    private TextView toggleTextView;
    private ImageView previewImgView;

    private DrawRectView drawRectView;
    private DrawPathView drawPathView;

    ProgressDialog progressDialog;
    boolean inRectMode = true;

    private FlagComparer flagComparer;
    private GrabCutter grabCutter;
    private Mat currentFrame;
    private Mat selectedRegion;

    private RestCountryProvider restCountryProvider;

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

        // setup REST provider for country informations
        restCountryProvider = new RestCountryProvider(this);

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
//        if (id == R.id.action_settings) {
//            return true;
//        }

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
        if (!inRectMode) {
            drawPathView.setDimensions(inputFrame.rows(), inputFrame.cols());
        }

        if (currentFrame != null && selectedRegion != null) {
//            findHomography(selectedRegion, currentFrame);
        }

        return inputFrame;
    }

    private void setupViews() {
        // The view setup needs to be programmatically right now, because JavaCameraView cannot be added by xml
        // But because it must be at the very back of all views, the others need to be added afterwards
        // z-depth is not available at the supported min android version

        // Base layout
        layout = (RelativeLayout) findViewById(R.id.main_relative_layout);

        progressDialog = new ProgressDialog(this);

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

        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(600, 600);
        imageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        imageParams.setMargins(0, 0, 20, 0);
        layout.addView(previewImgView, imageParams);

        // Setup draw rect view
        drawRectView = new DrawRectView(this);
        drawRectView.setId(R.id.drawrect_view);

        RelativeLayout.LayoutParams drawRectParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layout.addView(drawRectView, drawRectParams);

        drawRectView.setOnUpCallback(new DrawRectView.OnUpCallback() {
            @Override
            public void onRectFinished(final Rect rect) {
                onRegionSelected(rect);
            }
        });

        // Setup draw path view (dont add this one right now)
        drawPathView = new DrawPathView(this);
        drawRectView.setId(R.id.drawpath_view);

        drawPathView.setOnUpCallback(new DrawPathView.OnUpCallback() {
            @Override
            public void onPathFinished(final Mat mask) {
                onRegionSelected(mask);
            }
        });

        // Setup title view
        titleTextView = new TextView(this);
        titleTextView.setId(R.id.title_textview);
        titleTextView.setTextColor(Color.WHITE);
        titleTextView.setBackgroundColor(Color.rgb(100, 150, 250));
        titleTextView.setText("FlagSpot");
        titleTextView.setPadding(10, 5, 0, 5);

        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(titleTextView, titleParams);

        // Setup toggle mode view
        toggleTextView = new TextView(this);
        toggleTextView.setId(R.id.toggle_textview);
        toggleTextView.setTextColor(Color.WHITE);
        toggleTextView.setBackgroundColor(Color.rgb(100, 150, 250));
        toggleTextView.setText("Rect Mode");
        titleTextView.setPadding(0, 5, 10, 5);

        RelativeLayout.LayoutParams toggleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        toggleParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layout.addView(toggleTextView, toggleParams);

        toggleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelectionMode();
            }
        });
    }

    private void toggleSelectionMode() {

        // Remove text views
        layout.removeView(toggleTextView);
        layout.removeView(titleTextView);

        // Change between rect or path view
        if (inRectMode) {
            layout.removeView(drawRectView);
            RelativeLayout.LayoutParams drawPathParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            layout.addView(drawPathView, drawPathParams);
            toggleTextView.setText("GrabCut Mode");
        } else {

            // be sure the grabcut task is not running anymore
            grabCutter.cancelGrabCut();
            grabCutFinished(null);

            layout.removeView(drawPathView);
            RelativeLayout.LayoutParams drawPathParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            layout.addView(drawRectView, drawPathParams);
            toggleTextView.setText("Rect Mode");
        }

        // Readd text views
        RelativeLayout.LayoutParams titleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        layout.addView(titleTextView, titleParams);

        RelativeLayout.LayoutParams toggleParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        toggleParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layout.addView(toggleTextView, toggleParams);

        inRectMode = !inRectMode;
    }

    private void onRegionSelected(Rect rect) {
        if (currentFrame != null && rect.width > 30 && rect.height > 30) {
            // Cut out rectangle from input Mat
            Mat newlySelectedRegion = currentFrame.submat(rect);
            selectedRegion = newlySelectedRegion;

            processCapturedRect(newlySelectedRegion);
        }
    }

    private void onRegionSelected(Mat mask) {
        if (currentFrame != null && mask != null && !mask.empty()) {

            // To be sure not to pass the Mats by reference, copy them first
            Mat copiedFrame = new Mat();
            Mat copiedMask = new Mat();

            currentFrame.copyTo(copiedFrame);
            mask.copyTo(copiedMask);

            // Start the grabCut algorithm
            grabCutter = new GrabCutter(this);
            grabCutter.startGrabCut(copiedFrame, copiedMask);
        }
    }

    private void processCapturedRect(Mat image) {

        if (currentFrame != null) {

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

    private Mat displayLines(Mat sampledImage) {

        Mat resizeimage = new Mat();
        Size sz = new Size(600, 400);
        Imgproc.resize(sampledImage, resizeimage, sz);

        Mat binaryImage = new Mat();
        Imgproc.cvtColor(resizeimage, binaryImage, Imgproc.COLOR_RGB2GRAY);

        Size size = new Size(5, 5);
//        Imgproc.GaussianBlur(binaryImage, binaryImage, size, 0);

        Imgproc.Canny(binaryImage, binaryImage, 20, 100);
        Mat lines = new Mat();
        int threshold = 80;

        Imgproc.HoughLinesP(binaryImage, lines, 1, Math.PI / 90, threshold, 100, 10);

        for (int i = 0; i < lines.cols(); i++) {
            double[] line = lines.get(0, i);
            double xStart = line[0],
                    yStart = line[1],
                    xEnd = line[2],
                    yEnd = line[3];
            org.opencv.core.Point lineStart = new org.opencv.core.Point(xStart, yStart);

            org.opencv.core.Point lineEnd = new org.opencv.core.Point(xEnd, yEnd);

            Imgproc.line(binaryImage, lineStart, lineEnd, new
                    Scalar(0, 0, 255), 3);
        }
        return binaryImage;

    }

    // GRABCUT Callbacks

    @Override
    public void grabCutStarted() {
        progressDialog.setMessage("Processing Image...");
        progressDialog.setCancelable(true);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (grabCutter != null) {
                    grabCutter.cancelGrabCut();
                }
            }
        });
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        openCvCameraView.disableView();
    }

    @Override
    public void grabCutFinished(Bitmap cuttedImage) {
        if (cuttedImage != null) {
            // Set into preview
            previewImgView.setImageBitmap(cuttedImage);
        }

        drawPathView.resetDrawing();
        progressDialog.dismiss();
        openCvCameraView.enableView();
    }
}


