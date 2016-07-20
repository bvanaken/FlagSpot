package de.beuth.bva.flagspot.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by Betty van Aken on 19/07/16.
 */
public class PolyImageView extends ImageView {

    private static final String TAG = "PolyImageView";
    Matrix matrix = new Matrix();
    Bitmap bitmap;
    float[] cornerValues;

    public PolyImageView(Context context) {
        super(context);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
//        super.setImageBitmap(bm);
        bitmap = bm;
        invalidate();
        Log.d(TAG, "setImageBitmap:");
    }

    public void setCornerValues(float[] cornerVals){
        cornerValues = cornerVals;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(bitmap != null && cornerValues != null){

            matrix.setPolyToPoly(
                    new float[] {
                            0, 0,
                            bitmap.getWidth(), 0,
                            0, bitmap.getHeight(),
                            bitmap.getWidth(), bitmap.getHeight()
                    }, 0,
                    cornerValues, 0,
                    4);

            canvas.drawBitmap(bitmap, matrix, null);
            canvas.drawText("Test Text", cornerValues[0], cornerValues[1], null);

            Log.d(TAG, "onDraw: bitmap drawn");
        } else {
            Log.d(TAG, "onDraw: bitmap null");
        }
    }
}
