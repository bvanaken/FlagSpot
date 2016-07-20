package de.beuth.bva.flagspot;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
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
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener {

    private static final String TAG = "MainActivity";

    private RelativeLayout layout;
    private CameraBridgeViewBase openCvCameraView;
    private TextView countryNameTextView;
    private ImageView previewImgView;
    //    private DragRectView dragRectView;
    private DrawPathView dragRectView;
    ProgressDialog progressDialog;

    private FlagComparer flagComparer;
    private Mat currentFrame;
    private Mat selectedRegion;

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
        dragRectView.setDimensions(inputFrame.rows(), inputFrame.cols());

        if (currentFrame != null && selectedRegion != null) {
//            findHomography(selectedRegion, currentFrame);
        }

        return inputFrame;
    }

    private void setupViews() {

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

        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(300, 300);
//        RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        imageParams.setMargins(0, 0, 20, 0);
        layout.addView(previewImgView, imageParams);

        // Setup drag rect view
//        dragRectView = new DragRectView(this);
        dragRectView = new DrawPathView(this);
        dragRectView.setId(R.id.dragrect_view);

        RelativeLayout.LayoutParams dragParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layout.addView(dragRectView, dragParams);

//        dragRectView.setOnUpCallback(new DragRectView.OnUpCallback() {
//            @Override
//            public void onRectFinished(final Rect rect) {
//                onRegionSelected(rect);
//            }
//        });

        dragRectView.setOnUpCallback(new DrawPathView.OnUpCallback() {
            @Override
            public void onPathFinished(final Mat mask) {
                onRegionSelected(mask);
            }
        });

    }

    private void onRegionSelected(Rect rect) {
        if (currentFrame != null && rect.width > 0 && rect.height > 0) {
            // Cut out rectangle from input Mat
            Mat newlySelectedRegion = currentFrame.submat(rect);
            selectedRegion = newlySelectedRegion;

            processCapturedImage(newlySelectedRegion);
        }
    }

    private void onRegionSelected(Mat mask) {
        if (currentFrame != null && mask != null && !mask.empty()) {
            Mat copiedFrame = new Mat();
            Mat copiedMask = new Mat();
            currentFrame.copyTo(copiedFrame);
            mask.copyTo(copiedMask);
            new GrabCutTask().execute(copiedFrame, copiedMask);
//            applyGrabCut(currentFrame, mask);
        }
    }

    private void processCapturedImage(Mat image) {

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

    private void findHomography(Mat imgObject, Mat scene) {

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);

        MatOfKeyPoint keypointsObject = new MatOfKeyPoint();
        MatOfKeyPoint keypointsScene = new MatOfKeyPoint();

        detector.detect(imgObject, keypointsObject);
        detector.detect(scene, keypointsScene);

        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

        Mat descriptorObject = new Mat();
        Mat descriptorScene = new Mat();

        extractor.compute(imgObject, keypointsObject, descriptorObject);
        extractor.compute(scene, keypointsScene, descriptorScene);

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        MatOfDMatch matches = new MatOfDMatch();

        // Check for equal columns (sometimes they dont match)
        if (descriptorObject.cols() == descriptorScene.cols()) {

            // Find matches
            matcher.match(descriptorObject, descriptorScene, matches);
            List<DMatch> matchesList = matches.toList();

            Double max_dist = 0.0;
            Double min_dist = 100.0;

            for (int i = 0; i < descriptorObject.rows(); i++) {
                Double dist = (double) matchesList.get(i).distance;
                if (dist < min_dist) min_dist = dist;
                if (dist > max_dist) max_dist = dist;
            }

            System.out.println("-- Max dist : " + max_dist);
            System.out.println("-- Min dist : " + min_dist);

            LinkedList<DMatch> goodMatchesList = new LinkedList<>();

            for (int i = 0; i < descriptorObject.rows(); i++) {
                if (matchesList.get(i).distance < 1 * min_dist) {
                    goodMatchesList.addLast(matchesList.get(i));
                }
            }

            MatOfDMatch goodMatches = new MatOfDMatch();
            goodMatches.fromList(goodMatchesList);

            LinkedList<Point> objList = new LinkedList<>();
            LinkedList<Point> sceneList = new LinkedList<>();

            List<KeyPoint> keypointsObjectList = keypointsObject.toList();
            List<KeyPoint> keypointsSceneList = keypointsScene.toList();

            for (int i = 0; i < goodMatchesList.size(); i++) {
                objList.addLast(keypointsObjectList.get(goodMatchesList.get(i).queryIdx).pt);
                sceneList.addLast(keypointsSceneList.get(goodMatchesList.get(i).trainIdx).pt);
            }

            MatOfPoint2f pointsObject = new MatOfPoint2f();
            pointsObject.fromList(objList);

            MatOfPoint2f pointsScene = new MatOfPoint2f();
            pointsScene.fromList(sceneList);

            if (!pointsObject.empty() && !pointsScene.empty()) {
                Mat H = Calib3d.findHomography(pointsObject, pointsScene);

                Mat objCorners = new Mat(4, 1, CvType.CV_32FC2);
                Mat sceneCorners = new Mat(4, 1, CvType.CV_32FC2);

                objCorners.put(0, 0, new double[]{0, 0});
                objCorners.put(1, 0, new double[]{imgObject.cols(), 0});
                objCorners.put(2, 0, new double[]{imgObject.cols(), imgObject.rows()});
                objCorners.put(3, 0, new double[]{0, imgObject.rows()});

                if (!H.empty()) {
                    Core.perspectiveTransform(objCorners, sceneCorners, H);

                    for (int i = 0; i < sceneCorners.rows(); i++) {
                        for (int j = 0; j < sceneCorners.cols(); j++) {
                            Log.d(TAG, "i: " + i + ",j: " + j);
                            for (double val : sceneCorners.get(i, j)) {
                                Log.d(TAG, val + ",");
                            }
                        }
                    }

                    final float[] cornerValues = new float[]{
                            (float) sceneCorners.get(0, 0)[0], (float) sceneCorners.get(0, 0)[1],
                            (float) sceneCorners.get(1, 0)[0], (float) sceneCorners.get(1, 0)[1],
                            (float) sceneCorners.get(2, 0)[0], (float) sceneCorners.get(2, 0)[1],
                            (float) sceneCorners.get(3, 0)[0], (float) sceneCorners.get(3, 0)[1]
                    };

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            previewImgView.setCornerValues(cornerValues);
                        }
                    });

                    Log.d(TAG, "findHomography: found something");

//                    Imgproc.warpPerspective(imgObject,
//                            imgObject,
//                            H,
//                            new Size(900, 600),
//                            Imgproc.INTER_CUBIC);
//
//                    final Bitmap output = Bitmap.createBitmap(900, 600, Bitmap.Config.RGB_565);
//                    Utils.matToBitmap(imgObject, output);
//
//                    // Set into preview
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            previewImgView.setImageBitmap(output);
//                        }
//                    });

                } else {
                    Log.d(TAG, "findHomography: H was empty");
                }
            }

        } else {
            Log.d(TAG, "findHomography: Cols wasnt equal.");
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

    private Mat applyGrabCut(Mat img, Mat mask) {

        // Convert current frame image from RGBA to RGB
        Mat rgbImage = new Mat(0, 0, CvType.CV_8UC3, new Scalar(3));
        Imgproc.cvtColor(img, rgbImage, Imgproc.COLOR_RGBA2RGB, 3);

        // fill an inner rectangle in the mask with 'possible foreground values' to evaluate it
        for (int i = 40; i < rgbImage.height() - 40; i++) {
            for (int j = 40; j < rgbImage.width() - 40; j++) {

                if(mask.get(i, j) == null){
                    Log.d(TAG, "applyGrabCut: pixel val is null: " + i + ", " + j);
                    continue;
                }
//                if(mask.get(i, j) == null || mask.get(i, j)[0] == Imgproc.GC_BGD){
                if(mask.get(i, j)[0] == Imgproc.GC_BGD){
                    mask.put(i, j, new byte[]{Imgproc.GC_PR_FGD});
                }
            }
        }

        // run the grabCut with 5 iterations
        Imgproc.grabCut(rgbImage, mask, new Rect(), new Mat(), new Mat(), 5, Imgproc.GC_INIT_WITH_MASK);

        // after mask was changed by the grabCut, black out all image regions that are background or possible background
        for (int i = 0; i < mask.rows(); i++) {
            for (int j = 0; j < mask.cols(); j++) {

                if (mask.get(i, j)[0] == Imgproc.GC_BGD || mask.get(i, j)[0] == Imgproc.GC_PR_BGD) {
                    rgbImage.put(i, j, new byte[]{0, 0, 0});
                }
            }
        }


        Log.d(TAG, "applyGrabCut: IMAGE ADDED");
        return rgbImage;

    }

    private class GrabCutTask extends AsyncTask<Mat, Integer, Mat> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setMessage("Processing Image...");
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.show();

            openCvCameraView.disableView();
        }

        @Override
        protected Mat doInBackground(final Mat... params) {

            if(params.length >= 2){
                Mat frame = params[0];
                Mat mask = params[1];

                if(frame != null && mask != null && !mask.empty()){
                    return applyGrabCut(frame, mask);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Mat resultImage) {
            super.onPostExecute(resultImage);

            if(resultImage != null){
                // Create bitmap
                Bitmap bitmapOfImage = Bitmap.createBitmap(resultImage.cols(), resultImage.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(resultImage, bitmapOfImage);

                // Set into preview
                previewImgView.setImageBitmap(bitmapOfImage);
            }

            progressDialog.dismiss();
            openCvCameraView.enableView();

        }
    }
}


