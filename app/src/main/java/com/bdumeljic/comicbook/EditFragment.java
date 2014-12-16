package com.bdumeljic.comicbook;


import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


/**
 * Fragment that holds the {@link com.bdumeljic.comicbook.EditView} that is used for drawing.
 * </p>
 * Use the {@link EditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditFragment extends Fragment {

    public static final String TAG = "EditModeFragment";

    /** View that holds the {@link com.bdumeljic.comicbook.EditView} */
    private View mDecorView;

    /** Surface used for drawing, this is one page in the comic book volume */
    private EditView mEditView;
   // private EditSurfaceThread mEditSurfaceThread;

    /** Listener used to detect the undo and redo actions. */
    OnEdgeSwipeTouchListener onSwipeTouchListener;

    public static EditFragment newInstance() {
        return new EditFragment();
    }

    public EditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        mEditView = (EditView) view.findViewById(R.id.surface);
      //  mEditSurfaceThread = mEditSurfaceView.getThread();

        mEditView.refreshDrawableState();

        onSwipeTouchListener = new OnEdgeSwipeTouchListener() {

            @Override
            public void onSwipeLeft() {
            }

            @Override
            public void onSwipeRight() {
            }
        };

        mEditView.setFocusable(true);
        view.setOnTouchListener(onSwipeTouchListener);


        return view;
    }

    /**
     * Hide the system bars on android versions above KitKat, otherwise use fullscreen.
     * */
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

        mEditView.refreshDrawableState();
    }
}
