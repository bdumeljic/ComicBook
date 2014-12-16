package com.bdumeljic.comicbook;

import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;


public class EditActivity extends ActionBarActivity implements GestureOverlayView.OnGesturePerformedListener {
    static String TAG = "EditActivity";

    private GestureDetectorCompat mGestureDetector;
    private GestureLibrary mLibrary;
    RectF CircleBounds;

    EditView view;
    DrawModel model;
    EditSurfaceThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new DrawModel();
        view = new EditView(this);

        setContentView(R.layout.activity_edit);
        LinearLayout layout = (LinearLayout) findViewById(R.id.main);
        layout.addView(view);

        mLibrary = GestureLibraries.fromRawResource(this, R.raw.gestures);
        if (!mLibrary.load()) {
            finish();
        }

        mGestureDetector = new GestureDetectorCompat(this, mGestureListener);

        GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
        gestures.addOnGesturePerformedListener(this);
    }

    public DrawModel getModel() {
        return model;
    }

    public void onResume() {
        super.onResume();
        thread = new EditSurfaceThread();
        thread.start();
    }

    public void onPause() {
        super.onPause();
        thread.running = false;
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Thread used for managing the {@link com.bdumeljic.comicbook.EditView}.
     */
    class EditSurfaceThread extends Thread {
        /** Variable that keeps track if the surface thread is running or not. */
        private boolean running = true;

        @Override
        public void run() {
            while (running) {
                try {
                    view.postInvalidate();
                    Thread.sleep(50);
                } catch (Exception e) {
                    System.out.println("EditSurfaceThread: " + e);
                    e.printStackTrace();
                }
            }
        }
    }

    private final GestureDetector.SimpleOnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener(){
        public boolean onDown(MotionEvent event) {
            view.onTouchEvent(event);
            return true;
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent e)
    {
        mGestureDetector.onTouchEvent(e);
        return super.dispatchTouchEvent(e);
    }

     public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        ArrayList<Prediction> predictions = mLibrary.recognize(gesture);

        if (predictions.size() > 0 && predictions.get(0).score > 1.0) {
            String result = predictions.get(0).name;

            if ("circle".equalsIgnoreCase(result)) {
                CircleBounds = gesture.getBoundingBox();
                model.computeCircle(CircleBounds);
                Toast.makeText(this, "Circle drawn", Toast.LENGTH_SHORT).show();
            } else if ("triangle".equalsIgnoreCase(result)) {
                Toast.makeText(this, "Rect drawn", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
