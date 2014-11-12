package com.bdumeljic.comicbook;


import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditFragment extends Fragment {

    public static final String TAG = "EditModeFragment";

    public final static String PROJECT = "param_project";
    public final static String VOLUME = "param_volume";

    private int mProject;
    private int mVolume;

    private View mDecorView;

    private EditSurfaceView mEditSurfaceView;
    private EditSurfaceView.EditSurfaceThread mEditSurfaceThread;



    /**
     * Start a new EditFragment in which the page layout of a specified volume of a comic book can be edited.
     *
     * @param param1 Project ID
     * @param param2 Volume ID
     * @return A new instance of fragment EditFragment in which a volume of a comic book series can be edited.
     */
    // TODO: Rename and change types and number of parameters
    public static EditFragment newInstance(int param1, int param2) {
        EditFragment fragment = new EditFragment();
        Bundle args = new Bundle();
        args.putInt(PROJECT, param1);
        args.putInt(VOLUME, param2);
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
            mProject = getArguments().getInt(PROJECT, -1);
            mVolume = getArguments().getInt(VOLUME, -1);
        }

        if (mProject < 0 || mVolume < 0) {
            getActivity().finish();
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mDecorView = getActivity().getWindow().getDecorView();
        mDecorView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int i) {
                        int height = mDecorView.getHeight();
                        Log.i(TAG, "Current height: " + height);

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                hideSystemUI();
                            }
                        }, 5500);
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
        mEditSurfaceThread.setState(EditSurfaceView.EditSurfaceThread.STATE_READY);

        mEditSurfaceView.refreshDrawableState();

        return view;
    }

    // This hides the system bars
    public void hideSystemUI() {
        // Hide all the system bars so the user can focus on the drawing
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);

        mEditSurfaceView.refreshDrawableState();
    }



    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mEditSurfaceView.onResumeMySurfaceView();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mEditSurfaceView.getThread().pause();
        mEditSurfaceView.onPauseMySurfaceView();

    }



}
