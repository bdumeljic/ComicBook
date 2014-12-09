package com.bdumeljic.comicbook;


import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.bdumeljic.comicbook.Models.Page;
import com.bdumeljic.comicbook.Models.Project;
import com.bdumeljic.comicbook.Models.Volume;


/**
 * Fragment that holds the {@link com.bdumeljic.comicbook.EditSurfaceView} that is used for drawing.
 * </p>
 * Use the {@link EditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditFragment extends Fragment {

    public static final String TAG = "EditModeFragment";

    public final static String PROJECT = "param_project";
    public final static String VOLUME = "param_volume";

    /** Project being edited */
    private long mProjectId;
    /** Volume being edited */
    private long mVolumeId;

    public Volume volume;
    public Page currentPage;

    /** View that holds the {@link com.bdumeljic.comicbook.EditSurfaceView} */
    private View mDecorView;

    /** Surface used for drawing, this is one page in the comic book volume */
    private EditSurfaceView mEditSurfaceView;
    private EditSurfaceView.EditSurfaceThread mEditSurfaceThread;

    /** Listener used to detect the undo and redo actions. */
    OnEdgeSwipeTouchListener onSwipeTouchListener;

    public final int BLUE = 0;
    public final int BLACK = 1;

    /**
     * Start a new EditFragment in which the page layout of a specified volume of a comic book can be edited.
     *
     * @param param1 Project ID
     * @param param2 Volume ID
     * @return A new instance of fragment EditFragment in which a volume of a comic book series can be edited.
     */
    // TODO: Rename and change types and number of parameters
    public static EditFragment newInstance(long param1, long param2) {
        EditFragment fragment = new EditFragment();
        Bundle args = new Bundle();
        args.putLong(PROJECT, param1);
        args.putLong(VOLUME, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public EditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mProjectId = getArguments().getLong(PROJECT, -1);
            mVolumeId = getArguments().getLong(VOLUME, -1);
        }

        if (mProjectId < 0 || mVolumeId < 0) {
            getActivity().finish();
        }

        volume = Project.findProject(mProjectId).getVolume(mVolumeId);
        currentPage = volume.getPage(1);
    }

    /**
     * Enter fullscreen immersive more to provide more room for the content.
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDecorView = getActivity().getWindow().getDecorView();
        mDecorView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int i) {
                        int height = mDecorView.getHeight();
                        Log.i(TAG, "Current height: " + height + " i " + i);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideSystemUI();
                            }
                        }, 4000);
                    }
                });

        hideSystemUI();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);

        mEditSurfaceView = (EditSurfaceView) view.findViewById(R.id.surface);
        mEditSurfaceThread = mEditSurfaceView.getThread();

        mEditSurfaceView.refreshDrawableState();

        onSwipeTouchListener = new OnEdgeSwipeTouchListener() {

            @Override
            public void onSwipeLeft() {
                Log.d("EDGESWIPE", "left");
                mEditSurfaceView.onClickUndo();
            }

            @Override
            public void onSwipeRight() {
                Log.d("EDGESWIPE", "right");
                mEditSurfaceView.onClickRedo();
            }
        };

        mEditSurfaceView.setFocusable(true);
        view.setOnTouchListener(onSwipeTouchListener);

        Button blueButton = (Button) view.findViewById(R.id.button_blue);
        Button blackButton = (Button) view.findViewById(R.id.button_black);

        Button undoButton = (Button) view.findViewById(R.id.undoBtn);
        Button redoButton = (Button) view.findViewById(R.id.redoBtn);

        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditSurfaceView.setDrawingMode(BLUE);
            }
        });

        blackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditSurfaceView.setDrawingMode(BLACK);
            }
        });
        
        currentPage = volume.getPage(1);
        currentPage.loadPage();

        return view;
    }

    /** Hide the system bars */
    public void hideSystemUI() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            // Hide all the system bars so the user can focus on the drawing
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }

        mEditSurfaceView.refreshDrawableState();
    }

    /**
     * Get the surface view that is being drawn on.
     * @return {@link com.bdumeljic.comicbook.EditSurfaceView}
     */
    public EditSurfaceView getSurfaceView() {
        return mEditSurfaceView;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "resuming ..");

        mEditSurfaceView.onResumeMySurfaceView();
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "pausing ..");

        mEditSurfaceView.onPauseMySurfaceView();
        super.onPause();
    }

    public void changePage(long num) {
        currentPage = volume.getPage(num);
        currentPage.loadPage();
        mEditSurfaceView.setBluePaths(currentPage.getBlueLines());
        mEditSurfaceView.setPanels(currentPage.getPanels());

        Toast.makeText(getActivity(), "changed page", Toast.LENGTH_SHORT).show();

    }

    public void savePage() {
        Log.e(TAG, mEditSurfaceView.mBluePaths.toString());
        currentPage.savePage(mEditSurfaceView.mPanels, mEditSurfaceView.mBluePaths);
        Toast.makeText(getActivity(), "Saved page", Toast.LENGTH_SHORT).show();
    }
}
