package com.bdumeljic.comicbook;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

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

    /**
     * Handle drawing
     * @param event
     * @return
     */
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
                mLineStart = null;
                mLineEnd = null;
                currentPath = new Path();
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
    }

    /**
     * Called after a circle has been detected.
     * @param bounds
     */
    public void computeCircle(RectF bounds){
        mCircleCenter = new Point ((int) bounds.centerX(), (int) bounds.centerY());
        mCircleRadius = bounds.height() > bounds.width() ? bounds.width()/2 : bounds.height()/2;

        preBeaCircles.add(new Circle(mCircleCenter, mCircleRadius, blackPaint));

        // Remove faulty line at the end of circle drawing. If there was any.
        if (preBeaLines.size() > 0) {
            preBeaLines.remove(preBeaLines.size() - 1);
        }

        mCircleCenter = null;
        mCircleRadius = -1;
    }

    /**
     * Remove sketch lines.
     * Draw the neat lines and circles.
     * Apply the constraints.
     */
    public void beautify() {
        currentPath = new Path();
        blackPaths.clear();
        pathPoints.clear();

        beaLines.addAll(preBeaLines);
        beaCircles.addAll(preBeaCircles);

        preBeaLines.clear();
        preBeaCircles.clear();

        checkConnectingLines();
        checkNinetyDegrees();


        isBeautified = true;
    }

    /**
     * Checks for the constraint that lines drawn close together are supposed to be connected.
     */
    public void checkConnectingLines() {
        Line currentLine;

        for(int i = 0; i < beaLines.size(); i++) {
            currentLine = beaLines.get(i);
            for(int j = i + 1; j < beaLines.size(); j++) {
                Line lineToCompare = beaLines.get(j);
                Map list = currentLine.compare(lineToCompare);
                if (list == null) Log.d(TAG, "found NO lines to connect");
                else {
                    //Log.d(TAG, "found a line to connect");
                    Point currentPoint;

                    if(list.get("p1") == 0) currentPoint = currentLine.start;
                    else currentPoint = currentLine.end;

                    if (list.get("p2") == 0) lineToCompare.update(0, currentPoint);
                    else lineToCompare.update(1, currentPoint);
                }
            }
        }
    }

    /**
     * Apply 90 degrees constraints to lines
     */
    public void checkNinetyDegrees() {
        for(int i = 0; i < beaLines.size() - 1; i++) {
            for(int h = i; h < beaLines.size() - 1; h++) {
                //check for intersection and 90 degrees
                Point intersection = intersectLines(beaLines.get(i), beaLines.get(h));
                if (intersection != null) {
                    //cut of overreaching ends from intersecting lines if they are under the threshold
                    for (int j = 0; j < 2; j++) {
                        double distStartPt = distanceBetweenPoints(intersection, beaLines.get(i + j).getStartPoint());
                        double distEndPt = distanceBetweenPoints(intersection, beaLines.get(i + j).getEndPoint());

                        if (distStartPt < distEndPt && Math.abs(distStartPt) < 56) {
                            beaLines.get(i + j).setStartPoint(intersection);
                        } else if (Math.abs(distEndPt) < 56) {
                            beaLines.get(i + j).setEndPoint(intersection);
                        }
                    }

                    //set the point furthest away from the intersection point to the new value so the line is perpendicular
                    double diffAngle = angleBetween2Lines(beaLines.get(i), beaLines.get(h)) - 90;
                    if (Math.abs(diffAngle) < 5 && Math.abs(diffAngle) > 0.0) {
                        //compute distance of start and end point of line to intersection point
                        double distStartPt = distanceBetweenPoints(intersection, beaLines.get(h).getStartPoint());
                        double distEndPt = distanceBetweenPoints(intersection, beaLines.get(h).getEndPoint());
                        double angle = Math.toRadians(diffAngle);
                        //compute vector of first line intersection point is on to know shift direction
                        Point vectorLine = computeVectorOfLine(beaLines.get(i));
                        double normalizedVectorX = vectorLine.x / lengthOfVector(vectorLine);
                        double normalizedVectorY = vectorLine.y / lengthOfVector(vectorLine);

                        if (distStartPt < distEndPt) {
                            beaLines.get(h).setEndPoint(new Point((int) (beaLines.get(h).getEndPoint().x + shiftValue(beaLines.get(h), angle) * normalizedVectorX), (int) (beaLines.get(h).getEndPoint().y + shiftValue(beaLines.get(h), angle) * normalizedVectorY)));
                        } else {
                            beaLines.get(h).setStartPoint(new Point((int) (beaLines.get(h).getStartPoint().x + shiftValue(beaLines.get(h), angle) * normalizedVectorX), (int) (beaLines.get(h).getStartPoint().y + shiftValue(beaLines.get(h), angle) * normalizedVectorY)));
                        }
                        Log.d(TAG, "new Point set to 90 degrees");
                        Log.d(TAG, "ShiftValue: " + shiftValue(beaLines.get(h), angle));
                        Log.d(TAG, "Vector pit of Line: " + vectorLine);
                        Log.d(TAG, "Normalized vector: " + normalizedVectorX + " , " + normalizedVectorY);
                    }

                }
            }
        }
    }

    /**
     * Split long lines with curves into sepreate lines.
     * @param simplifiedPath
     */
    public void computeVectorDirections(ArrayList<Point> simplifiedPath) {
        int pointStep = 1;
        Point lastTurn = new Point();
        simplifiedPath = Douglas_Peucker_Algorithm.reduceWithTolerance(simplifiedPath, 0.01);
        lastTurn = simplifiedPath.get(0);
        ArrayList<Point> directionVectors = new ArrayList<Point>();

        //simplification of path
        if (simplifiedPath.size() > 1) {
            for (int i = 0; i < simplifiedPath.size() - 1; i = i + pointStep) {
                if (i + pointStep < simplifiedPath.size()) {
                    directionVectors.add(new Point(simplifiedPath.get(i + pointStep).x - simplifiedPath.get(i).x, simplifiedPath.get(i + pointStep).y - simplifiedPath.get(i).y));
                } else {
                    directionVectors.add(new Point(simplifiedPath.get(simplifiedPath.size() - 1).x - simplifiedPath.get(i).x, simplifiedPath.get(simplifiedPath.size() - 1).y - simplifiedPath.get(i).y));
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
            newPathPoints.add(simplifiedPath.get(simplifiedPath.size() - 1));
            newPathPoints = Douglas_Peucker_Algorithm.reduceWithTolerance(newPathPoints, 70);

            // Create lines out of new point array
            for (int i = 0; i < newPathPoints.size() - 1; i++) {
                preBeaLines.add(new Line(newPathPoints.get(i), newPathPoints.get(i + 1), blackPaint));
            }

            // If there was only one line and nothing to simplify, make only that one line
            if (newPathPoints.size() < 2 && mLineStart != null && mLineEnd != null) {
                preBeaLines.add(new Line(mLineStart, mLineEnd, blackPaint));
            }
        }
    }

    /**
     * Return how much to shift a line to make it 90 degrees to another line.
      * @param line
     * @param angle
     * @return
     */
    public double shiftValue(Line line, double angle) {
        return line.length * Math.sin(angle);
    }

    /**
     * Calculate the distance between two points.
     * @param one
     * @param two
     * @return
     */
    public double distanceBetweenPoints(Point one, Point two) {
        return Math.sqrt((Math.abs(one.x - two.x) * Math.abs(one.x - two.x)) + (Math.abs(one.y - two.y) * Math.abs(one.y - two.y)));
    }

    public Point computeVectorOfLine(Line line) {
        return new Point(line.getEndPoint().x - line.getStartPoint().x, line.getEndPoint().y - line.getStartPoint().y);
    }

    /**
     * Compute the angle between two lines
     * @param line1
     * @param line2
     * @return
     */
    public double angleBetween2Lines(Line line1, Line line2) {
        return computeAngle(computeVectorOfLine(line1), computeVectorOfLine(line2));
    }

    public double computeAngle(Point vector1, Point vector2){
        double dot = dot(vector1, vector2);
        double lengths = lengthOfVector(vector1) * lengthOfVector(vector2);
        return Math.acos(dot/lengths)* 180/Math.PI;
    }

    public double lengthOfVector(Point vector){
        return  Math.sqrt(vector.x * vector.x + vector.y * vector.y);
    }

    /**
     * Compute the dot product of two vectors
     *
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

    /**
     * Check if two lines intersect
     *
     * @param line1
     * @param line2
     * @return The intersection point
     */
    public Point intersectLines(Line line1,Line line2){
        double line1startX = line1.getStartPoint().x;
        double line1endX = line1.getEndPoint().x;
        double line2startX = line2.getStartPoint().x;
        double line2endX = line2.getEndPoint().x;
        double line1startY = line1.getStartPoint().y;
        double line1endY = line1.getEndPoint().y;
        double line2startY = line2.getStartPoint().y;
        double line2endY = line2.getEndPoint().y;


        double zx = (line1startX * line1endY - line1startY * line1endX)*(line2startX-line2endX) - (line1startX - line1endX) * (line2startX * line2endY - line2startY * line2endX);
        double zy = (line1startX * line1endY - line1startY * line1endX)*(line2startY-line2endY) - (line1startY - line1endY) * (line2startX * line2endY - line2startY * line2endX);


        double n = (line1startX - line1endX) * (line2startY - line2endY) - (line1startY - line1endY) * (line2startX - line2endX);

        // Coordinates of Intersection
        double x = zx/n;
        double y = zy/n;

        // division by zero ??
        if (Double.isNaN(x)& Double.isNaN(y))
        {
            return null;
        }
        // Intersection point on line segment?
        if ((x - line1startX) / (line1endX - line1startX) > 1 || (x - line2startX) / (line2endX - line2startX) > 1 || (y - line1startY) / (line1endY - line1startY) > 1 || (y - line2startY) / (line2endY - line2startY) > 1 )
        {
            return null;
        }
        return new Point((int) x,(int) y);
    }

    /**
     * Remove all drawings from the canvas.
     */
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
}
