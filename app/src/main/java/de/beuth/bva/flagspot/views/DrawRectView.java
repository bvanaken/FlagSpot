package de.beuth.bva.flagspot.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.opencv.core.Rect;

/**
 * Created by Betty van Aken on 07/07/16.
 */

public class DrawRectView extends View {

    private Paint mRectPaint;

    private int mStartX = 0;
    private int mStartY = 0;
    private int mEndX = 0;
    private int mEndY = 0;
    private boolean mDrawRect = false;

    private OnUpCallback mCallback = null;

    public interface OnUpCallback {
        void onRectFinished(Rect rect);
    }

    public DrawRectView(final Context context) {
        super(context);
        init();
    }

    public DrawRectView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawRectView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * Sets callback for up
     *
     * @param callback {@link OnUpCallback}
     */
    public void setOnUpCallback(OnUpCallback callback) {
        mCallback = callback;
    }

    /**
     * Inits internal data
     */
    private void init() {
        this.setBackgroundColor(Color.TRANSPARENT);
        mRectPaint = new Paint();
        mRectPaint.setColor(Color.GREEN);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(5);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDrawRect = false;
                mStartX = (int) event.getX();
                mStartY = (int) event.getY();
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                final int x = (int) event.getX();
                final int y = (int) event.getY();

                if (!mDrawRect || Math.abs(x - mEndX) > 5 || Math.abs(y - mEndY) > 5) {
                    mEndX = x;
                    mEndY = y;
                    invalidate();
                }

                mDrawRect = true;
                break;

            case MotionEvent.ACTION_UP:
                if (mCallback != null) {
                    mCallback.onRectFinished(new Rect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
                            Math.abs(mStartX - mEndX), Math.abs(mStartY - mEndY)));
                }
                invalidate();
                mDrawRect = false;

                break;

            default:
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (mDrawRect) {
            canvas.drawRect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
                    Math.max(mEndX, mStartX), Math.max(mEndY, mStartY), mRectPaint);
        }
    }
}