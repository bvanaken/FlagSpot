package de.beuth.bva.flagspot.imgprocessing;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Betty van Aken on 17/07/16.
 */
public class GrabCutter {

    private static final String TAG = "GrabCutter";
    GrabCutListener listener;
    GrabCutTask task;

    public GrabCutter(GrabCutListener lst) {
        listener = lst;
    }

    public void startGrabCut(Mat img, Mat mask) {
        task = new GrabCutTask();
        task.execute(img, mask);
    }

    public void cancelGrabCut(){
        if(task != null){
            task.cancel(true);
            task = null;
        }
    }

    private Mat cutOutImage(Mat img, Mat mask) {

        // Convert current frame image from RGBA to RGB
        Mat rgbImage = new Mat(0, 0, CvType.CV_8UC3, new Scalar(3));
        Imgproc.cvtColor(img, rgbImage, Imgproc.COLOR_RGBA2RGB, 3);

        // fill an inner rectangle in the mask with 'possible foreground values' to evaluate it
        for (int i = 40; i < rgbImage.height() - 40; i++) {
            for (int j = 40; j < rgbImage.width() - 40; j++) {

                // only fill if value was 'background' before (to not overwrite foreground values)
                if (mask.get(i, j)[0] == Imgproc.GC_BGD) {
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

        return rgbImage;

    }

    private class GrabCutTask extends AsyncTask<Mat, Integer, Mat> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (listener != null) {
                listener.grabCutStarted();
            }
        }

        @Override
        protected Mat doInBackground(final Mat... params) {

            if (params.length >= 2) {
                Mat frame = params[0];
                Mat mask = params[1];

                if (frame != null && mask != null && !mask.empty()) {
                    return cutOutImage(frame, mask);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Mat resultImage) {
            super.onPostExecute(resultImage);

            Bitmap bitmapOfImage = null;

            if (resultImage != null) {
                // Create bitmap
                bitmapOfImage = Bitmap.createBitmap(resultImage.cols(), resultImage.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(resultImage, bitmapOfImage);
            }
            listener.grabCutFinished(bitmapOfImage);
        }

        @Override
        protected void onCancelled(){
            listener.grabCutFinished(null);
        }
    }

    public interface GrabCutListener {
        void grabCutStarted();

        void grabCutFinished(Bitmap cuttedImage);
    }

}
