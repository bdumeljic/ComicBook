package com.bdumeljic.comicbook;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.bdumeljic.comicbook.Models.Project;
import com.bdumeljic.comicbook.Models.Volume;

import java.util.ArrayList;

/**
 *  Activity that controls the selection of projects and volumes.
 *
 *  This is the launcher activity.
 */
public class ProjectActivity extends ActionBarActivity implements ProjectFragment.OnFragmentInteractionListener {

    public final static String PROJECT = "param_project";
    public final static String VOLUME = "param_volume";

    private ArrayList<String> mVolNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ProjectFragment())
                    .commit();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.project, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    /**
     * Handles the interaction that happened in the fragment that it controls.
     *
     * This is called when a project has been selected. This opens a dialog that enables the user to choose a volume to edit. After a volume has been selected, this starts the edit activity.
     *
     * @param project Selected project
     */
    @Override
    public void onFragmentInteraction(final Project project) {
        mVolNames = new ArrayList<String>();

        for (Volume vol : (ArrayList<Volume>) project.getVolumes()) {
            if (vol.getVolName() != null) {
                mVolNames.add(vol.getVolName());
            }
        }

        ListAdapter mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, mVolNames);

        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_volumes)

                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // here you can add functions
                        dialog.dismiss();
                    }
                })
                .setAdapter(mAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item

                        Intent editIntent = new Intent(getBaseContext(), EditActivity.class);
                        editIntent.putExtra(PROJECT, project.getProjectId());
                        editIntent.putExtra(VOLUME, which);
                        startActivity(editIntent);
                    }
                })
                .create();

        alertDialog.show();
    }

}
