package com.bdumeljic.comicbook;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;


/**
 * A fragment used in the sliding drawer in the {@link com.bdumeljic.comicbook.EditActivity}.
 * This fragment is used to set the drawing settings while editing a comic book volume.
 * </p>
 *
 * Activities that contain this fragment must implement the
 * {@link SettingsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SettingsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    public SettingsFragment() {
        // Required empty public constructor
    }

    public static final int BLUE = 0;
    public static final int BLACK = 1;
    public static final int CLEAR = 2;
    public static final int SAVE = 3;

    /** Used to toggle the visibility of blue ink. */
    public Switch mBlueToggle;
    /** Used to toggle the visibility of black ink. */
    public Switch mBlackToggle;
    /** Used to clear the whole page. */
    public Button mClearButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mBlueToggle = (Switch) view.findViewById(R.id.toggleButtonBlueInk);
        mBlackToggle = (Switch) view.findViewById(R.id.toggleButtonBlackInk);

        mBlackToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mListener != null) {
                    mListener.onFragmentInteraction(BLACK, isChecked);
                }
            }
        });

        mBlueToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mListener != null) {
                    mListener.onFragmentInteraction(BLUE, mBlueToggle.isChecked());
                }
            }
        });

        mClearButton = (Button) view.findViewById(R.id.clear);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onFragmentInteraction(CLEAR, false);
                }
            }
        });


        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * Pass interactions with the settings fragment back to the {@link com.bdumeljic.comicbook.EditActivity}.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(int type, Boolean bool);
    }

}
