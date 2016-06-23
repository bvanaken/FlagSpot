package de.beuth.bva.flagspot;

/**
 * Created by Betty van Aken on 18/06/16.
 */

import java.io.Serializable;
import java.util.Arrays;

public class Flag implements Serializable {

    static final long serialVersionUID = 1006920636116702072L;

    public int neededArrayCheck = 0;
    public int didntNeedArrayCheck = 0;

    String name;

    boolean topHorizontalEqual;

    boolean bottomHorizontalEqual;
    boolean startVerticalEqual;
    boolean endVerticalEqual;

    int[][] colorValues;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isTopHorizontalEqual() {
        return topHorizontalEqual;
    }

    public void setTopHorizontalEqual(boolean topHorizontalEqual) {
        this.topHorizontalEqual = topHorizontalEqual;
    }

    public boolean isStartVerticalEqual() {
        return startVerticalEqual;
    }

    public void setStartVerticalEqual(boolean startVerticalEqual) {
        this.startVerticalEqual = startVerticalEqual;
    }

    public boolean isBottomHorizontalEqual() {
        return bottomHorizontalEqual;
    }

    public void setBottomHorizontalEqual(boolean bottomHorizontalEqual) {
        this.bottomHorizontalEqual = bottomHorizontalEqual;
    }

    public boolean isEndVerticalEqual() {
        return endVerticalEqual;
    }

    public void setEndVerticalEqual(boolean endVerticalEqual) {
        this.endVerticalEqual = endVerticalEqual;
    }

    public int[][] getColorValues() {
        return colorValues;
    }

    public void setColorValues(int[][] colorValues) {
        this.colorValues = colorValues;
    }

    public String toString() {
        return name + " {" + "\n" +
                "topHorizontalEqual: " + topHorizontalEqual + "\n" +
                "bottomHorizontalEqual: " + bottomHorizontalEqual + "\n" +
                "startVerticalEqual: " + startVerticalEqual + "\n" +
                "endVerticalEqual: " + endVerticalEqual + "\n" +
                "colorValues: " + colorValues + "" + "\n" +
                "}";
    }

    public boolean hasEqualValues(Flag flag) {
        if (topHorizontalEqual == flag.isTopHorizontalEqual()
                && bottomHorizontalEqual == flag.isBottomHorizontalEqual()
                && startVerticalEqual == flag.isStartVerticalEqual()
                && endVerticalEqual == flag.isEndVerticalEqual()) {
            neededArrayCheck++;
            return Arrays.deepEquals(colorValues, flag.getColorValues());
        }
        didntNeedArrayCheck++;
        return false;
    }

    public boolean hasEqualValues(Flag flag, int threshold) {
        if (topHorizontalEqual == flag.isTopHorizontalEqual()
                && bottomHorizontalEqual == flag.isBottomHorizontalEqual()
                && startVerticalEqual == flag.isStartVerticalEqual()
                && endVerticalEqual == flag.isEndVerticalEqual()) {
            neededArrayCheck++;

            return averageColorDistanceToFlag(flag) < threshold;
        }
        didntNeedArrayCheck++;
        return false;
    }

    public double averageColorDistanceToFlag(Flag flag) {
        double totalDistance = 0;
        int distanceChecks = 0;
        for (int i = 0; i < colorValues.length; i++) {
            for (int j = 0; j < colorValues[0].length; j++) {

                totalDistance += ColorHelper.colorDistance(colorValues[i][j], flag.getColorValues()[i][j]);
                distanceChecks++;
            }
        }
        return totalDistance / distanceChecks;
    }
}

