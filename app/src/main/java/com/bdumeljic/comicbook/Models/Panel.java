package com.bdumeljic.comicbook.Models;

import android.content.Context;
import android.graphics.Point;

public class Panel {
    Context mContext;
    int startX;
    int startY;
    int width;
    int height;
    int id;
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
        this.startX = point.x;
        this.startY = point.y;
        this.height = height;
        this.width = width;
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
     * Get the height of a panel
     *
     * @return Height of this panel
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Get the x value of the panel's starting point
     * @return X value of the starting point
     */
    public int getX() {
        return startX;
    }

    /**
     * Get the y value of the panel's starting point
     * @return Y value of the starting point
     */
    public int getY() {
        return startY;
    }
}
