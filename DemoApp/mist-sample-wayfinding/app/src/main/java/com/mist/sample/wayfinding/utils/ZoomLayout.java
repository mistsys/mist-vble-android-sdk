package com.mist.sample.wayfinding.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;

public class ZoomLayout extends RelativeLayout implements ScaleGestureDetector.OnScaleGestureListener {

    private enum Mode {
        NONE,
        DRAG,
        ZOOM
    }

    private static final String TAG = "ZoomLayout";
    private static final float MIN_ZOOM = 1.0f;
    private static final float MAX_ZOOM = 16.0f;

    private Mode mode = Mode.NONE;
    private float scale = 1.0f;
    private float lastScaleFactor = 0f;

    // Where the finger first  touches the screen
    private float startX = 0f;
    private float startY = 0f;

    // How much to translate the canvas
    private float dx = 0f;
    private float dy = 0f;
    private float prevDx = 0f;
    private float prevDy = 0f;
    private ZoomViewTouchListener zoomViewTouchListener;
    float maxDx;
    float maxDy;


    private boolean isTouchListenerEnabled;

    public ZoomLayout(Context context) {
        super(context);
        init(context);
    }

    public ZoomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ZoomLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public void setListener(ZoomViewTouchListener zoomViewTouchListener) {
        this.zoomViewTouchListener = zoomViewTouchListener;
    }

    private void init(Context context) {
        final ScaleGestureDetector scaleDetector = new ScaleGestureDetector(context, this);
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        Log.i(TAG, "DOWN");
                        if (scale > MIN_ZOOM) {
                            mode = Mode.DRAG;
                            startX = motionEvent.getX() - prevDx;
                            startY = motionEvent.getY() - prevDy;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mode == Mode.DRAG) {
                            dx = motionEvent.getX() - startX;
                            dy = motionEvent.getY() - startY;
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        mode = Mode.ZOOM;
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = Mode.DRAG;
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.i(TAG, "UP");
                        mode = Mode.NONE;
                        prevDx = dx;
                        prevDy = dy;
                        isTouchListenerEnabled = true;
                        break;
                }
                scaleDetector.onTouchEvent(motionEvent);

                if ((mode == Mode.DRAG && scale >= MIN_ZOOM) || mode == Mode.ZOOM) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    maxDx = (child().getWidth() - (child().getWidth() / scale)) / 2 * scale;
                    maxDy = (child().getHeight() - (child().getHeight() / scale)) / 2 * scale;
                    dx = Math.min(Math.max(dx, -maxDx), maxDx);
                    dy = Math.min(Math.max(dy, -maxDy), maxDy);
                    Log.i(TAG, "Width: " + child().getWidth() + ", scale " + scale + ", dx " + dx
                            + ", max " + maxDx);
                    Log.i(TAG, "Height: " + child().getHeight() + ", scale " + scale + ", dy " + dy
                            + ", max " + maxDy);
                    applyScaleAndTranslation();
                } else if (mode == Mode.NONE)// && MIN_ZOOM ==scale
                {
                    if (isTouchListenerEnabled && zoomViewTouchListener != null) {

                        isTouchListenerEnabled = false;

                        if (scale == MIN_ZOOM)
                            zoomViewTouchListener.onTouchZoomView(motionEvent.getX(), motionEvent.getY());
                        else {

                            float finalXvalue, finalYvalue;

                            if (dx == 0) {
                                finalXvalue = maxDx + motionEvent.getX();
                            } else if (dx == maxDx) {
                                finalXvalue = motionEvent.getX();
                            } else if (dx > 0) {
                                finalXvalue = maxDx - dx + motionEvent.getX();
                            } else if (dx < 0) {
                                finalXvalue = maxDx + (dx * -1) + motionEvent.getX();
                            } else {
                                finalXvalue = motionEvent.getX();
                            }

                            if (dy == 0) {
                                finalYvalue = maxDy + motionEvent.getY();
                            } else if (dy == maxDy) {
                                finalYvalue = motionEvent.getY();
                            } else if (dy > 0) {
                                finalYvalue = maxDy - dy + motionEvent.getY();
                            } else if (dy < 0) {
                                finalYvalue = maxDy + (dy * -1) + motionEvent.getY();
                            } else {
                                finalYvalue = motionEvent.getY();
                            }

                            zoomViewTouchListener.onTouchZoomView(finalXvalue / scale, finalYvalue / scale);
                        }
                    }

                }

                return true;
            }
        });
    }

    // ScaleGestureDetector

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleDetector) {
        Log.i(TAG, "onScaleBegin");
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleDetector) {
        float scaleFactor = scaleDetector.getScaleFactor();
        Log.i(TAG, "onScale" + scaleFactor);
        if (lastScaleFactor == 0 || (Math.signum(scaleFactor) == Math.signum(lastScaleFactor))) {
            scale *= scaleFactor;
            scale = Math.max(MIN_ZOOM, Math.min(scale, MAX_ZOOM));
            lastScaleFactor = scaleFactor;
        } else {
            lastScaleFactor = 0;
        }
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleDetector) {
        Log.i(TAG, "onScaleEnd");
    }

    private void applyScaleAndTranslation() {
        if (zoomViewTouchListener != null)
            zoomViewTouchListener.onZoomScaleValue(scale);
        setScale(child());
    }

    private View child() {
        return getChildAt(0);
    }

    private void setScale(View v) {
        v.setScaleX(scale);
        v.setScaleY(scale);
        v.setTranslationX(dx);
        v.setTranslationY(dy);
    }

    /**
     * Zooming view touch listener interface.
     */
    public interface ZoomViewTouchListener {

        void onTouchZoomView(float x, float y);

        void onZoomScaleValue(float scale);
    }
}
