package com.bdumeljic.comicbook;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
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
import com.bdumeljic.comicbook.Models.Page;
import com.bdumeljic.comicbook.Models.Panel;
import com.bdumeljic.comicbook.Models.Project;
import com.bdumeljic.comicbook.Models.Volume;

import java.util.ArrayList;

/**
 * Custom {@link android.view.SurfaceView} used for drawing the page layout. One surface is one page in a comic book volume.
 */
public class EditSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final int MARGIN = 20;
    private static final int DISTPANELS = 10;
    private String TAG = "EditSurfaceView";

    private int prevX;
    private int prevY;
    private boolean isPanelSelected = false;
    private int mTouchSlop = -1;

    private ArrayList<Handle> resizeHandles;
    int groupId = -1;
    private int handleId = -1;
    private boolean isHandleTouched;
    private Rect borderRect;
    private Paint gridLinePaint;
    // variable to know what ball is being dragged

    public Page currentPage;
    public Volume volume;

    private long projectId;
    private long volumeId;
    private long pageNum;

    /**
     * Thread used for managing the {@link com.bdumeljic.comicbook.EditSurfaceView}.
     */
    class EditSurfaceThread extends Thread {

        private final SurfaceHolder mSurfaceHolder;
        private EditSurfaceView mEditSurfaceView;

        /** Variable that keeps track if the surface thread is running or not. */
        private boolean mRun = false;
        private final Object mRunLock = new Object();

        private int mCanvasHeight = 1;
        private int mCanvasWidth = 1;

        public EditSurfaceThread(SurfaceHolder surfaceHolder, EditSurfaceView surfaceView) {
            // get handles to some important objects
            this.mSurfaceHolder = surfaceHolder;
            this.mEditSurfaceView = surfaceView;
        }

        /**
         * Change if the surface is running or not. Used to pause on resume.
         *
         * @param running Boolean value
         */
        public void setRunning(boolean running) {
            mRun = running;
        }

        @SuppressLint("WrongCall")
        @Override
        public void run() {
            if (mRun) {
            }

            while (mRun) {
                Canvas canvas = null;
                try {
                    canvas = mSurfaceHolder.lockCanvas();
                    synchronized (mSurfaceHolder) {

                        // If mRun has been toggled false, inhibit canvas operations.
                      //  synchronized (mRunLock) {
                            if (mRun) {
                                mEditSurfaceView.onDraw(canvas);
                                postInvalidate();
                      //      }
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

    private int mCanvasWidth = 0;
    private int mCanvasHeight = 0;

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
    ArrayList<Panel> mPanels = new ArrayList<Panel>();

    /** List of the paths that were drawn, but have been undone. Used for the redo function. */
    private ArrayList<Path> undonePaths = new ArrayList<Path>();

    /** Reference to the selected panel. */
    private Panel selectedPanel;

    public EditSurfaceView(Context context) {
        super(context);

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

        thread = new EditSurfaceThread(surfaceHolder, this);
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
        mSelectedPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mSelectedPaint.setColor(getResources().getColor(R.color.accent_lighter));
        mSelectedPaint.setStrokeWidth(6f);

        gridLinePaint = new Paint();
        initPaint(gridLinePaint);
        gridLinePaint.setColor(Color.LTGRAY);
        gridLinePaint.setStrokeWidth(3);
        gridLinePaint.setStyle(Paint.Style.STROKE);
        gridLinePaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

        mCurrentPath = new Path();

        thread.setRunning(true);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCanvasWidth = w;
        mCanvasHeight = h;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * Define panels regarding to preset options
     * @param presetNumber
     */
    public void computePreset(int presetNumber) {
        clearPage();
        deletePageContents();
        drawing_mode = BLACK;
        ArrayList<Rect> rects = new ArrayList<Rect>();
        int screenHeight = getHeight();
        int screenWidth = getWidth();
        int heightRect;
        int widthRect;
        switch(presetNumber) {
            case 0:
                heightRect = (screenHeight - DISTPANELS) / 2 - MARGIN;
                widthRect = (screenWidth - DISTPANELS) / 2 - MARGIN;

                rects.add(new Rect(MARGIN, MARGIN, MARGIN + widthRect, MARGIN + heightRect));
                rects.add(new Rect(MARGIN + widthRect + DISTPANELS, MARGIN, screenWidth - MARGIN, MARGIN + heightRect));
                rects.add(new Rect(MARGIN, MARGIN + heightRect + DISTPANELS, MARGIN + widthRect, screenHeight - MARGIN));
                rects.add(new Rect(MARGIN + widthRect + DISTPANELS, MARGIN + heightRect + DISTPANELS, screenWidth - MARGIN, screenHeight - MARGIN));

                for (int i = 0; i < 4; i++) {
                    Panel panel =  new Panel(rects.get(i).left, rects.get(i).top, rects.get(i).height(), rects.get(i).width());
                    mPanels.add(i, panel);
                    currentPage.addPanel(panel);
                    mDrawings.add(new Pair(panel, PANEL));
                }
                break;
            case 1:
                heightRect = screenHeight / 3 - DISTPANELS / 2 - MARGIN;
                widthRect = (screenWidth - DISTPANELS) / 2 - MARGIN;

                rects.add(new Rect(MARGIN, MARGIN, screenWidth - MARGIN, MARGIN + heightRect));
                rects.add(new Rect(MARGIN, MARGIN + heightRect + DISTPANELS, MARGIN + widthRect, MARGIN + 2*heightRect + DISTPANELS));
                rects.add(new Rect(MARGIN + widthRect + DISTPANELS, MARGIN + heightRect + DISTPANELS, screenWidth - MARGIN, MARGIN + 2*heightRect + DISTPANELS));
                rects.add(new Rect(MARGIN, screenHeight - MARGIN - heightRect, screenWidth - MARGIN, MARGIN ));

                for (int i = 0; i < 4; i++) {
                    Panel panel =  new Panel(rects.get(i).left, rects.get(i).top, heightRect, rects.get(i).width());
                    mPanels.add(i, panel);
                    currentPage.addPanel(panel);
                    mDrawings.add(new Pair(panel, PANEL));
                }
                break;
            case 2:
                heightRect = screenHeight / 3 - DISTPANELS / 2 - MARGIN;
                widthRect = (screenWidth - DISTPANELS) / 2 - MARGIN;

                rects.add(new Rect(MARGIN, MARGIN, widthRect, screenHeight - MARGIN));
                rects.add(new Rect(MARGIN + widthRect + DISTPANELS, MARGIN, screenWidth - MARGIN, MARGIN + 2*heightRect));
                rects.add(new Rect(MARGIN + widthRect + DISTPANELS, MARGIN + 2*heightRect + DISTPANELS, screenWidth - MARGIN, screenHeight - MARGIN));

                for (int i = 0; i < 3; i++) {
                    Panel panel =  new Panel(rects.get(i).left, rects.get(i).top, rects.get(i).height(), rects.get(i).width());
                    mPanels.add(i, panel);
                    currentPage.addPanel(panel);
                    mDrawings.add(new Pair(panel, PANEL));                }
                break;
        }
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
        p.setStrokeWidth(4f);
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
        Log.d("DRAWING", "Panel not selected? " + isPanelSelected);;

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
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
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
                            Log.d("DRAWING","Handle id " + handleId);
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
                break;
            case MotionEvent.ACTION_UP:
                if(selectedPanel == null){
                    isPanelSelected = false;
                } else {
                    selectedPanel.save();
                }
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
        isPanelSelected = false;
        if (mDrawings.size() > 0) {
            if(mDrawings.get(mDrawings.size()-1).second == BLUEPATH){
                mBluePaths.remove(mBluePaths.size()-1);
            }
            if(mDrawings.get(mDrawings.size()-1).second == PANEL){
                mPanels.remove(mPanels.size()-1);
            }
            
            mUndoneDrawings.add(mDrawings.remove(mDrawings.size() - 1));
            
            Toast.makeText(getContext(), "UNDO", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.no_undo), Toast.LENGTH_SHORT).show();
        }
    }
    
    /**
     * Redo a drawing action that has been undone. Inform the user of this action.
     */
    public void onClickRedo() {
        isPanelSelected = false;
        if (mUndoneDrawings.size() > 0) {
            if(mUndoneDrawings.get(mUndoneDrawings.size()-1).second == BLUEPATH){
                mBluePaths.add((Path) mUndoneDrawings.get(mUndoneDrawings.size()-1).first);
            }
            else if(mUndoneDrawings.get(mUndoneDrawings.size()-1).second == PANEL){
                mPanels.add((Panel) mUndoneDrawings.get(mUndoneDrawings.size()-1).first);
            }
            
            mDrawings.add(mUndoneDrawings.remove(mUndoneDrawings.size() - 1));
            
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
                    Log.d(TAG, "" + panel + " " + panel.getDefinedRect() + " paint " + mBlackPaint );
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
            if (isPanelSelected && selectedPanel != null) {
                canvas.drawRect(borderRect, gridLinePaint);

                canvas.drawLine(MARGIN, selectedPanel.getDefinedRect().bottom , this.getWidth(), selectedPanel.getDefinedRect().bottom , gridLinePaint);
                canvas.drawLine(selectedPanel.getDefinedRect().left, MARGIN, selectedPanel.getDefinedRect().left, this.getHeight() - MARGIN, gridLinePaint);
                canvas.drawLine(MARGIN, selectedPanel.getDefinedRect().top, this.getWidth() - MARGIN, selectedPanel.getDefinedRect().top, gridLinePaint);
                canvas.drawLine(selectedPanel.getDefinedRect().right, MARGIN, selectedPanel.getDefinedRect().right, this.getHeight() - MARGIN, gridLinePaint);

                canvas.drawRect(selectedPanel.getDefinedRect(),  mSelectedPaint);
                for(Handle ball : resizeHandles){
                    canvas.drawBitmap(ball.getBitmap(),ball.getX(),ball.getY(), mSelectedPaint);
                }
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

        try {
            Thread.sleep(75);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
            Panel panel = new Panel(left, top, (int) height, (int) width);
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
            currentPage.addPanel(panel);
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

        isPanelSelected = false;
        selectedPanel = null;

        Toast.makeText(getContext(), "Page Cleared", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
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
        thread.setRunning(false);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        Log.d(TAG, "window lost focus..");

       if (hasWindowFocus) {
           thread.setRunning(true);
           postInvalidate();
        }
    }

    /**
     * Restart the surface after {@link com.bdumeljic.comicbook.EditFragment} has been paused.
     */
    public void onResumeMySurfaceView(){
        Log.d(TAG, "resuming thread..");

        thread = new EditSurfaceThread(getHolder(), this);
    }

    /**
     * Restart the surface after {@link com.bdumeljic.comicbook.EditFragment} has been paused.
     */
    public void onPauseMySurfaceView(){

        thread.setRunning(false);

        try {
            thread.join();
        } catch (InterruptedException e) {
        }
    }

    /**
     * MODEL INTERACTION
     */


    /**
     * Set the first page upon creating an EditSurfaceView.
     * Set the project and volume that is being edited.
     * Followed by the current page that is open.
     * Load panels if there were any saved.
     *
     * @param project Project
     * @param vol
     * @param num
     */
    public void setToFirstPage(long project, long vol, long num) {

        this.projectId = project;
        this.volumeId = vol;
        this.pageNum = num;

        this.volume = Volume.findById(Volume.class, volumeId);
        this.currentPage = volume.getPage(pageNum);


        loadPageFromDB();

        Toast.makeText(getContext(), "First page", Toast.LENGTH_SHORT).show();
    }

    public void changePage(long num) {
        clearPage();
        this.pageNum = num;
        this.currentPage = volume.getPage(pageNum);

        loadPageFromDB();

        Toast.makeText(getContext(), "Changed page", Toast.LENGTH_SHORT).show();
    }

    private void loadPageFromDB() {
        //new PageLoader(currentPage, this).execute();

        ArrayList<Panel> panels = currentPage.getPanels();
        if (panels != null && !panels.isEmpty()) {
            mPanels = panels;
            setDrawingMode(BLACK);
        }
    }

    public void addPage() {
        this.volume.addPage();
    }

    public void deletePageContents() {
        Panel.deleteAll(Panel.class, "page_id = ?", String.valueOf(currentPage.getId()));
        clearPage();
    }

    /**
     * PANEL INTERACTION
     */

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
        if (((left > MARGIN  ) && (right < mScreenWidth - MARGIN) && ((top > MARGIN) && (bottom  < mScreenHeight - MARGIN)))) {
            // invalidate current position as we are moving...
            Rect resizedRect = new Rect(
                    left + resizeHandles.get(0).getWidthOfBall() / 2,
                    top + resizeHandles.get(0).getWidthOfBall() / 2,
                    right + resizeHandles.get(0).getWidthOfBall() / 2,
                    bottom + resizeHandles.get(0).getWidthOfBall() / 2);

                for (Panel panel : mPanels) {
                    if (mPanels.size() > 1 && panel.getDefinedRect().intersects(resizedRect.left, resizedRect.top, resizedRect.right, resizedRect.bottom)) {
                        Log.e(TAG, "intersect");
                    }
                    else {
                        //if nothing intersects we can set the panel to the new dimensions
                        selectedPanel.setDefinedRect(resizedRect);
                    }
                }
            setHandlesToRectBounds(selectedPanel.getDefinedRect());
            prevX = touchX;
            prevY = touchY;
            selectedPanel.save();
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
                break;
            }
        }
    }
    private void showResizeHandles() {
        Rect selectedRect = selectedPanel.getDefinedRect();
        resizeHandles = new ArrayList<Handle>();
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_resize_bubble);
        int bitmapWidth = bitmap.getWidth(); //Width = Height as Bitmap is a Circle
        resizeHandles.add(new Handle(getContext(), bitmap, new Point(selectedRect.left - bitmapWidth / 2, selectedRect.top - bitmapWidth / 2), 0));
        resizeHandles.add(new Handle(getContext(), bitmap, new Point(selectedRect.right - bitmapWidth / 2, selectedRect.top - bitmapWidth / 2), 1));
        resizeHandles.add(new Handle(getContext(), bitmap, new Point(selectedRect.right - bitmapWidth / 2, selectedRect.bottom - bitmapWidth / 2), 2));
        resizeHandles.add(new Handle(getContext(), bitmap, new Point(selectedRect.left - bitmapWidth / 2, selectedRect.bottom - bitmapWidth / 2), 3));
    }

    private void setHandlesToRectBounds(Rect rect){
        int widthOfHandle = resizeHandles.get(0).getWidthOfBall()/2;
        resizeHandles.get(0).setToCorner(rect.left- widthOfHandle, rect.top - widthOfHandle);
        resizeHandles.get(1).setToCorner(rect.right - widthOfHandle, rect.top - widthOfHandle);
        resizeHandles.get(2).setToCorner(rect.right - widthOfHandle, rect.bottom - widthOfHandle);
        resizeHandles.get(3).setToCorner(rect.left - widthOfHandle, rect.bottom - widthOfHandle);
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
               showBorders();
            }

            return true;
        }
    }

    private void showBorders() {
        borderRect = new Rect(MARGIN, MARGIN, mCanvasWidth - MARGIN, mCanvasHeight - MARGIN);
    }
}
