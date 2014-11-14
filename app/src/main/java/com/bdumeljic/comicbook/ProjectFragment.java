package com.bdumeljic.comicbook;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.bdumeljic.comicbook.Models.ProjectModel;

import java.util.ArrayList;
import java.util.Random;

/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ProjectFragment extends Fragment implements AbsListView.OnItemClickListener {

    private OnFragmentInteractionListener mListener;

    private Random random = new Random();

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mGridView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayList<ProjectModel.Project> mProjects;
    private ListAdapter mAdapter;

    public static ProjectFragment newInstance(String param1, String param2) {
        ProjectFragment fragment = new ProjectFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProjectFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProjects = ProjectModel.getProjects();

        // TODO: Change Adapter to display your content
       // mAdapter = new ArrayAdapter<ProjectModel.Project>(getActivity(),
         //       android.R.layout.simple_list_item_1, android.R.id.text1, ProjectModel.getProjects());

        mAdapter = new ProjectsAdapter(mProjects);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project, container, false);

        // Set the adapter
        mGridView = (GridView) view.findViewById(R.id.projects);
        mGridView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mGridView.setOnItemClickListener(this);

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


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("PROJECT", "project clicked " + mAdapter.getItem(position).toString());

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.

            mListener.onFragmentInteraction(mProjects.get(position));
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mGridView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(ProjectModel.Project p);
    }

    protected class ProjectsAdapter extends BaseAdapter {

        private ArrayList<ProjectModel.Project> mProjects;

        public ProjectsAdapter(ArrayList<ProjectModel.Project> projects) {
            super();
            this.mProjects = projects;
        }

        @Override
        public int getCount() {
            return mProjects.size();
        }

        @Override
        public ProjectModel.Project getItem(int position) {
            return mProjects.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int row, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_project, parent, false);
            }

            int[] card_colors = getResources().getIntArray(R.array.project_card_colors);
            int color = card_colors[random.nextInt(card_colors.length)];

            CardView card = (CardView) convertView.findViewById(R.id.project_card);
            card.setBackgroundColor(color);

            TextView mNameText = (TextView) convertView.findViewById(R.id.project_name);
            mNameText.setText(mProjects.get(row).getProjectName());

            return convertView;
        }
    }
}
