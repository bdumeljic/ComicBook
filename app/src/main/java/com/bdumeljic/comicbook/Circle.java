package com.bdumeljic.comicbook;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

public class Circle {
    Point center;
    float radius;
    Paint paint;

    public Circle(Point c, float r, Paint paint) {
        this.center = c;
        this.radius = r;
        this.paint = paint;
    }

    public void update(Point start, Point end) {

    }

    public void draw(Canvas c) {
        c.drawCircle(center.x, center.y, radius, paint);
    }
}
