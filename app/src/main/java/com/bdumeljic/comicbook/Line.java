package com.bdumeljic.comicbook;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import java.util.HashMap;
import java.util.Map;

public class Line {

    Point start;
    Point end;
    Paint paint;
    double length;

    float TOLERANCE = 56;

    public Line(Point start, Point end, Paint paint) {
        this.start = start;
        this.end = end;
        this.paint = paint;

        this.length = dist(start, end);
    }

    public double dist(Point one, Point two) {
        return Math.sqrt((Math.abs(one.x - two.x) * Math.abs(one.x - two.x)) + (Math.abs(one.y - two.y) * Math.abs(one.y - two.y)));
    }

    public Point getStartPoint(){
        return this.start;
    }

    public void setStartPoint(Point start){
        this.start = start;
    }

    public Point getEndPoint(){
        return this.end;
    }

    public void setEndPoint(Point end) {
        this.end = end;
    }

    /**
     * Update the line with the new point provided.
     * @param point     Which point should be changed; 0 = start and 1 = end.
     * @param newPoint  The new point
     */
    public void update(int point, Point newPoint) {
        if (point == 0) this.start = newPoint;
        else this.end = newPoint;
    }

    /**
     * Determine the shortest distance between two lines. This line and line that was passed along as an argument.
     * @param otherLine
     * @return
     */
    public Map<String, Object> compare(Line otherLine) {
        // Check if the line is long enough
        if (this.length < TOLERANCE || otherLine.length < TOLERANCE) {
          return null;
        }

        double minDist = 10000;
        int p1 = -1;
        int p2 = -1;

        double currentDist = dist(this.start, otherLine.start);
        if (currentDist < minDist) {
            minDist = currentDist;
            p1 = 0;
            p2 = 0;
        }

        currentDist = dist(this.start, otherLine.end);
        if (currentDist < minDist) {
            minDist = currentDist;
            p1 = 0;
            p2 = 1;
        }

        currentDist = dist(this.end, otherLine.start);
        if (currentDist < minDist) {
            minDist = currentDist;
            p1 = 1;
            p2 = 0;
        }

        currentDist = dist(this.end, otherLine.end);
        if (currentDist < minDist) {
            minDist = currentDist;
            p1 = 1;
            p2 = 1;
        }

        if(minDist < TOLERANCE && minDist > 0.0) {
            Map<String, Object> result = new HashMap();
            result.put("minDist", minDist);
            result.put("p1", p1);
            result.put("p2", p2);
            //Log.d("LINE", "found: "  + result.toString());
            return result;
        }

        return  null;
    }

    public void draw(Canvas c) {
        c.drawLine(start.x, start.y, end.x, end.y, paint);
    }
}
