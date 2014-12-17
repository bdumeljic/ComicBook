package com.bdumeljic.comicbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Custom {@link android.view.SurfaceView} used for drawing the page layout. One surface is one page in a comic book volume.
 */
public class EditView extends View {
    private String TAG = "EditView";

    EditActivity controller;

    public EditView(Context context) {
        super(context);
        this.controller = (EditActivity) context;
    }

    public EditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.controller = (EditActivity) context;
    }

    public EditView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.controller = (EditActivity) context;
    }

    public void onDraw(Canvas canvas) {
        canvas.drawPath(controller.getModel().currentPath, controller.getModel().blackPaint);
        for (Path pathBlack : controller.getModel().blackPaths) {
            canvas.drawPath(pathBlack, controller.getModel().blackPaint);
        }

        if(controller.getModel().mLineEnd != null && controller.getModel().mLineStart != null){
            canvas.drawLine(controller.getModel().mLineStart.x, controller.getModel().mLineStart.y, controller.getModel().mLineEnd.x, controller.getModel().mLineEnd.y, controller.getModel().blackPaint);
            controller.getModel().mLineEnd = null;
            controller.getModel().mLineStart = null;
        }

        if(controller.getModel().mCircleCenter != null && controller.getModel().mCircleRadius != -1){
           canvas.drawCircle(controller.getModel().mCircleCenter.x, controller.getModel().mCircleCenter.y, controller.getModel().mCircleRadius, controller.getModel().blackPaint);
        }

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        controller.getModel().drawPath(event);
        return true;
    }
}