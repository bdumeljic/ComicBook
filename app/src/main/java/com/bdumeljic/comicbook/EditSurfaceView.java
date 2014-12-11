package com.bdumeljic.comicbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.bdumeljic.comicbook.Models.Handle;
import com.bdumeljic.comicbook.Models.Panel;

import java.util.ArrayList;

/**
 * Custom {@link android.view.SurfaceView} used for drawing the page layout. One surface is one page in a comic book volume.
 */
public class EditSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final int MARGIN = 20;
    private static final int DISTPANELS = 10;
    private String TAG = "EditSurfaceView";
    private Rect lastNotIntersecting;

    boolean surfaceCreated;
    private int prevX;
    private int prevY;
    private boolean isPanelSelected = false;
    private int mTouchSlop = -1;

    private ArrayList<Handle> resizeHandles;
    int groupId = -1;
    private int handleId = -1;
    private boolean isHandleTouched;
    // variable to know what ball is being dragged

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

    GestureDetector gestureDetector;

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

        gestureDetector = new GestureDetector(context, new GestureListener());

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
    /**
     * Handle all the touch events on the canvas, e.g. drawing and selecting panels.
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //double tap check
        gestureDetector.onTouchEvent(event);
        Log.d("DRAWING", "Panel not selected? " + isPanelSelected);

        if(!isPanelSelected){
            drawPath(event);
        }
        else if(isPanelSelected){
            onDragMovePanel(event);
        }
        return true;
    }

    private boolean drawPath(MotionEvent event) {
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();

        switch(action) {
            case MotionEvent.ACTION_DOWN:
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
                touch_up();
                invalidate();
                break;
        }

        return true;
    }

    private boolean onDragMovePanel(MotionEvent event) {
        int positionX = (int) event.getX();
        int positionY = (int) event.getY();


        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN: // touch down so check if the finger is on
                if(!isHandleTouched){
                    detectTouchedHandle(positionX, positionY);
                }
                if(handleId == -1 && !(selectedPanel.getDefinedRect().contains(positionX, positionY))) {
                    //Deselection of Panel
                    Log.d("DESELECTION", "Deselection");
                    selectedPanel = null;
                }
                // Remember where we started (for dragging)
                prevX = positionX;
                prevY = positionY;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = positionX - prevX;
                int deltaY = positionY - prevY;

                int mScreenWidth  = this.getWidth();
                int mScreenHeight  = this.getHeight();
                // Check if we have moved far enough that it looks more like a
                // scroll than a tap
                if (selectedPanel != null && (deltaX > mTouchSlop || deltaY > mTouchSlop)) {
                    // Check if delta is added, is the rectangle is within the visible screen
                    if (handleId > -1) {
                        // move the balls the same as the finger

                        if ( positionX > MARGIN && positionX < this.getWidth() - MARGIN && positionY > MARGIN && positionY < this.getHeight() - MARGIN) {
                            resizeHandles.get(handleId).setX(positionX);
                            resizeHandles.get(handleId).setY(positionY);
                            if (groupId == 1) {
                                //
                                resizeHandles.get(1).setX(resizeHandles.get(0).getX());
                                resizeHandles.get(1).setY(resizeHandles.get(2).getY());
                                resizeHandles.get(3).setX(resizeHandles.get(2).getX());
                                resizeHandles.get(3).setY(resizeHandles.get(0).getY());
                            } else {
                                resizeHandles.get(0).setX(resizeHandles.get(1).getX());
                                resizeHandles.get(0).setY(resizeHandles.get(3).getY());
                                resizeHandles.get(2).setX(resizeHandles.get(3).getX());
                                resizeHandles.get(2).setY(resizeHandles.get(1).getY());
                            }
                            computeResizedRect(positionX, positionY);
                        }
                    }else if((selectedPanel.getDefinedRect().left+ deltaX) > MARGIN && ((selectedPanel.getDefinedRect().right +deltaX) < mScreenWidth - MARGIN )  && ((selectedPanel.getDefinedRect().top +deltaY > MARGIN ) && (selectedPanel.getDefinedRect().bottom+deltaY < mScreenHeight - MARGIN))) {
                        selectedPanel.getDefinedRect().offset(deltaX, deltaY);
                        setHandlesToRectBounds(selectedPanel.getDefinedRect());
                    }
                }
                prevX = positionX;
                prevY = positionY;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if(selectedPanel == null){
                    isPanelSelected = false;
                }
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
     * Handle the end of a touch or draw.
     */
    private void touch_up() {
        mCurrentPath.lineTo(mX, mY);

        if (isDrawingModeBlue()) {
            mBluePaths.add(mCurrentPath);
            mDrawings.add(new Pair(mCurrentPath, BLUEPATH));
        }

        // Check for double tap
        if(isDrawingModeBlack()) {
            mBlackPaths.add(mCurrentPath);
            mBlackPoints.add(new Point((int) mX, (int) mY));

            if (mBlackPoints.size() >= 4) {
                check_shape();
                mBlackPaths.clear();
            }

        }

        mCurrentPath = new Path();

    }

    /**
     * Undo the previous drawing action. Inform the user of this action.
     */
    public void onClickUndo() {
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
                    canvas.drawRect(panel.getDefinedRect(),  mBlackPaint);
                }
            }
        }

        if (isDrawingModeBlack()) {
            // First draw all the panels.
            for (Panel panel : mPanels) {
                canvas.drawRect(panel.getDefinedRect(),  mBlackPaint);
            }

            // If a panel is selected redraw it with the selected paint.
            if (selectedPanel != null) {
                canvas.drawRect(selectedPanel.getDefinedRect(),  mSelectedPaint);
                for(Handle ball : resizeHandles){
                    canvas.drawBitmap(ball.getBitmap(),ball.getX(),ball.getY(), mSelectedPaint);
                }
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
            for(Panel oldPanel : mPanels){

                Rect oldPanelRect = oldPanel.getDefinedRect();
                Rect newPanelRect = panel.getDefinedRect();

                if(newPanelRect.intersects(oldPanelRect.left, oldPanelRect.top, oldPanelRect.right, oldPanelRect.bottom)) {
                    if (newPanelRect.centerX() > oldPanelRect.centerX()) {
                        //shift to the right side
                        newPanelRect.left = oldPanelRect.right + DISTPANELS;
                        break;
                    }
                    if (newPanelRect.centerX() < oldPanelRect.centerX()) {
                        //shift to the left side
                        newPanelRect.right = oldPanelRect.left - DISTPANELS;
                        break;
                    }
                }


            }
            //check if inside border
            if(panel.getDefinedRect().left < MARGIN){
                panel.getDefinedRect().left = MARGIN;
            }
            if(panel.getDefinedRect().right > getWidth() - MARGIN){
                panel.getDefinedRect().right = getWidth() - MARGIN;
            }
            if(panel.getDefinedRect().top < MARGIN){
                panel.getDefinedRect().top = MARGIN;
            }
            if(panel.getDefinedRect().bottom > getHeight() - MARGIN){
                panel.getDefinedRect().bottom = getHeight() - MARGIN;
            }

            mPanels.add(panel);
            mDrawings.add(new Pair(panel, PANEL));
            Log.d(TAG, "rect added");
            mBlackPoints.clear();
        } else {
            mBlackPoints.clear();
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

        Panel foundPanel = null;

        for (Panel panel : mPanels) {
            if(panel.getDefinedRect().contains((int) x,(int)y)){
                foundPanel = panel;
            };
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

    /**
     * computation of new defined rect after resizing
     * @param touchX    x coordinate of usertouch
     * @param touchY    y coordinate of usertouch
     */
    private void computeResizedRect(int touchX, int touchY) {
        int left, top, right, bottom;

        int mScreenWidth = this.getWidth();
        int mScreenHeight = this.getHeight();

        left = resizeHandles.get(0).getX();
        top = resizeHandles.get(0).getY();
        right = resizeHandles.get(0).getX();
        bottom = resizeHandles.get(0).getY();

        for (int i = 1; i < resizeHandles.size(); i++) {
            left = left > resizeHandles.get(i).getX() ? resizeHandles.get(i).getX():left;
            top = top > resizeHandles.get(i).getY() ? resizeHandles.get(i).getY():top;
            right = right < resizeHandles.get(i).getX() ? resizeHandles.get(i).getX():right;
            bottom = bottom < resizeHandles.get(i).getY() ? resizeHandles.get(i).getY():bottom;
        }
        // Check if rectangle is within the visible screen
        if (((left > MARGIN ) && (right < mScreenWidth - MARGIN) && ((top > MARGIN) && (bottom  < mScreenHeight - MARGIN)))) {
            // invalidate current position as we are moving...
            Rect resizedRect = new Rect(
                    left + resizeHandles.get(0).getWidthOfBall() / 2,
                    top + resizeHandles.get(0).getWidthOfBall() / 2,
                    right + resizeHandles.get(2).getWidthOfBall() / 2,
                    bottom + resizeHandles.get(2).getWidthOfBall() / 2);

            for(Panel panel : mPanels){
               if(!(panel.getDefinedRect().intersects(resizedRect.left, resizedRect.top, resizedRect.right, resizedRect.bottom))){

                   selectedPanel.setDefinedRect(resizedRect);
               }
            }
            setHandlesToRectBounds(selectedPanel.getDefinedRect());
            invalidate();
            prevX = touchX;
            prevY = touchY;

        }

    }

    /**
     * Detects if selected rectangles handle was touched and sets fields handleId and isHandleTouched accordingly
     *
     * @param   rect   rect to check for intersection side
     */
    private int detectIntersectedSide(Rect rect, Rect intersectedRect){
                if(rect.left < intersectedRect.right + DISTPANELS  ){
                    //colliding on left side
                    return 0;
                }if(rect.right > intersectedRect.left - DISTPANELS ){
                    //colliding on right side
                    return 1;
                }if(rect.top < intersectedRect.bottom + DISTPANELS ){
                    //colliding on top side
                    return 2;
                }if(rect.bottom > intersectedRect.top - DISTPANELS ){
                    //colliding on bottom side
                    return 3;
                }
        return -1;
    }
    /**
     * Detects if selected rectangles handle was touched and sets fields handleId and isHandleTouched accordingly
     *
     * @param   positionX   x Coordinate of touchposition
     * @param   positionY   y Coordinate of touchposition
     */
    private void detectTouchedHandle(int positionX, int positionY){
        handleId = -1;
        groupId = -1;
        for (Handle handle : resizeHandles) {
            // check if inside the bounds of the ball (circle)
            // get the center for the ball
            int centerX = handle.getX() + handle.getWidthOfBall();
            int centerY = handle.getY() + handle.getHeightOfBall();
            // calculate the radius from the touch to the center of the
            // ball
            double radCircle = Math
                    .sqrt((double) (((centerX - positionX) * (centerX - positionX)) + (centerY - positionY)
                            * (centerY - positionY)));

            if (radCircle < handle.getWidthOfBall() + 20) {
                //set selected handle Id
                handleId = handle.getID();
                if (handleId == 1 || handleId == 3) {
                    groupId = 2;
                } else {
                    groupId = 1;
                }
                invalidate();
                break;
            }
            invalidate();
        }
    }
    private void showResizeHandles() {
        Rect selectedRect = selectedPanel.getDefinedRect();
        resizeHandles = new ArrayList<Handle>();
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_resize_bubble);
        int bitmapWidth = bitmap.getWidth(); //Width = Height as Bitmap is a Circle
        resizeHandles.add(new Handle(getContext(), bitmap, new Point(selectedRect.left - bitmapWidth / 2, selectedRect.bottom - bitmapWidth / 2), 0));
        resizeHandles.add(new Handle(getContext(), bitmap, new Point(selectedRect.left - bitmapWidth / 2, selectedRect.top - bitmapWidth / 2), 1));
        resizeHandles.add(new Handle(getContext(), bitmap, new Point(selectedRect.right - bitmapWidth / 2, selectedRect.top - bitmapWidth / 2), 2));
        resizeHandles.add(new Handle(getContext(), bitmap, new Point(selectedRect.right - bitmapWidth / 2, selectedRect.bottom - bitmapWidth / 2), 3));
        invalidate();
    }

    private void setHandlesToRectBounds(Rect rect){
        int widthOfHandle = resizeHandles.get(0).getWidthOfBall()/2;
        resizeHandles.get(0).setToCorner(rect.left- widthOfHandle, rect.bottom - widthOfHandle);
        resizeHandles.get(1).setToCorner(rect.left - widthOfHandle, rect.top - widthOfHandle);
        resizeHandles.get(2).setToCorner(rect.right - widthOfHandle, rect.top - widthOfHandle);
        resizeHandles.get(3).setToCorner(rect.right - widthOfHandle, rect.bottom - widthOfHandle);
    }
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        // event when double tap occurs
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();
            selectedPanel = try_to_find_panel(x,y);
            if(selectedPanel != null){
               isPanelSelected = true;
               showResizeHandles();
            }
            Log.d("Double Tap", "Tapped at: (" + x + "," + y + ")");

            return true;
        }
    }
}
