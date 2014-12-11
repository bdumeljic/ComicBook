package com.bdumeljic.comicbook;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.bdumeljic.comicbook.Models.Project;
import com.bdumeljic.comicbook.Models.Volume;

import java.util.ArrayList;
import java.util.Random;

/**
 * A fragment representing the list of comic book projects.
 * <p />
 * Activities containing this fragment MUST implement the
 * interface to the {@link com.bdumeljic.comicbook.ProjectActivity} in order for project selection events to be handled.
 */
public class ProjectFragment extends Fragment implements AbsListView.OnItemClickListener {

    private OnFragmentInteractionListener mListener;

    private Random random = new Random();

    /**
     * The fragment's GridView containing the comic book projects.
     */
    private AbsListView mGridView;

    /**
     * The Adapter which will be used to populate the gridView with
     * Views.
     */
    public ListAdapter mAdapter;

    /**
     * List of comic book projects.
     */
    public ArrayList<Project> mProjects;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ProjectFragment() {
    }

    /**
     * Get the user's projects from the {@link com.bdumeljic.comicbook.Models.Project} and populate the adapter with these projects.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProjects = (ArrayList<Project>) Project.listAll(Project.class);
        mAdapter = new ProjectsAdapter(getActivity(), R.layout.list_item_project, mProjects);

        Log.e("PFragment", mProjects.toString());
        Log.e("PFragment", ((ArrayList<Volume>) Volume.listAll(Volume.class)).toString());


        if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        inflater.inflate(R.menu.project_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_project:
                startAddProjectDialog();
                return true;
            default:
                break;
        }

        return false;
    }

    /**
     * Inflate the gridview and add an onItemClickListener to it.
     *
     * @return The inflated view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_project, container, false);

        // Set the adapter
        mGridView = (GridView) view.findViewById(R.id.projects);
        mGridView.setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mGridView.setOnItemClickListener(this);

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            View fab = view.findViewById(R.id.fab_add_project);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startAddProjectDialog();
                }
            });
        }

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
     * Creates a dialog where the user can make a new project.
     */
    public void startAddProjectDialog() {
        View projectDialogView = getActivity().getLayoutInflater().inflate(R.layout.dialog_new_project, null);
        final EditText projectTitle = (EditText) projectDialogView.findViewById(R.id.new_project_name);
        final EditText firstVolTitle = (EditText) projectDialogView.findViewById(R.id.new_project_vol_name);


        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.new_project)
                .setView(projectDialogView)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Project project = new Project(Project.count(Project.class, null, null), projectTitle.getText().toString(), firstVolTitle.getText().toString());
                        project.save();

                        mProjects = (ArrayList<Project>) Project.listAll(Project.class);
                        mAdapter = new ProjectsAdapter(getActivity(), R.layout.list_item_project, mProjects);
                        mGridView.setAdapter(mAdapter);
                        mGridView.invalidateViews();
                    }
                })
                .create();

        alertDialog.show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d("PROJECT", "project clicked " + mAdapter.getItem(position).toString());

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            Project project = mProjects.get(position);
            long pid = project.getProjectId();
            Log.d("PROJECT", "project clicked id " + pid);

            mListener.onFragmentInteraction(pid);
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
     * This passes on the selected projects to the activity.
     * </p>
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(long p);
    }

    /**
     * Adapter that holds all the comic book projects.
     */
    public class ProjectsAdapter extends ArrayAdapter {

        private ArrayList<Project> mProjects;

        /**
         * Create a new ProjectsAdapter with the provided list of projects.
         * @param projects List of comic book projects
         */
        public ProjectsAdapter(Context context, int resource, ArrayList<Project> projects) {
            super(context, resource);
            this.mProjects = projects;
        }

        @Override
        public int getCount() {
            return mProjects.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        /**
         * Inflate one comicbook project that goes into the gridview holding the list of projects.
         * @param row
         * @param convertView
         * @param parent
         * @return
         */
        @Override
        public View getView(int row, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_project, parent, false);
            }

            CardView card = (CardView) convertView.findViewById(R.id.project_card);

            if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                int[] card_colors = getResources().getIntArray(R.array.project_card_colors);
                int color = card_colors[random.nextInt(card_colors.length)];
                card.setBackgroundColor(color);
            }

            TextView mNameText = (TextView) convertView.findViewById(R.id.project_name);
            mNameText.setText(mProjects.get(row).getProjectName());

            return convertView;
        }
    }
}
