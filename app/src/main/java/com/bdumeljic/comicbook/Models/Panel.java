package com.bdumeljic.comicbook.Models;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;

public class Panel{
    Context mContext;
    int startX;
    int startY;
    int width;
    int height;
    int id;
    Rect rect;
    static int count = 0;

    /**
     * Create a new panel
     *
     * @param pointX Starting point x value of the pane
     * @param pointY Starting point y value of the pane
     * @param height Height of the panel
     * @param width Width of the panel
     */
    public Panel(Context context, int pointX, int pointY, int height, int width) {
        this.id = count++;
        mContext = context;
        this.startX = pointX;
        this.startY = pointY;
        this.height = height;
        this.width = width;
        this.rect = new Rect(pointX, pointY, pointX + width, pointY + height);
    }

    /**
     * Get the id of a panel
     *
     * @return Id of this panel
     */

    public int getId() {
        return id;
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
        return startX;
    }

    /**
     * Set the x value of the panel's starting point
     */
    public void setX(int x) {
        startX = x;
    }

    /**
     * Get the y value of the panel's starting point
     * @return Y value of the starting point
     */
    public int getY() {
        return startY;
    }

    /**
     * Set the y value of the panel's starting point
     */
    public void setY(int y) {
        startX = y;
    }
    /**
     * Get the rect defined by Panel
     * @return Rect defined by Panel
     */

    public Rect getDefinedRect(){
        return this.rect;
    }
    /**
     * Set the rect defined by Panel
     * @return Rect defined by Panel
     */

    public void setDefinedRect(Rect rect){
        this.rect = new Rect(rect);
    }
}
