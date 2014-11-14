package com.bdumeljic.comicbook;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

import com.bdumeljic.comicbook.Models.Panel;

import java.util.ArrayList;

class EditSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private String TAG = "EditSurfaceView";

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

    private Context mContext;

    EditSurfaceThread thread;
    SurfaceHolder surfaceHolder;
    volatile boolean running = false;

    private Path mCurrentPath;
    public Paint mBlackPaint;
    public Paint mBluePaint;

    public final String BLUE_INK = "blue_ink";
    public final String BLACK_INK = "black_ink";

    public final int BLUE = 0;
    public final int BLACK = 1;

    public int drawing_mode = BLACK;

    ArrayList<Path> mBluePaths = new ArrayList<Path>();

    ArrayList<Point> mBlackPoints = new ArrayList<Point>();
    ArrayList<Path> mBlackPaths = new ArrayList<Path>();

    private ArrayList<Panel> mPanels = new ArrayList<Panel>();

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

        mBluePaths.clear();
        undonePaths.clear();

        mBlackPaint = new Paint();
        mBlackPaint.setAntiAlias(true);
        //mBlackPaint.setDither(true);
        mBlackPaint.setStyle(Paint.Style.STROKE);
        mBlackPaint.setColor(Color.BLACK);
        mBlackPaint.setStrokeJoin(Paint.Join.ROUND);
        mBlackPaint.setStrokeCap(Paint.Cap.ROUND);
        mBlackPaint.setStrokeWidth(4);

        mBluePaint = new Paint();
        mBluePaint.setAntiAlias(true);
        //mBluePaint.setDither(true);
        mBluePaint.setStyle(Paint.Style.STROKE);
        mBluePaint.setColor(getResources().getColor(R.color.non_photo_blue));
        mBluePaint.setStrokeJoin(Paint.Join.ROUND);
        mBluePaint.setStrokeCap(Paint.Cap.ROUND);
        mBluePaint.setStrokeWidth(4);

        mCurrentPath = new Path();

        setFocusable(true);
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
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        Log.d(TAG, event.toString());

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }

        return true;
    }

    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;

    private void touch_start(float x, float y) {
        undonePaths.clear();
        mCurrentPath.reset();
        mCurrentPath.moveTo(x, y);
        mX = x;
        mY = y;

        if(drawing_mode == BLACK) {
            mBlackPoints.add(new Point((int) x, (int) y));
        }
    }

    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mCurrentPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touch_up() {
        mCurrentPath.lineTo(mX, mY);

        mBluePaths.add(mCurrentPath);
        mCurrentPath = new Path();

        Log.d("", "pathsize:::" + mBluePaths.size());
        Log.d("", "undonepathsize:::" + undonePaths.size());

        if(drawing_mode == BLACK) {
            mBlackPoints.add(new Point((int) mX, (int) mY));
        }

        if(drawing_mode == BLACK && mBlackPoints.size() >= 3 ) {
            check_shape();
        }
    }

    public void onClickUndo() {
        Log.d("", "pathsize:::" + mBluePaths.size());
        Log.d("", "undonepathsize:::" + undonePaths.size());
        if (mBluePaths.size() > 0) {
            undonePaths.add(mBluePaths.remove(mBluePaths.size() - 1));
            invalidate();

            Toast.makeText(getContext(), "UNDO", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.no_undo), Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickRedo() {
        Log.e("", "pathsize:::" + mBluePaths.size());
        Log.e("", "undonepathsize:::" + undonePaths.size());
        if (undonePaths.size() > 0) {
            mBluePaths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidate();

            Toast.makeText(getContext(), "REDO", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.no_redo), Toast.LENGTH_SHORT).show();
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(mCurrentPath, mBlackPaint);

        for (Path pBlue : mBluePaths) {
            canvas.drawPath(pBlue, mBluePaint);
        }

        for (Panel panel : mPanels) {
            canvas.drawRect(panel.getX(),  panel.getY(),  panel.getX() + panel.getWidth(), panel.getY() + panel.getHeight(),  mBlackPaint);
        }
    }

    public void check_shape() {

        Log.d(TAG, "entered check shape " + mBlackPoints.toString());

        int SHAPE_THRESHOLD = 100;

        float width = 0;
        float height = 0;

        int startX = mBlackPoints.get(0).x;
        int startY = mBlackPoints.get(0).y;

        float diffY1 = mBlackPoints.get(1).y - mBlackPoints.get(0).y;
        float diffX1 = mBlackPoints.get(1).x - mBlackPoints.get(0).x;

        Log.d(TAG, "diff " + diffX1 + " " + diffY1);


        if (Math.abs(diffX1) > SHAPE_THRESHOLD || Math.abs(diffY1) > SHAPE_THRESHOLD) {
            Log.d(TAG, "proper shape detected 1");
            if (Math.abs(diffX1) > Math.abs(diffY1) ) {
                width = diffX1;
            } else {
                height = diffY1;
            }
        }

        float diffY2 = mBlackPoints.get(3).y - mBlackPoints.get(2).y;
        float diffX2 = mBlackPoints.get(3).x - mBlackPoints.get(2).x;

        if (Math.abs(diffX2) > SHAPE_THRESHOLD || Math.abs(diffY2) > SHAPE_THRESHOLD) {
            Log.d(TAG, "proper shape detected 2");
            if (Math.abs(diffX2) > Math.abs(diffY2) ) {
                width = diffX2;
            } else {
                height = diffY2;
            }
        }

        if(width > 0 && height > 0) {
            Panel panel = new Panel(getContext(), new Point(startX, startY), (int) height, (int) width, mPanels.size());
            mPanels.add(panel);

            Log.d(TAG, "rect added");
            mBlackPoints.clear();

        } else {
            mBlackPoints.clear();
            Log.d(TAG, "points cleared");

        }
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
