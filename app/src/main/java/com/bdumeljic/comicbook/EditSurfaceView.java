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
    public Paint mSelectedPaint;

    public final int BLUE = 0;
    public final int BLACK = 1;

    public int drawing_mode;

    public boolean visibilityBlue;
    public boolean visibilityBlack;

    ArrayList<Path> mBluePaths = new ArrayList<Path>();

    ArrayList<Point> mBlackPoints = new ArrayList<Point>();
    ArrayList<Path> mBlackPaths = new ArrayList<Path>();
    private ArrayList<Panel> mPanels = new ArrayList<Panel>();

    private ArrayList<Path> undonePaths = new ArrayList<Path>();

    private Panel selectedPanel;

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

        setDrawingMode(BLUE);
        visibilityBlue = false;
        visibilityBlack = false;

        mBluePaths.clear();
        undonePaths.clear();

        mBlackPaint = new Paint();
        initPaint(mBlackPaint);
        mBlackPaint.setColor(Color.BLACK);

        mBluePaint = new Paint();
        initPaint(mBluePaint);
        mBluePaint.setColor(getResources().getColor(R.color.non_photo_blue));

        mSelectedPaint = new Paint();
        initPaint(mSelectedPaint);
        mSelectedPaint.setStyle(Paint.Style.FILL);
        mSelectedPaint.setColor(getResources().getColor(R.color.pink_alpha));
        mSelectedPaint.setStrokeWidth(6);

        mCurrentPath = new Path();

        setFocusable(true);
    }

    public void initPaint(Paint p) {
        p.setAntiAlias(true);
        //mBlackPaint.setDither(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(4);
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

    public void setDrawingMode(int mode) {
        switch (mode) {
            case BLUE:
                drawing_mode = BLUE;
                Toast.makeText(getContext(), "Selected Blue Ink", Toast.LENGTH_SHORT).show();
                break;
            case BLACK:
                drawing_mode = BLACK;
                Toast.makeText(getContext(), "Selected Black Ink", Toast.LENGTH_SHORT).show();
                break;
        }

        Log.d(TAG, "switched to drawing in " + mode);
        invalidate();
    }

    public boolean isDrawingModeBlack() {
        return drawing_mode == BLACK;
    }

    public boolean isDrawingModeBlue() {
        return drawing_mode == BLUE;

    }

    public void toggleVisibilityBlue(Boolean visible) {
        if(!visible) {
            visibilityBlue = false;
            Toast.makeText(getContext(), "Turn blue ink OFF", Toast.LENGTH_SHORT).show();

        } else {
            visibilityBlue = true;
            Toast.makeText(getContext(), "Keep blue ink ON", Toast.LENGTH_SHORT).show();

        }

        invalidate();
        Log.d(TAG, "toggle blue to  " + String.valueOf(visibilityBlue));
    }

    public void toggleVisibilityBlack(Boolean visible) {
        if(!visible) {
            visibilityBlack = false;
            Toast.makeText(getContext(), "Turn black ink OFF", Toast.LENGTH_SHORT).show();

        } else {
            visibilityBlack = true;
            Toast.makeText(getContext(), "Keep black ink ON", Toast.LENGTH_SHORT).show();

        }

        invalidate();
        Log.d(TAG, "toggle black to  " + String.valueOf(visibilityBlack));
    }

    //variable for counting two successive up-down events
    int clickCount = 0;
    //variable for storing the time of first click
    long startTime;
    //variable for calculating the total time
    long duration;
    //constant for defining the time duration between the click that can be considered as double-tap
    static final int MAX_DURATION = 500;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        Log.d(TAG, event.toString());

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                if(isDrawingModeBlack()) {
                    startTime = System.currentTimeMillis();
                    clickCount++;
                }

                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if(isDrawingModeBlack()) {
                    long time = System.currentTimeMillis() - startTime;
                    duration = duration + time;
                    if (clickCount == 2) {
                        if (duration <= MAX_DURATION) {
                            Log.d(TAG, "tap tap");

                            selectedPanel = try_to_find_panel(x, y);
                        }
                        clickCount = 0;
                        duration = 0;

                    } else if (duration > MAX_DURATION) {
                        clickCount = 0;
                        duration = 0;

                    }
                }

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

        if(isDrawingModeBlack()) {
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

        if (isDrawingModeBlue()) {
            mBluePaths.add(mCurrentPath);
        }

        Log.d("", "pathsize:::" + mBluePaths.size());
        Log.d("", "undonepathsize:::" + undonePaths.size());

        if(isDrawingModeBlack()) {
            mBlackPaths.add(mCurrentPath);
            mBlackPoints.add(new Point((int) mX, (int) mY));

            if (mBlackPoints.size() >= 4) {
                check_shape();
            }

            if (mBlackPoints.size() >= 1 && selectedPanel != null) {
                selectedPanel = null;
                Log.d(TAG, "panel deselected");
            }

        }

        mCurrentPath = new Path();

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

        if (isDrawingModeBlue()) {
            canvas.drawPath(mCurrentPath, mBluePaint);

            for (Path pBlue : mBluePaths) {
                canvas.drawPath(pBlue, mBluePaint);
            }

            if(visibilityBlack) {
                for (Panel panel : mPanels) {
                    canvas.drawRect(panel.getX(),  panel.getY(),  panel.getX() + panel.getWidth(), panel.getY() + panel.getHeight(),  mBlackPaint);
                }
            }
        }

        if (isDrawingModeBlack()) {
            for (Panel panel : mPanels) {
                canvas.drawRect(panel.getX(),  panel.getY(),  panel.getX() + panel.getWidth(), panel.getY() + panel.getHeight(),  mBlackPaint);
            }

            if (selectedPanel != null) {
                canvas.drawRect(selectedPanel.getX(),  selectedPanel.getY(),  selectedPanel.getX() + selectedPanel.getWidth(), selectedPanel.getY() + selectedPanel.getHeight(),  mSelectedPaint);
                Log.d(TAG, "there is a selected panel");
            }

            canvas.drawPath(mCurrentPath, mBlackPaint);

            for (Path pathBlack : mBlackPaths) {
                canvas.drawPath(pathBlack, mBlackPaint);
            }

            if(visibilityBlue) {
                for (Path pBlue : mBluePaths) {
                    canvas.drawPath(pBlue, mBluePaint);
                }
            }
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
            mBlackPaths.clear();

        } else {
            mBlackPoints.clear();
            Log.d(TAG, "points cleared");

        }
    }

    public Panel try_to_find_panel(float x, float y) {
        boolean existsX = false;
        boolean existsY = false;

        Panel foundPanel = null;

        for (Panel panel : mPanels) {
            if (panel.getX() < (int) x && (int) x < (panel.getX() + panel.getWidth())) {
                Log.d(TAG, "found panel within x");
                existsX = true;
            }

            if (panel.getY() < (int) y && (int) y < (panel.getY() + panel.getHeight())) {
                Log.d(TAG, "found panel within y");
                existsY = true;
            }

            if (existsX && existsY) {
                Log.d(TAG, "found panel!");
                foundPanel = panel;
            } else {
                existsX = false;
                existsY = false;
            }
        }

        return foundPanel;
    }

    public void clearPage() {
        mBluePaths.clear();
        mBlackPaths.clear();
        mBlackPoints.clear();
        mPanels.clear();

        undonePaths.clear();

        mCurrentPath = new Path();

        invalidate();

        // TODO: Ask for confirmation before clearing
        Toast.makeText(getContext(), "Page Cleared", Toast.LENGTH_SHORT).show();
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
                Log.d(TAG, "trying to destroy surface");
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }
}
