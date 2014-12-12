package com.bdumeljic.comicbook;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.bdumeljic.comicbook.Models.Page;
import com.bdumeljic.comicbook.Models.Project;

import java.util.ArrayList;

/**
 * Fragment used for switching between volume pages.
 * </p>
 * Representing a list of pages that are in the volume that is being edited.
 * <p/>
 * A fragment used in the sliding drawer in the {@link com.bdumeljic.comicbook.EditActivity}.
 * This fragment is used to set the drawing settings while editing a comic book volume.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class PagesFragment extends ListFragment {

    public final static String PROJECT = "param_project";
    public final static String VOLUME = "param_volume";

    String TAG = "PagesFragment";

    private long mProject;
    private long mVolume;

    private OnFragmentInteractionListener mListener;

    /** Adapter holding the pages. */
    PagesAdapter mPagesAdapter;
    ArrayList<Page> mPages;

    /**
     * Create a new instance of the fragment with the provided project ID and volume ID.
     *
     * @param project Project id
     * @param volume Volume id
     * @return Fragment with a list populated by the pages of the provided project and volume.
     */
     public static PagesFragment newInstance(long project, long volume) {
        PagesFragment fragment = new PagesFragment();
        Bundle args = new Bundle();
        args.putLong(PROJECT, project);
        args.putLong(VOLUME, volume);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PagesFragment() {
    }

    /**
     * Retrieve the pages for the specified project's volume and populate the list's adapter with these pages.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mProject = getArguments().getLong(PROJECT, -1);
            mVolume = getArguments().getLong(VOLUME, -1);
        }

       mPages = Project.findProject(mProject).getVolume(mVolume).getPages();

        mPagesAdapter = new PagesAdapter(mPages);
        setListAdapter(mPagesAdapter);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pages, container, false);

        Button button = (Button) view.findViewById(R.id.add_new_page_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    Log.e(TAG, "page selected: " + -1);
                    mListener.onFragmentInteraction(-1);
                }
            }
        });

        return view;
    }

    public void update() {
        mPages = Project.findProject(mProject).getVolume(mVolume).getPages();

        mPagesAdapter = new PagesAdapter(mPages);
        setListAdapter(mPagesAdapter);
        getListView().invalidate();
        getListView().setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        getListView().setStackFromBottom(true);

    }

    @Override
    public void onStart() {
        setSelection(0);
        super.onStart();
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


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            long number = mPagesAdapter.getItem(position).getNumber();
            Log.e(TAG, "page selected: " + number);
            mListener.onFragmentInteraction(number);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(long num);
    }

    /**
     * Adapter that holds a list of pages for the specified project's volume .
     */
    protected class PagesAdapter extends BaseAdapter {

        private ArrayList<Page> mPages;

        /**
         * Create a new PagesAdapter with the provided list of pages.
         * @param pages List of pages
         */
        public PagesAdapter(ArrayList<Page> pages) {
            super();
            this.mPages = pages;
        }

        @Override
        public int getCount() {
            return mPages.size();
        }

        @Override
        public Page getItem(int position) {
            return mPages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int row, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_page, parent, false);
            }

            //CardView card = (CardView) convertView.findViewById(R.id.page_card);
            //card.setBackgroundColor(color);

            TextView mNumText = (TextView) convertView.findViewById(R.id.page_num);
            mNumText.setText(String.valueOf(mPages.get(row).getNumber()));

            return convertView;
        }
    }

}
