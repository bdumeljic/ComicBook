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

import java.lang.reflect.Array;
import java.util.ArrayList;

import static com.bdumeljic.comicbook.R.color.accent;

public class DrawModel {
    static String TAG = "DrawModel";

    Context context;

    ArrayList<Path> blackPaths;
    ArrayList<Point> pathPoints = new ArrayList<Point>();
    Path currentPath;

    ArrayList<Line> lineSegments = new ArrayList<>();
    ArrayList<Point> newPathPoints = new ArrayList<Point>();

    Paint blackPaint, selectedPaint, lineSegmentPaint;

    public Point mLineStart;
    public Point mLineEnd;

    public Point mCircleCenter;
    public float mCircleRadius = -1;

    public DrawModel(Context context) {
        this.context = context;

        this.currentPath = new Path();
        this.blackPaths = new ArrayList<Path>();

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

        this.lineSegmentPaint = new Paint();
        lineSegmentPaint.setAntiAlias(true);
        lineSegmentPaint.setStyle(Paint.Style.STROKE);
        lineSegmentPaint.setStrokeJoin(Paint.Join.ROUND);
        lineSegmentPaint.setStrokeCap(Paint.Cap.ROUND);
        lineSegmentPaint.setStrokeWidth(4f);
        lineSegmentPaint.setColor(Color.RED);
    }

    public boolean drawPath(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                pathPoints.clear();
                newPathPoints.clear();
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
                computeVectorDirections(pathPoints);
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

    public void beautify() {
    }

    public void clear() {
        currentPath = new Path();
        blackPaths.clear();
        lineSegments.clear();
        mLineEnd = null;
        mLineStart = null;
        mCircleCenter = null;
        mCircleRadius = -1;

        Toast.makeText(context, "Cleared drawings", Toast.LENGTH_SHORT).show();
    }

    public void computeVectorDirections(ArrayList<Point> simplifiedPath){
        int pointStep = 1;
        Point lastTurn = new Point();
        simplifiedPath = Douglas_Peucker_Algorithm.reduceWithTolerance(simplifiedPath, 0.01);
        lastTurn = simplifiedPath.get(0);
        ArrayList<Point> directionVectors = new ArrayList<Point>();

        if (simplifiedPath.size() > 1) {
            for(int i = 0; i < simplifiedPath.size() - 1; i = i + pointStep) {
                if(i+pointStep < simplifiedPath.size()) {
                    directionVectors.add(new Point(simplifiedPath.get(i + pointStep).x - simplifiedPath.get(i).x, simplifiedPath.get(i + pointStep).y - simplifiedPath.get(i).y));
                }else{
                    directionVectors.add(new Point(simplifiedPath.get(simplifiedPath.size()-1).x - simplifiedPath.get(i).x, simplifiedPath.get(simplifiedPath.size() - 1).y - simplifiedPath.get(i).y));
                }
                if (directionVectors.size() > 1) {
                    double angle = dot(directionVectors.get(directionVectors.size() - 2), directionVectors.get(directionVectors.size() - 1));
                    Log.d(TAG, "Angle: " + angle);
                    if (Math.abs(angle) < 160) {
                        //add as new line as turning point detected, line defined by last turning point and new detected turning point
                        /*Line line = new Line(lastTurn, simplifiedPath.get(i));
                        lineSegments.add(line);*/
                        newPathPoints.add(lastTurn);
                        newPathPoints.add(simplifiedPath.get(i));
                        lastTurn = simplifiedPath.get(i);
                    }
                }
            }
            //lineSegments.add(new Line(lastTurn, simplifiedPath.get(simplifiedPath.size()-1)));
            newPathPoints.add(simplifiedPath.get(simplifiedPath.size()-1));
            newPathPoints = Douglas_Peucker_Algorithm.reduceWithTolerance(newPathPoints, 30);


        }

    }

    public double computeAngle(Point start, Point end){
        Log.d(TAG, "Vectors: " + start + " , " + end);
        return Math.toDegrees(Math.atan2(start.y - end.y, start.x - end.x));
    }

    public double computeGradient(Point vector1, Point vector2){
        if(vector2.x - vector1.x == 0){
            return 0;
        }
        return (vector2.y - vector1.y) / (vector2.x - vector1.x);
    }

    public double LengthOfLine(Line line){
        return Math.sqrt((line.getEndPoint().x- line.getStartPoint().x)*(line.getEndPoint().x- line.getStartPoint().x) + (line.getEndPoint().y- line.getStartPoint().y)*(line.getEndPoint().y - line.getStartPoint().y));
    }
    /**
     * Compute the dot product of two vectors
     * @param v1 The first vector
     * @param v2 The second vector
     * @return v1 dot v2
     **/
    public static float dot(Point v1, Point v2) {
        float res = 0;
        res += v1.x * v2.x;
        res += v1.y * v2.y;

        return res;
    }
}
