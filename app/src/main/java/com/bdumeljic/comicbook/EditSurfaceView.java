package com.bdumeljic.comicbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

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
                // put back all the mapped things from saveState
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

   // Path path;
   // ArrayList<Point> points = new ArrayList<Point>();
    //ArrayList<ArrayList> paths = new ArrayList<ArrayList>();

    //private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Random random;

    private Path mPath;
    public Paint mPaint;
    ArrayList<Path> paths = new ArrayList<Path>();
    private ArrayList<Path> undonePaths = new ArrayList<Path>();


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

        setFocusable(true);

        random = new Random();

        paths.clear();
        undonePaths.clear();

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        //mPaint.setDither(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setColor(getResources().getColor(R.color.non_photo_blue));
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(4);

        mPath = new Path();

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

        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "event: " + " down, starting new points array");
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "event: " + " move");
                touch_move(x, y);
                invalidate();
                break;
        }

        return true;
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        undonePaths.clear();
        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;


    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mPath.lineTo(mX, mY);
        Canvas mCanvas = surfaceHolder.lockCanvas(null);

        mCanvas.drawPath(mPath, mPaint);
        paths.add(mPath);
        mPath = new Path();
        surfaceHolder.unlockCanvasAndPost(mCanvas);

        Log.e("", "pathsize:::" + paths.size());
        Log.e("", "undonepathsize:::" + undonePaths.size());

    }

    public void onClickUndo() {

        Log.e("", "pathsize:::" + paths.size());
        Log.e("", "undonepathsize:::" + undonePaths.size());
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            invalidate();

            Toast.makeText(getContext(), "UNDO", Toast.LENGTH_SHORT).show();

        } else {

        }
        // toast the user

    }

    public void onClickRedo() {

        Log.e("", "pathsize:::" + paths.size());
        Log.e("", "undonepathsize:::" + undonePaths.size());
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidate();

            Toast.makeText(getContext(), "REDO", Toast.LENGTH_SHORT).show();
        } else {

        }
        // toast the user

    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "drawing");

        for (Path p : paths) {
            canvas.drawPath(p, mPaint);
        }

        canvas.drawPath(mPath, mPaint);
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
