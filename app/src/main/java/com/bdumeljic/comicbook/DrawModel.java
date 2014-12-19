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

    ArrayList<Line> preBeaLines;
    ArrayList<Circle> preBeaCircles;

    ArrayList<Line> beaLines;
    ArrayList<Circle> beaCircles;

    ArrayList<Path> blackPaths;
    ArrayList<Point> pathPoints;
    Path currentPath;

    ArrayList<Line> lineSegments = new ArrayList<>();
    ArrayList<Point> newPathPoints = new ArrayList<Point>();

    Paint blackPaint, selectedPaint, lineSegmentPaint;

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

       // preBeaLines.add(new Line(mLineStart, mLineEnd, blackPaint));

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
        lineSegments.clear();
        mLineEnd = null;
        mLineStart = null;
        mCircleCenter = null;
        mCircleRadius = -1;
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

    public void computeVectorDirections(ArrayList<Point> simplifiedPath){
        int pointStep = 1;
        Point lastTurn = new Point();
        simplifiedPath = Douglas_Peucker_Algorithm.reduceWithTolerance(simplifiedPath, 0.01);
        lastTurn = simplifiedPath.get(0);
        ArrayList<Point> directionVectors = new ArrayList<Point>();

        //simplification of path
        if (simplifiedPath.size() > 1) {
            for(int i = 0; i < simplifiedPath.size() - 1; i = i + pointStep) {
                if(i+pointStep < simplifiedPath.size()) {
                    directionVectors.add(new Point(simplifiedPath.get(i + pointStep).x - simplifiedPath.get(i).x, simplifiedPath.get(i + pointStep).y - simplifiedPath.get(i).y));
                }else{
                    directionVectors.add(new Point(simplifiedPath.get(simplifiedPath.size()-1).x - simplifiedPath.get(i).x, simplifiedPath.get(simplifiedPath.size() - 1).y - simplifiedPath.get(i).y));
                }
                if (directionVectors.size() > 1) {
                    double angle = dot(directionVectors.get(directionVectors.size() - 2), directionVectors.get(directionVectors.size() - 1));
                    if (Math.abs(angle) < 160) {
                        newPathPoints.add(lastTurn);
                        newPathPoints.add(simplifiedPath.get(i));
                        lastTurn = simplifiedPath.get(i);
                    }
                }
            }
            newPathPoints.add(simplifiedPath.get(simplifiedPath.size()-1));
            newPathPoints = Douglas_Peucker_Algorithm.reduceWithTolerance(newPathPoints, 70);
            //create lines out of new point array
            for(int i = 0; i < newPathPoints.size() - 1; i++){
                preBeaLines.add(new Line(newPathPoints.get(i), newPathPoints.get(i+1), blackPaint));
            }
            //apply constraints to new pre breautified lines
            for(int i = 0; i < preBeaLines.size() - 1; i++) {
                //check for intersection and 90 degrees
                Point intersection = intersectLines(preBeaLines.get(i), preBeaLines.get(i+1));
                if(intersection != null){
                    //cut of overreaching ends from intersecting lines
                    for(int j = 0; j<2; j++){
                        double distStartPt = distanceBetweenPoints(intersection, preBeaLines.get(i+j).getStartPoint());
                        double distEndPt = distanceBetweenPoints(intersection, preBeaLines.get(i+j).getEndPoint());

                        if(distStartPt < distEndPt){
                            preBeaLines.get(i+j).setStartPoint(intersection);
                        }else {
                            preBeaLines.get(i+j).setEndPoint(intersection);
                        }
                    }

                    //set the point furthest away from the intersection point to the new value so the line is perpendicular
                    double diffAngle = angleBetween2Lines(preBeaLines.get(i), preBeaLines.get(i+1)) - 90;
                    if(Math.abs(diffAngle) < 10){
                        //compute distance of start and end point of line to intersection point
                        double distStartPt = distanceBetweenPoints(intersection, preBeaLines.get(i+1).getStartPoint());
                        double distEndPt = distanceBetweenPoints(intersection, preBeaLines.get(i+1).getEndPoint());
                        double angle = Math.toRadians(diffAngle);
                        //compute vector of first line intersection point is on to know shift direction
                        Point vectorLine = computeVectorOfLine(preBeaLines.get(i));
                        double normalizedVectorX = vectorLine.x/LengthOfVector(vectorLine);
                        double normalizedVectorY = vectorLine.y/LengthOfVector(vectorLine);

                        if(distStartPt < distEndPt){
                            preBeaLines.get(i+1).setEndPoint(new Point((int)(preBeaLines.get(i+1).getEndPoint().x + shiftValue(preBeaLines.get(i+1), angle) * normalizedVectorX),(int)(preBeaLines.get(i+1).getEndPoint().y + shiftValue(preBeaLines.get(i+1), angle ) * normalizedVectorY)));
                        }else {
                            preBeaLines.get(i+1).setStartPoint(new Point((int)(preBeaLines.get(i+1).getStartPoint().x + shiftValue(preBeaLines.get(i+1), angle) * normalizedVectorX),(int)(preBeaLines.get(i+1).getStartPoint().y + shiftValue(preBeaLines.get(i+1), angle ) * normalizedVectorY)));
                        }
                        Log.d(TAG, "new Point set to 90 degrees");
                        Log.d(TAG, "ShiftValue: "+ shiftValue(preBeaLines.get(i+1), angle));
                        Log.d(TAG, "Vector pit of Line: "+ vectorLine);
                        Log.d(TAG, "Normalized vector: "+ normalizedVectorX + " , " + normalizedVectorY);
                    }

                }
            }

        }

    }
    public double shiftValue(Line line, double angle){
        return LengthOfLine(line) * Math.sin(angle);
    }

    public double distanceBetweenPoints(Point p1, Point p2){
        return Math.sqrt((p2.x -p1.x) * (p2.x -p1.x)) + ((p2.y -p1.y) * (p2.y -p1.y));
    }

    public Point computeVectorOfLine(Line line){
        return new Point(line.getEndPoint().x - line.getStartPoint().x, line.getEndPoint().y - line.getStartPoint().y);
    }

    public double LengthOfLine(Line line){
        return Math.sqrt((line.getEndPoint().x- line.getStartPoint().x)*(line.getEndPoint().x- line.getStartPoint().x) + (line.getEndPoint().y- line.getStartPoint().y)*(line.getEndPoint().y - line.getStartPoint().y));
    }


    public double angleBetween2Lines(Line line1, Line line2)
    {
        return computeAngle(computeVectorOfLine(line1), computeVectorOfLine(line2));
    }

    public double computeAngle(Point vector1, Point vector2){
        double dot = dot(vector1, vector2);
        double lengths = LengthOfVector(vector1) * LengthOfVector(vector2);
        return Math.acos(dot/lengths)* 180/Math.PI;

    }

    public double LengthOfVector(Point vector){
        return  Math.sqrt(vector.x * vector.x + vector.y * vector.y);
    }

    public double slopeOfLine(Line line){
        return (line.getEndPoint().y - line.getStartPoint().y) / (line.getEndPoint().x - line.getStartPoint().x);
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

    public Point intersectLines(Line line1,Line line2){
        double x1 = line1.getStartPoint().x;
        double x2 = line1.getEndPoint().x;
        double x3 = line2.getStartPoint().x;
        double x4 = line2.getEndPoint().x;
        double y1 = line1.getStartPoint().y;
        double y2 = line1.getEndPoint().y;
        double y3 = line2.getStartPoint().y;
        double y4 = line2.getEndPoint().y;


        double zx = (x1 * y2 - y1 * x2)*(x3-x4) - (x1 - x2) * (x3 * y4 - y3 * x4);
        double zy = (x1 * y2 - y1 * x2)*(y3-y4) - (y1 - y2) * (x3 * y4 - y3 * x4);


        double n = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

        // Coordinates of Intersection
        double x = zx/n;
        double y = zy/n;

        // division by zero ??
        if (Double.isNaN(x)& Double.isNaN(y))
        {
            return null;
        }
        // Intersection point on line segment?
        if ((x - x1) / (x2 - x1) > 1 || (x - x3) / (x4 - x3) > 1 || (y - y1) / (y2 - y1) > 1 || (y - y3) / (y4 - y3) > 1 )
        {
            return null;
        }
        return new Point((int) x,(int) y);
    }
}
