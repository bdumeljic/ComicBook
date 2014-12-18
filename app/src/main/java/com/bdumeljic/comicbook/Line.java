package com.bdumeljic.comicbook;

import android.content.Context;
import android.graphics.Bitmap;
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
}
