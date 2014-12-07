package com.bdumeljic.comicbook.Models;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;

public class Panel{
    Context mContext;
    Point start;
    int width;
    int height;
    int id;
    Rect rect;
    static int count = 0;

    /**
     * Create a new panel
     *
     * @param point Starting point of the pane
     * @param height Height of the panel
     * @param width Width of the panel
     * @param id Panel ID
     */
    public Panel(Context context, Point point, int height, int width, int id) {
        this.id = count++;
        mContext = context;
        this.start = point;
        this.height = height;
        this.width = width;
        this.rect = new Rect(point.x, point.y, point.x + width, point.y + height);
    }

    /**
     * Get the width of a panel
     *
     * @return Width of this panel
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * Set the width of a panel
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Get the height of a panel
     *
     * @return Height of this panel
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Set the height of a panel
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Get the x value of the panel's starting point
     * @return X value of the starting point
     */
    public int getX() {
        return start.x;
    }

    /**
     * Set the x value of the panel's starting point
     */
    public void setX(int x) {
        start.x = x;
    }

    /**
     * Get the y value of the panel's starting point
     * @return Y value of the starting point
     */
    public int getY() {
        return start.y;
    }

    /**
     * Set the y value of the panel's starting point
     */
    public void setY(int y) {
        start.y = y;
    }
    /**
     * Get the rect defined by Panel
     * @return Rect defined by Panel
     */

    public Rect getDefinedRect(){
        return this.rect;
    }
}
