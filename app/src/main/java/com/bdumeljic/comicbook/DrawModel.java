package com.bdumeljic.comicbook;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.ArrayList;

import static com.bdumeljic.comicbook.R.color.accent;

public class DrawModel {
    static String TAG = "DrawModel";

    Context context;

    ArrayList<Line> preBeaLines;
    ArrayList<Circle> preBeaCircles;

    ArrayList<Line> beaLines;
    ArrayList<Circle> beaCircles;

    ArrayList<Path> blackPaths;
    ArrayList<Point> pathPoints;
    Path currentPath;

    Paint blackPaint, selectedPaint;

    public Point mLineStart;
    public Point mLineEnd;

    public Point mCircleCenter;
    public float mCircleRadius = -1;

    boolean isBeautified = false;

    public DrawModel(Context context) {
        this.context = context;

        this.currentPath = new Path();
        this.blackPaths = new ArrayList<Path>();

        this.preBeaLines = new ArrayList<Line>();
        this.preBeaCircles = new ArrayList<Circle>();
        this.beaLines = new ArrayList<Line>();
        this.beaCircles = new ArrayList<Circle>();

        this.pathPoints = new ArrayList<Point>();

        this.blackPaint = new Paint();
        blackPaint.setAntiAlias(true);
        blackPaint.setStyle(Paint.Style.STROKE);
        blackPaint.setStrokeJoin(Paint.Join.ROUND);
        blackPaint.setStrokeCap(Paint.Cap.ROUND);
        blackPaint.setStrokeWidth(4f);
        blackPaint.setColor(Color.BLACK);

        this.selectedPaint = new Paint();
        selectedPaint.setAntiAlias(true);
        selectedPaint.setStyle(Paint.Style.STROKE);
        selectedPaint.setStrokeJoin(Paint.Join.ROUND);
        selectedPaint.setStrokeCap(Paint.Cap.ROUND);
        selectedPaint.setStrokeWidth(4f);
        selectedPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        selectedPaint.setColor(accent);
        selectedPaint.setStrokeWidth(6f);
    }

    public boolean drawPath(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                pathPoints.add(new Point((int)x,(int)y));
                touch_start(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                pathPoints.add(new Point((int)x,(int)y));
                touch_move(x, y);
                break;
            case MotionEvent.ACTION_UP:
                pathPoints.add(new Point((int)x,(int)y));
                touch_up();
                simplifyPath();
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

        preBeaLines.add(new Line(mLineStart, mLineEnd, blackPaint));

        mLineStart = null;
        mLineEnd = null;
        currentPath = new Path();
    }

    public void computeCircle(RectF bounds){
        mCircleCenter = new Point ((int) bounds.centerX(), (int) bounds.centerY());
        mCircleRadius = bounds.height() > bounds.width() ? bounds.width()/2 : bounds.height()/2;

        preBeaCircles.add(new Circle(mCircleCenter, mCircleRadius, blackPaint));

        preBeaLines.remove(preBeaLines.size() - 1);

        mCircleCenter = null;
        mCircleRadius = -1;
    }

    public void simplifyPath() {
        Log.d(TAG, "Pathpoints before : " + pathPoints.size());
        pathPoints = Douglas_Peucker_Algorithm.reduceWithTolerance(pathPoints, 80);
        Log.d(TAG, "Pathpoints after : " + pathPoints.size());
    }

    public void beautify() {
        currentPath = new Path();
        blackPaths.clear();
        pathPoints.clear();

        beaLines.addAll(preBeaLines);
        beaCircles.addAll(preBeaCircles);

        preBeaLines.clear();
        preBeaCircles.clear();

        isBeautified = true;
    }

    public void clear() {
        currentPath = new Path();
        blackPaths.clear();
        pathPoints.clear();

        beaLines.clear();
        beaCircles.clear();
        preBeaLines.clear();
        preBeaCircles.clear();

        mLineEnd = null;
        mLineStart = null;

        mCircleCenter = null;
        mCircleRadius = -1;

        isBeautified = false;

        Toast.makeText(context, "Cleared drawings", Toast.LENGTH_SHORT).show();
    }
}
