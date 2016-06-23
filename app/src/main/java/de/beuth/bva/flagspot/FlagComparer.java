package de.beuth.bva.flagspot;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by betty on 18/06/16.
 */
public class FlagComparer {

    private static final String TAG = "FlagComparer";

    static List<Flag> flags;
    static Flag compareFlag;

    public static void compareFlag(Context context, Bitmap flagImg) {
        if (flags == null) {
            setupFlagList(context);
        }

        compareFlag = new Flag();
        compareFlag.setName("Scanned Flag");
        compareFlag = FlagIdentifier.fillFlagColors(flagImg, compareFlag, 200);

        compareWithFlagList();
    }

    private static void compareWithFlagList() {

        List<Flag> matches = new ArrayList<>();

        int arrayChecks = 0;
        int noArrayChecks = 0;

        for (Flag flag : flags) {
            if (compareFlag.hasEqualValues(flag, 200)) {
                matches.add(flag);
            }
        }
        arrayChecks += compareFlag.neededArrayCheck;
        noArrayChecks += compareFlag.didntNeedArrayCheck;

        System.out.println(arrayChecks + " array checks. " + noArrayChecks + " without.");

        sortListByColorDistance(matches, compareFlag);
        for (Flag flagMatch : matches) {
            System.out.println(compareFlag.getName() + " has equal values as " + flagMatch.getName());
        }
    }

    private static List<Flag> sortListByColorDistance(List<Flag> list, final Flag compareFlag) {

        Collections.sort(list, new Comparator<Flag>() {
            @Override
            public int compare(Flag f1, Flag f2) {
                double averageDist1 = f1.averageColorDistanceToFlag(compareFlag);
                double averageDist2 = f2.averageColorDistanceToFlag(compareFlag);

                return averageDist1 > averageDist2 ? 1 : (averageDist2 > averageDist1) ? -1 : 0;
            }
        });

        return list;
    }

    private static void setupFlagList(Context context) {
        try {
            // Deserialize an List<Flag> Object
            AssetManager assetManager = context.getAssets();
            InputStream fileIn = assetManager.open("flags.txt");

            ObjectInputStream in = new ObjectInputStream(fileIn);
            flags = (List<Flag>) in.readObject();
            in.close();
            fileIn.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
