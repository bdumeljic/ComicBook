package com.bdumeljic.comicbook;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public class Line {

    Point start;
    Point end;
    Paint paint;

    public Line(Point start, Point end, Paint paint) {
        this.start = start;
        this.end = end;
        this.paint = paint;
    }

    public void update(Point start, Point end) {

    }

    public void draw(Canvas c) {
        c.drawLine(start.x, start.y, end.x, end.y, paint);
    }

}
