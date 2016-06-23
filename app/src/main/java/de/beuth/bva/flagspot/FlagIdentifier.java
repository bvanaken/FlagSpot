package de.beuth.bva.flagspot;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

/**
 * Created by betty on 18/06/16.
 */
public class FlagIdentifier {

    private static final String TAG = "FlagIdentifier";

    public static Flag fillFlagColors(Bitmap flagImg, Flag flag, int threshold) {
        int[][] colorMatrix = new int[3][3];
        int width = flagImg.getWidth();
        int height = flagImg.getHeight();

        colorMatrix[0][0] = flagImg.getPixel((int) (1 / 10.0 * width), (int) (1 / 10.0 * height));
        Log.d(TAG, "fillFlagColors: oben links");
        Log.d(TAG, "fillFlagColors: " + Color.red(colorMatrix[0][0]));
        Log.d(TAG, "fillFlagColors: " + Color.green(colorMatrix[0][0]));
        Log.d(TAG, "fillFlagColors: " + Color.blue(colorMatrix[0][0]));
        Log.d(TAG, "fillFlagColors: pixel: " + (int) (1 / 10.0 * width) + ", " + (int) (1 / 10.0 * height));

        colorMatrix[0][1] = flagImg.getPixel((int) (5 / 10.0 * width), (int) (1 / 10.0 * height));
        colorMatrix[0][2] = flagImg.getPixel((int) (9 / 10.0 * width), (int) (1 / 10.0 * height));
        Log.d(TAG, "fillFlagColors: oben rechts");
        Log.d(TAG, "fillFlagColors: " + Color.red(colorMatrix[0][2]));
        Log.d(TAG, "fillFlagColors: " + Color.green(colorMatrix[0][2]));
        Log.d(TAG, "fillFlagColors: " + Color.blue(colorMatrix[0][2]));
        Log.d(TAG, "fillFlagColors: pixel: " + (int) (9 / 10.0 * width) + ", " + (int) (1 / 10.0 * height));

//        colorMatrix[0][3] = flagImg.getPixel((int)(9/10.0 * width), (int)(1/10.0 * height));

        colorMatrix[1][0] = flagImg.getPixel((int) (1 / 10.0 * width), (int) (5 / 10.0 * height));
        colorMatrix[1][1] = flagImg.getPixel((int) (5 / 10.0 * width), (int) (5 / 10.0 * height));
        colorMatrix[1][2] = flagImg.getPixel((int) (9 / 10.0 * width), (int) (5 / 10.0 * height));

//        colorMatrix[1][3] = flagImg.getPixel((int)(9/10.0 * width), (int)(5/10.0 * height));

        colorMatrix[2][0] = flagImg.getPixel((int) (1 / 10.0 * width), (int) (9 / 10.0 * height));
        Log.d(TAG, "fillFlagColors: unten links");
        Log.d(TAG, "fillFlagColors: " + Color.red(colorMatrix[2][0]));
        Log.d(TAG, "fillFlagColors: " + Color.green(colorMatrix[2][0]));
        Log.d(TAG, "fillFlagColors: " + Color.blue(colorMatrix[2][0]));
        Log.d(TAG, "fillFlagColors: pixel: " + (int) (1 / 10.0 * width) + ", " + (int) (9 / 10.0 * height));

        colorMatrix[2][1] = flagImg.getPixel((int) (5 / 10.0 * width), (int) (9 / 10.0 * height));
        colorMatrix[2][2] = flagImg.getPixel((int) (9 / 10.0 * width), (int) (9 / 10.0 * height));
        Log.d(TAG, "fillFlagColors: unten rechts");
        Log.d(TAG, "fillFlagColors: " + Color.red(colorMatrix[2][2]));
        Log.d(TAG, "fillFlagColors: " + Color.green(colorMatrix[2][2]));
        Log.d(TAG, "fillFlagColors: " + Color.blue(colorMatrix[2][2]));
        Log.d(TAG, "fillFlagColors: pixel: " + (int) (9 / 10.0 * width) + ", " + (int) (9 / 10.0 * height));
//        colorMatrix[2][3] = flagImg.getPixel((int)(9/10.0 * width), (int)(9/10.0 * height));

        flag.setColorValues(colorMatrix);

        // top horizontal check
        flag.setTopHorizontalEqual(ColorHelper.isEqualColor(colorMatrix[0][0], colorMatrix[0][2], threshold));

        // bottom horizontal check
        flag.setBottomHorizontalEqual(ColorHelper.isEqualColor(colorMatrix[2][0], colorMatrix[2][2], threshold));

        // start vertical check
        flag.setStartVerticalEqual(ColorHelper.isEqualColor(colorMatrix[0][0], colorMatrix[2][0], threshold));

        // end vertical check
        flag.setEndVerticalEqual(ColorHelper.isEqualColor(colorMatrix[0][2], colorMatrix[2][2], threshold));

        return flag;
    }


}
