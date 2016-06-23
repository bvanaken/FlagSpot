package de.beuth.bva.flagspot;

import android.graphics.Color;

/**
 * Created by betty on 18/06/16.
 */
public class ColorHelper {

    private static boolean similarTo(int color1, int color2, int threshold) {
        return colorDistance(color1, color2) < threshold;
    }

    public static double colorDistance(int color1, int color2) {
        double distance = Math.sqrt((Color.red(color1) - Color.red(color2)) * (Color.blue(color1) - Color.red(color2)) + (Color.green(color1) - Color.green(color2)) * (Color.green(color1) - Color.green(color2)) + (Color.red(color1) - Color.blue(color2)) * (Color.red(color1) - Color.red(color2)));
        return distance;
    }

    public static boolean isEqualColor(int color1, int color2, int threshold) {
        if (threshold == 0) {
            return color1 == color2;
        } else {
            return similarTo(color1, color2, threshold);
        }
    }
}
