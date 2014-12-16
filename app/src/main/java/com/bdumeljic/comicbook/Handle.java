package com.bdumeljic.comicbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;

public class Handle {

    Bitmap bitmap;
    Context mContext;
    Point point;
    int id;

    public Handle(Context context, Bitmap bitmap, Point point, int id) {
        this.id = id;
        this.bitmap = bitmap;
        mContext = context;
        this.point = point;
    }

    public int getWidthOfBall() {
        return bitmap.getWidth();
    }

    public int getHeightOfBall() {
        return bitmap.getHeight();
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return point.x;
    }

    public int getY() {
        return point.y;
    }

    public int getID() {
        return id;
    }

    public void setX(int x) {
        point.x = x;
    }

    public void setY(int y) {
        point.y = y;
    }

    public void setToCorner(int x, int y){
        point.x = x;
        point.y = y;
    }
}
