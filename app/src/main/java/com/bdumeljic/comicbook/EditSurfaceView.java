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
import android.util.Pair;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.bdumeljic.comicbook.Models.Panel;

import java.util.ArrayList;

/**
 * Custom {@link android.view.SurfaceView} used for drawing the page layout. One surface is one page in a comic book volume.
 */
public class EditSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private String TAG = "EditSurfaceView";

    boolean surfaceCreated;
    private boolean isPanelDetected;

    /**
     * Thread used for managing the {@link com.bdumeljic.comicbook.EditSurfaceView}.
     */
    class EditSurfaceThread extends Thread {

        private SurfaceHolder mSurfaceHolder;

        /** Variable that keeps track if the surface thread is running or not. */
        private boolean mRun = false;
        private final Object mRunLock = new Object();

        private int mCanvasHeight = 1;
        private int mCanvasWidth = 1;

        public EditSurfaceThread(SurfaceHolder surfaceHolder, Context context) {
            // get handles to some important objects
            mSurfaceHolder = surfaceHolder;
            mContext = context;
        }

        /**
         * Change if the surface is running or not. Used to pause on resume.
         *
         * @param running Boolean value
         */
        public void setRunning(boolean running) {
            // Do not allow mRun to be modified while any canvas operations
            // are potentially in-flight. See doDraw().
            if(!mRun && running) {
                Log.d(TAG, "UNpausing .." );

            } else if(mRun && !running) {
                Log.d(TAG, "PPPausing .." );
            }

            Log.d(TAG, "set running to .." + running);
            synchronized (mSurfaceHolder) {
                synchronized (mRunLock) {
                    mRun = running;
                }
            }
        }

        @Override
        public void run() {
            while (mRun) {
                Canvas canvas = null;
                try {
                    canvas = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {

                        // If mRun has been toggled false, inhibit canvas operations.
                        synchronized (mRunLock) {
                            if (mRun) doDraw(canvas);
                        }

                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (canvas != null) {
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        private void doDraw(Canvas canvas) {

            //  Log.i(TAG, "DO DRAW IN THREAD CALLED");
        }

        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;

            }
        }
    }

    private Context mContext;

    EditSurfaceThread thread;
    SurfaceHolder surfaceHolder;

    private static final int BLACKPATH = 0;
    private static final int BLUEPATH = 1;
    private static final int PANEL = 2;

    /** Path that is currently being drawn. */
    private Path mCurrentPath;

    /** Paint object that paints black ink. */
    public Paint mBlackPaint;
    /** Paint object that paints blue ink. */
    public Paint mBluePaint;
    /** Paint object that paints selected panel objects. */
    public Paint mSelectedPaint;

    public final int BLUE = 0;
    public final int BLACK = 1;

    /** Variable that keeps track of which drawing mode is used (black or blue ink). */
    public int drawing_mode;

    /** Visibility of the blue ink. */
    public boolean visibilityBlue;
    /** Visibility of the black ink. */
    public boolean visibilityBlack;

    /** List of the paths drawn with blue ink on the canvas. */
    ArrayList<Path> mBluePaths = new ArrayList<Path>();

    ArrayList<Pair<Object, Integer>> mDrawings = new ArrayList<Pair<Object, Integer>>();
    private ArrayList<Pair<Object, Integer>> mUndoneDrawings = new ArrayList<Pair<Object, Integer>>();

    /**
     * List of the points of the paths drawn with black ink on the canvas.
     * Used to retrace the steps made if a valid panel has been drawn
     * */
    ArrayList<Point> mBlackPoints = new ArrayList<Point>();
    /** List of the paths drawn with black ink on the canvas. */
    ArrayList<Path> mBlackPaths = new ArrayList<Path>();
    /** List of the panels drawn on the canvas. */
    private ArrayList<Panel> mPanels = new ArrayList<Panel>();

    /** List of the paths that were drawn, but have been undone. Used for the redo function. */
    private ArrayList<Path> undonePaths = new ArrayList<Path>();

    /** Reference to the selected panel. */
    private Panel selectedPanel;

    public EditSurfaceView(Context context) {
        super(context);
        surfaceCreated = true;

        init(context);
    }

    public EditSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);
    }

    public EditSurfaceThread getThread() {
        return thread;
    }

    /**
     * Setup the drawing surface. Start with blue ink.
     * @param context
     */
    public void init(Context context) {

        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        thread = new EditSurfaceThread(surfaceHolder, context);
        setFocusable(true);

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

    }

    /**
     * Setup a Paint object.
     * @param p
     */
    public void initPaint(Paint p) {
        p.setAntiAlias(true);
        //mBlackPaint.setDither(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(4);
    }

    /**
     * Set the current drawing mode. Which can either be blue or black ink.
     * @param mode Blue or black ink
     */
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

    /**
     * Check if the current drawing ink is black.
     * @return True or false for black being the current drawing mode.
     */
    public boolean isDrawingModeBlack() {
        return drawing_mode == BLACK;
    }

    /**
     * Check if the current drawing ink is blue.
     * @return True or false for blue being the current drawing mode.
     */
    public boolean isDrawingModeBlue() {
        return drawing_mode == BLUE;

    }

    /**
     * Toggle the blue ink to be permanently visible or not. Even if the user is drawing in black.
     * @param visible Boolean value of blue ink visibility
     */
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

    /**
     * Toggle the black ink to be permanently visible or not. Even if the user is drawing in blue.
     * @param visible  Boolean value of black ink visibility
     */
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

    /** Variable for counting two successive up-down events for the selection of panels. */
    int clickCount = 0;
    /** Variable for storing the time of first click. */
    long startTime;
    /** Variable for calculating the total time */
    long duration;
    /** Constant for defining the time duration between the click that can be considered as double-tap. */
    static final int MAX_DURATION = 500;

    /**
     * Handle all the touch events on the canvas, e.g. drawing and selecting panels.
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        Log.d(TAG, event.toString());

        switch(action) {
            case MotionEvent.ACTION_DOWN:
                // Start check for a double tap if in BLACK mode
                if(isDrawingModeBlack()) {
                    startTime = System.currentTimeMillis();
                    clickCount++;
                }

                // Start a draw event
                touch_start(x, y);

                // Redraw the view
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                // Check for a double tap used for panel selection if in BLACK drawing mode
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
    /** Minimum touch distance */
    private static final float TOUCH_TOLERANCE = 4;

    /**
     * Start a touch event from the provided location.
     * </p>
     * Reset the undone paths array when drawing is resumed.
     * @param x X value of touch starting point
     * @param y Y value of touch starting point
     */
    private void touch_start(float x, float y) {
        undonePaths.clear();
        mCurrentPath.reset();
        mCurrentPath.moveTo(x, y);
        mX = x;
        mY = y;

        // Add this new point to the black points array if BLACK mode is on
        if(isDrawingModeBlack()) {
            mBlackPoints.add(new Point((int) x, (int) y));
        }
    }

    /**
     * Handle a move event. Continue drawing the path.
     * @param x
     * @param y
     */
    private void touch_move(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mCurrentPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    /**
     * Handle the end of a touch or draw. Check if this was a double tap for panel selection or the drawing of a line.
     */
    private void touch_up() {
        mCurrentPath.lineTo(mX, mY);

        if (isDrawingModeBlue()) {
            mBluePaths.add(mCurrentPath);
            mDrawings.add(new Pair(mCurrentPath, BLUEPATH));
        }

        Log.d("", "pathsize:::" + mBluePaths.size());
        Log.d("", "undonepathsize:::" + undonePaths.size());

        // Check for double tap
        if(isDrawingModeBlack()) {
            mBlackPaths.add(mCurrentPath);
            mBlackPoints.add(new Point((int) mX, (int) mY));

            if (mBlackPoints.size() >= 4) {
                check_shape();
                mBlackPaths.clear();
            }

            if (mBlackPoints.size() >= 1 && selectedPanel != null) {
                selectedPanel = null;
                Log.d(TAG, "panel deselected");
            }

        }

        mCurrentPath = new Path();

    }

    /**
     * Undo the previous drawing action. Inform the user of this action.
     */
    public void onClickUndo() {
        Log.d("", "pathsize:::" + mBluePaths.size());
        Log.d("", "undonepathsize:::" + undonePaths.size());
        if (mDrawings.size() > 0) {
            if(mDrawings.get(mDrawings.size()-1).second == BLUEPATH){
                mBluePaths.remove(mBluePaths.size()-1);
            }
            if(mDrawings.get(mDrawings.size()-1).second == PANEL){
                mPanels.remove(mPanels.size()-1);
            }

            mUndoneDrawings.add(mDrawings.remove(mDrawings.size() - 1));
            invalidate();

            Toast.makeText(getContext(), "UNDO", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.no_undo), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Redo a drawing action that has been undone. Inform the user of this action.
     */
    public void onClickRedo() {
        Log.e("", "pathsize:::" + mBluePaths.size());
        Log.e("", "undonepathsize:::" + undonePaths.size());
        if (mUndoneDrawings.size() > 0) {
            if(mUndoneDrawings.get(mUndoneDrawings.size()-1).second == BLUEPATH){
                mBluePaths.add((Path) mUndoneDrawings.get(mUndoneDrawings.size()-1).first);
            }
            else if(mUndoneDrawings.get(mUndoneDrawings.size()-1).second == PANEL){
                mPanels.add((Panel) mUndoneDrawings.get(mUndoneDrawings.size()-1).first);
            }

            mDrawings.add(mUndoneDrawings.remove(mUndoneDrawings.size() - 1));
            invalidate();

            Toast.makeText(getContext(), "REDO", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.no_redo), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle drawing the drawn object on the canvas. Take into account which layer (BLUE or BLACK) is visible.
     * @param canvas Canvas that is being drawn on
     */
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isDrawingModeBlue()) {
            // Draw the latest blue path.
            canvas.drawPath(mCurrentPath, mBluePaint);

            // Draw the older blue paths.
            for (Path pBlue : mBluePaths) {
                canvas.drawPath(pBlue, mBluePaint);
            }

            // Draw the black panels if the black ink is visible.
            if(visibilityBlack) {
                for (Panel panel : mPanels) {
                    canvas.drawRect(panel.getX(),  panel.getY(),  panel.getX() + panel.getWidth(), panel.getY() + panel.getHeight(),  mBlackPaint);
                }
            }
        }

        if (isDrawingModeBlack()) {
            // First draw all the panels.
            for (Panel panel : mPanels) {
                canvas.drawRect(panel.getX(),  panel.getY(),  panel.getX() + panel.getWidth(), panel.getY() + panel.getHeight(),  mBlackPaint);
            }

            // If a panel is selected redraw it with the selected paint.
            if (selectedPanel != null) {
                canvas.drawRect(selectedPanel.getX(),  selectedPanel.getY(),  selectedPanel.getX() + selectedPanel.getWidth(), selectedPanel.getY() + selectedPanel.getHeight(),  mSelectedPaint);
                Log.d(TAG, "there is a selected panel");
            }

            // Add the latest path.
            canvas.drawPath(mCurrentPath, mBlackPaint);

            // Add older paths.
            for (Path pathBlack : mBlackPaths) {
                canvas.drawPath(pathBlack, mBlackPaint);
            }

            // Add the blue paths if blue ink is visible.
            if(visibilityBlue) {
                for (Path pBlue : mBluePaths) {
                    canvas.drawPath(pBlue, mBluePaint);
                }
            }
        }
    }

    /**
     * Check if the shape that has been drawn is a proper rectangle.
     */
    public void check_shape() {
        /** Minimal required height/width */
        int SHAPE_THRESHOLD = 100;
        int left, top;
        /** Panel width */
        float width = 0;
        /** Panel height */
        float height = 0;

        /** Panel starting point X value */
        int startX = mBlackPoints.get(0).x;
        /** Panel starting point Y value */
        int startY = mBlackPoints.get(0).y;

        /** Vertical difference between point 1 and point 2 */
        float diffY1 = Math.abs(mBlackPoints.get(1).y - mBlackPoints.get(0).y);
        /** Horizontal difference between point 1 and point 2 */
        float diffX1 = Math.abs(mBlackPoints.get(1).x - mBlackPoints.get(0).x);

        if (diffX1 > SHAPE_THRESHOLD || diffY1 > SHAPE_THRESHOLD) {
            Log.d(TAG, "proper shape detected 1");
            if (diffX1 > diffY1 ) {
                width = diffX1;
            } else {
                height = diffY1;
            }
        }

        /** Vertical difference between point 3 and point 4 */
        float diffY2 = Math.abs(mBlackPoints.get(3).y - mBlackPoints.get(2).y);
        /** Horizontal difference between point 3 and point 4 */
        float diffX2 = Math.abs(mBlackPoints.get(3).x - mBlackPoints.get(2).x);

        if (Math.abs(diffX2) > SHAPE_THRESHOLD || Math.abs(diffY2) > SHAPE_THRESHOLD) {
            Log.d(TAG, "proper shape detected 2");
            if (diffX2 > diffY2 ) {
                width = diffX2;
            } else {
                height = diffY2;
            }
        }

        left = mBlackPoints.get(0).x;
        top = mBlackPoints.get(0).y;

        for (int i = 0; i < mBlackPoints.size(); i++) {
            left = left > mBlackPoints.get(i).x ? mBlackPoints.get(i).x:left;
            top = top > mBlackPoints.get(i).y ? mBlackPoints.get(i).y:top;
        }
        if(width > 0 && height > 0) {
            Panel panel = new Panel(getContext(), new Point(left, top), (int) height, (int) width, mPanels.size());
            mPanels.add(panel);
            mDrawings.add(new Pair(panel, PANEL));
            Log.d(TAG, "rect added");
            mBlackPoints.clear();
        } else {
            mBlackPoints.clear();
            Log.d(TAG, "points cleared");

        }
    }

    /**
     * Try to select a panel at the provided location by checking if a panel exists there.
     * This check is performed after a double tap on the canvas.
     *
     * @param x X value of the double tap
     * @param y Y value of the double tap
     * @return Panel that was found or null
     */
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

    /**
     * Remove everything that was drawn on this page.
     */
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

    /**
     * Used to (re)start the surface thread
     * @param holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surface created.." + thread.getState().toString());

        if (thread.getState() == Thread.State.TERMINATED){
            Log.d(TAG, "surface created.. terminated .. restarting");

            thread = new EditSurfaceThread(getHolder(), getContext());
            thread.setRunning(true);
            thread.start();
        } else {
            Log.d(TAG, "surface created.. new ..");

            thread.setRunning(true);
            // thread.start();
        }
    }

    /**
     * Set the height of the canvas.
     * @param holder
     * @param format
     * @param width
     * @param height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setSurfaceSize(width, height);
    }

    /**
     * Destroy the surface when the activity is destroyed.
     * @param holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        surfaceCreated = false;
        thread.setRunning(false);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        Log.d(TAG, "window lost focus..");

        if (!hasWindowFocus) thread.setRunning(false);
    }

    /**
     * Restart the surface after {@link com.bdumeljic.comicbook.EditFragment} has been paused.
     */
    public void onResumeMySurfaceView(){
        Log.d(TAG, "resuming thread..");

        surfaceCreated(getHolder());
    }

    /**
     * Restart the surface after {@link com.bdumeljic.comicbook.EditFragment} has been paused.
     */
    public void onPauseMySurfaceView(){
        Log.d(TAG, "pausing thread..");

        // boolean retry = true;
        thread.setRunning(false);

        //while (retry) {
        try {
            Log.d(TAG, "trying to destroy surface");
            thread.join();
            //       retry = false;
        } catch (InterruptedException e) {
        }
        //}
    }
}
