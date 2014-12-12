package com.bdumeljic.comicbook.Models;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

public class Panel extends SugarRecord<Panel> {
    long id;

    int startX;
    int startY;
    int width;
    int height;

    long pageId;

    @Ignore
    Rect rect;

    //static int count = 0;

    public Panel() {

    }

    /**
     * Create a new panel
     *
     * @param pointX Starting point x value of the pane
     * @param pointY Starting point y value of the pane
     * @param height Height of the panel
     * @param width Width of the panel
     */
    public Panel(int pointX, int pointY, int height, int width) {
        this.startX = pointX;
        this.startY = pointY;
        this.height = height;
        this.width = width;
        this.pageId = -1;

        this.rect = new Rect(startX, startY, startX + width, startY + height);
    }

    /**
     * Get the id of the page this panel belongs to.
     * @return Page Id
     */
    public long getPageId() {
        return pageId;
    }

    public void setPageId(long page) {
        this.pageId = page;
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
        if (rect == null) {
            this.rect = new Rect(startX, startY, startX + width, startY + height);
        }

        return rect;
    }
    /**
     * Set the rect defined by Panel
     * @return Rect defined by Panel
     */

    public void setDefinedRect(Rect rect){
        this.rect = new Rect(rect);
    }
}
