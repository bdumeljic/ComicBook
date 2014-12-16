package com.bdumeljic.comicbook;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;

import static com.bdumeljic.comicbook.R.color.accent_lighter;

public class DrawModel {
    static String TAG = "DrawModel";

    ArrayList<Path> blackPaths;
    Path currentPath;

    static Paint blackPaint, selectedPaint;

    public Point mLineStart;
    public Point mLineEnd;

    public Point mCircleCenter;
    public float mCircleRadius = -1;

    public DrawModel() {
        currentPath = new Path();
        blackPaths = new ArrayList<Path>();

        blackPaint = new Paint();
        blackPaint.setAntiAlias(true);
        blackPaint.setStyle(Paint.Style.STROKE);
        blackPaint.setStrokeJoin(Paint.Join.ROUND);
        blackPaint.setStrokeCap(Paint.Cap.ROUND);
        blackPaint.setStrokeWidth(4f);
        blackPaint.setColor(Color.BLACK);

        selectedPaint = new Paint();
        selectedPaint.setAntiAlias(true);
        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeJoin(Paint.Join.ROUND);
        selectedPaint.setStrokeCap(Paint.Cap.ROUND);
        selectedPaint.setStrokeWidth(4f);
        selectedPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        selectedPaint.setColor(accent_lighter);
        selectedPaint.setStrokeWidth(6f);
    }

    public boolean drawPath(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                break;
        }

        return true;
    }


    private float mX, mY;
    /** Minimum touch distance */
    private static final float TOUCH_TOLERANCE = 4;

    /**
     * Start a touch event from the provided location.
     * </p>
     * Reset the undone paths array when drawing is resumed.
     * @param x X value of touch starting point
     * @param y Y value of touch starting point
     */
    public void touch_start(float x, float y) {
        currentPath.reset();
        currentPath.moveTo(x, y);
        mLineStart = new Point((int)x, (int)y);
        mX = x;
        mY = y;
    }

    /**
     * Handle a move event. Continue drawing the path.
     * @param x
     * @param y
     */
    public void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            currentPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    /**
     * Handle the end of a touch or draw.
     */
    public void touch_up() {
        currentPath.lineTo(mX, mY);
        blackPaths.add(currentPath);
        mLineEnd = new Point((int)mX, (int)mY);

        currentPath = new Path();
    }

    public void computeCircle(RectF bounds){
        mCircleCenter = new Point ((int)bounds.centerX(), (int)bounds.centerY());

        mCircleRadius = bounds.height() > bounds.width() ? bounds.width()/2 : bounds.height()/2;
    }
}
