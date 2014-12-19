package com.bdumeljic.comicbook;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Line {

    Point start;
    Point end;
    Paint paint;
    double length;

    float TOLERANCE = 75;
    float THRESHOLD = 100;

    public Line(Point start, Point end, Paint paint) {
        this.start = start;
        this.end = end;
        this.paint = paint;

        this.length = dist(start, end);
        Log.d("LINE", "made line with length: " + String.valueOf(length));
    }

    public void update(int point, Point newPoint) {
        Log.d("LINE", "updated line " + point + " to " + newPoint.toString());
        if (point == 0) this.start = newPoint;
        else this.end = newPoint;
    }

    public double dist(Point one, Point two) {
        double result = Math.sqrt((Math.abs(one.x - two.x) * Math.abs(one.x - two.x)) + (Math.abs(one.y - two.y) * Math.abs(one.y - two.y)));
        Log.d("LINE" , String.valueOf(result));
        return result;
    }


    public Map<String, Object> compare(Line otherLine) {
        //if (this.length < THRESHOLD || otherLine.length < THRESHOLD) {
          //  return null;
        //}

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

        if(minDist < TOLERANCE) {
            Map<String, Object> result = new HashMap();
            result.put("minDist", minDist);
            result.put("p1", p1);
            result.put("p2", p2);
            Log.d("LINE", "found: "  + result.toString());
            return result;
        }

        return  null;
    }

    public void draw(Canvas c) {
        c.drawLine(start.x, start.y, end.x, end.y, paint);
    }

}
