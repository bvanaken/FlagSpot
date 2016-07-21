package de.beuth.bva.flagspot.flaglogic;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import org.apache.commons.math3.util.MathArrays;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.beuth.bva.flagspot.model.Flag;

/**
 * Created by betty on 18/06/16.
 */
public class FlagComparer {

    private static final String TAG = "FlagComparer";

    final static int PIXEL_MARGIN = 10;

    final static int SAMPLES_X = 40;
    final static int SAMPLES_Y = 40;

    final static int EXCLUSION_THRESHOLD = 5000;

    List<Flag> flags;

    public FlagComparer(Context context) {
        if (flags == null) {
            setupFlagList(context);
        }
    }

    public List<String> compareFlag(Bitmap flagImg) {

        double[] testVector = getVectorForImage(flagImg, SAMPLES_X, SAMPLES_Y);
        if (testVector != null) {

            List<String> nearestFlags = new ArrayList<>();

            double shortestDistance = Double.MAX_VALUE;
            String nearestFlag = null;
            Map<Integer, Flag> otherNearFlags = new HashMap<>();

            for (Flag flag : flags) {
                double distance = MathArrays.distance(flag.getVector(), testVector);
                if (distance < shortestDistance && distance < EXCLUSION_THRESHOLD) {
                    shortestDistance = distance;
                    nearestFlag = flag.getName();

                } else if (distance < EXCLUSION_THRESHOLD) {
                    otherNearFlags.put((int) distance, flag);
                    Log.d(TAG, flag.getName() + ": " + distance);
                }
            }

            nearestFlags.add(nearestFlag);

            Log.d(TAG, "-----");
            Log.d(TAG, nearestFlag + ": " + shortestDistance);

            double shortestDifference = Double.MAX_VALUE;

            for (Integer distance : otherNearFlags.keySet()) {
                if (distance - shortestDistance < 500 && distance - shortestDistance < shortestDifference){
                    nearestFlags.add(otherNearFlags.get(distance).getName());
                    shortestDifference = distance - shortestDistance;
                }
            }

            return nearestFlags;
        }

        return null;
    }

    private double[] getVectorForImage(Bitmap img, int samplesX, int samplesY) {

        int stepsWidth = (int) Math.floor(((img.getWidth() - PIXEL_MARGIN * 2.0) / (samplesX - 1)));
        int stepsHeight = (int) Math.floor((img.getHeight() - PIXEL_MARGIN * 2.0) / (samplesY - 1));
        int channels = 3;

        if (stepsWidth < 0 || stepsHeight < 0) {
            return null;
        }

        double[] vector = new double[samplesX * samplesY * 3];

        for (int i = 0; i < samplesY; i++) {
            for (int j = 0; j < samplesX; j++) {

                int color = img.getPixel(stepsWidth * i, stepsHeight * j);

                vector[i * samplesX * channels + j * channels] = Color.red(color);
                vector[i * samplesX * channels + j * channels + 1] = Color.green(color);
                vector[i * samplesX * channels + j * channels + 2] = Color.blue(color);
            }
        }
        return vector;
    }

    private void setupFlagList(Context context) {
        try {
            // Deserialize List<Flag> Object
            AssetManager assetManager = context.getAssets();
            InputStream fileIn = assetManager.open("flags_highsampled.csv");

            ObjectInputStream in = new ObjectInputStream(fileIn);
            flags = (List<Flag>) in.readObject();
            in.close();
            fileIn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
