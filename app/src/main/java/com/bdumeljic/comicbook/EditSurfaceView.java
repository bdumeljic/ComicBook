package com.bdumeljic.comicbook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

class EditSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    class EditSurfaceThread extends Thread {

        private SurfaceHolder mSurfaceHolder;
        private Handler mHandler;

        private boolean mRun = false;
        private final Object mRunLock = new Object();

        private int mMode;

        public static final int STATE_PAUSE = 1;
        public static final int STATE_READY = 2;
        public static final int STATE_RUNNING = 3;

        private int mCanvasHeight = 1;
        private int mCanvasWidth = 1;

        public EditSurfaceThread(SurfaceHolder surfaceHolder, Context context,
                                 Handler handler) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mHandler = handler;
            mContext = context;
        }

        public void doStart() {
            synchronized (mSurfaceHolder) {
                setState(STATE_RUNNING);
            }
        }

        public void pause() {
            synchronized (mSurfaceHolder) {
                if (mMode == STATE_RUNNING) setState(STATE_PAUSE);
            }
        }

        @Override
        public void run() {
            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        //if (mMode == STATE_RUNNING) updatePhysics();
                        // Critical section. Do not allow mRun to be set false until
                        // we are sure all canvas draw operations are complete.
                        //
                        // If mRun has been toggled false, inhibit canvas operations.
                        synchronized (mRunLock) {
                            if (mRun) doDraw(c);
                        }
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }

        public Bundle saveState(Bundle map) {
            synchronized (mSurfaceHolder) {

            }

            return map;
        }

        public synchronized void restoreState(Bundle savedState) {
            synchronized (mSurfaceHolder) {

            }
        }

        public void setRunning(boolean b) {
            // Do not allow mRun to be modified while any canvas operations
            // are potentially in-flight. See doDraw().
            synchronized (mRunLock) {
                mRun = b;
            }
        }

        public void setState(int mode) {
            synchronized (mSurfaceHolder) {
                Log.d(TAG, String.valueOf(mode));
                mMode = mode;
            }
        }

        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;

            }
        }

        public void unpause() {
            // Move the real time clock up to now
            synchronized (mSurfaceHolder) {
                //mLastTime = System.currentTimeMillis() + 100;
            }
            setState(STATE_RUNNING);
        }

        private void doDraw(Canvas canvas) {

   //         canvas.drawLine(mGoalX, 1 + mCanvasHeight - TARGET_PAD_HEIGHT,
     //               mGoalX + mGoalWidth, 1 + mCanvasHeight - TARGET_PAD_HEIGHT,
       //             mLinePaint);

        }

    }

    private String TAG = "EditSurfaceView";

    private Context mContext;

    EditSurfaceThread thread;
    SurfaceHolder surfaceHolder;
    volatile boolean running = false;

    Path path;
    ArrayList<Point> points = new ArrayList<Point>();


    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Random random;

    public EditSurfaceView(Context context) {
        super(context);

        init(context);
    }

    public EditSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public EditSurfaceThread getThread() {
        return thread;
    }

    public void init(Context context) {

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new EditSurfaceThread(surfaceHolder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {

            }
        });

        //setFocusable(true);

        random = new Random();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.BLACK);

    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (!hasWindowFocus) thread.pause();
    }

    public void onResumeMySurfaceView(){
        running = true;
        thread.doStart();
    }

    public void onPauseMySurfaceView(){
        boolean retry = true;
        running = false;
        while(retry){
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "touched");
        /*if(event.getAction() == MotionEvent.ACTION_DOWN){
            path = new Path();
            path.moveTo(event.getX(), event.getY());
        }else if(event.getAction() == MotionEvent.ACTION_MOVE){
            path.lineTo(event.getX(), event.getY());
        }else if(event.getAction() == MotionEvent.ACTION_UP){
            path.lineTo(event.getX(), event.getY());
        }

        if(path != null){
            Canvas canvas = surfaceHolder.lockCanvas();
            paint.setColor(Color.GREEN);
            canvas.drawPath(path, paint);
            surfaceHolder.unlockCanvasAndPost(canvas);
            Log.d(TAG, "showing path");
        }
*/
        Point point = new Point();
        point.x = (int) event.getX();
        point.y = (int) event.getY();
        points.add(point);
        invalidate();
        Log.d("PathDraw", "point: " + point);

        return true;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "drawing");

        Path path = new Path();
        boolean first = true;
        for(Point point : points){
            if(first){
                first = false;
                path.moveTo(point.x, point.y);
            }
            else{
                path.lineTo(point.x, point.y);
            }
        }
        canvas.drawPath(path, paint);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setSurfaceSize(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        thread.setRunning(false);
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
}
