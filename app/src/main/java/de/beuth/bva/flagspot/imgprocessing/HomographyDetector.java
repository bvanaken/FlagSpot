package de.beuth.bva.flagspot.imgprocessing;

import android.graphics.Bitmap;
import android.util.Log;

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
import org.opencv.core.Size;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Betty van Aken on 20/07/16.
 */
public class HomographyDetector {

    private static final String TAG = "HomographyDetector";

    HomographyListener listener;

    public HomographyDetector(HomographyListener lst) {
        listener = lst;
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

                    listener.onCornerValuesDetected(cornerValues);

                    Log.d(TAG, "findHomography: detected corners");

                    Imgproc.warpPerspective(imgObject,
                            imgObject,
                            H,
                            new Size(900, 600),
                            Imgproc.INTER_CUBIC);

                    final Bitmap output = Bitmap.createBitmap(900, 600, Bitmap.Config.RGB_565);
                    Utils.matToBitmap(imgObject, output);

                    listener.onBitmapWarped(output);

                    Log.d(TAG, "findHomography: warped image");

                } else {
                    Log.d(TAG, "findHomography: H was empty");
                }
            }

        } else {
            Log.d(TAG, "findHomography: Cols wasnt equal.");
        }
    }

    public interface HomographyListener {
        void onCornerValuesDetected(float[] cornerValues);

        void onBitmapWarped(Bitmap warpedImage);
    }

}
