package com.bdumeljic.comicbook.Models;

import android.content.Context;
import android.graphics.Point;

public class Panel {
    Context mContext;
    Point start;
    int width;
    int height;
    int id;
    static int count = 0;

    public Panel(Context context, Point point, int height, int width, int id) {
        this.id = count++;
        mContext = context;
        this.start = point;
        this.height = height;
        this.width = width;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public int getX() {
        return start.x;
    }

    public int getY() {
        return start.y;
    }

    public int getID() {
        return id;
    }

    public void setX(int x) {
        start.x = x;
    }

    public void setY(int y) {
        start.y = y;
    }
}
