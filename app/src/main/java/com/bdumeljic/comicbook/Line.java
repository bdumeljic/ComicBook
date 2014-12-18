package com.bdumeljic.comicbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public class Line {

    Point start;
    Point end;

    public Line(Point start, Point end) {
        this.start = start;
        this.end = end;
    }

    public Point getStartPoint(){
        return start;
    }

    public void setStartPoint(Point start){
        this.start = start;
    }

    public Point getEndPoint(){
        return end;
    }

    public void setEndPoint(Point end){
        this.end = end;
    }

    public void draw(Canvas c) {
        c.drawLine(start.x, start.y, end.x, end.y, paint);
    }
}
