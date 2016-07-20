package de.beuth.bva.flagspot;

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

public class DragStrokeView extends View {

    private Paint mPaint;

    private int mStartX = 0;
    private int mStartY = 0;
    private int mEndX = 0;
    private int mEndY = 0;
    private boolean mDrawStroke = false;

    private OnUpCallback mCallback = null;

    public interface OnUpCallback {
        void onStrokeFinished(Rect rect);
    }

    public DragStrokeView(final Context context) {
        super(context);
        init();
    }

    public DragStrokeView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DragStrokeView(final Context context, final AttributeSet attrs, final int defStyle) {
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
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5); // TODO: should take from resources
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDrawStroke = false;
                mStartX = (int) event.getX();
                mStartY = (int) event.getY();
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                final int x = (int) event.getX();
                final int y = (int) event.getY();

                if (!mDrawStroke || Math.abs(x - mEndX) > 5 || Math.abs(y - mEndY) > 5) {
                    mEndX = x;
                    mEndY = y;
                    invalidate();
                }

                mDrawStroke = true;
                break;

            case MotionEvent.ACTION_UP:
                if (mCallback != null) {
                    mCallback.onStrokeFinished(new Rect(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
                            Math.abs(mStartX - mEndX), Math.abs(mStartY - mEndY)));
                }
                invalidate();
                mDrawStroke = false;

                break;

            default:
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        if (mDrawStroke) {
            canvas.drawLine(Math.min(mStartX, mEndX), Math.min(mStartY, mEndY),
                    Math.max(mEndX, mStartX), Math.max(mEndY, mStartY), mPaint);
        }
    }
}