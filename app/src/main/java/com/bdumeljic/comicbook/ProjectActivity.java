package com.bdumeljic.comicbook;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

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
    ListAdapter mVolAdapter;

    AlertDialog alertDialog;

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

    /**
     * Handles the interaction that happened in the fragment that it controls.
     *
     * This is called when a project has been selected. This opens a dialog that enables the user to choose a volume to edit. After a volume has been selected, this starts the edit activity.
     *
     * @param projectId Selected project id
     */
    @Override
    public void onFragmentInteraction(final long projectId) {
        mVolNames = new ArrayList<String>();

        final Project project = Project.findById(Project.class, projectId);



        Log.e("PA", "p id provided " + String.valueOf(projectId) );
        Log.e("PA", " p id " + String.valueOf(project.getId()));
        Log.e("PA", " volumes found " + project.getVolumes().size() );
        Log.e("PA p", "trying to get volumes for project: " + project.toString());

        for (Volume vol : project.getVolumes()) {
            Log.e("PA loop", vol.toString() + " p " + vol.getProjectId() + " id " + vol.getId() + " t " + vol.getTitle());

            if (vol.getTitle() != null) {
                mVolNames.add(vol.getTitle());

            }
        }


        final View volumeDialogView = getLayoutInflater().inflate(R.layout.dialog_volume, null);

        mVolAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, mVolNames);

        final EditText newVolTitle = (EditText) volumeDialogView.findViewById(R.id.new_vol_title);
        final View newVol = volumeDialogView.findViewById(R.id.add_vol);
        TextView newVolAddButton = (TextView) volumeDialogView.findViewById(R.id.add_new_volume);
        newVolAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volumeDialogView.findViewById(R.id.add_new_volume_button).setVisibility(View.VISIBLE);
                newVol.setVisibility(View.GONE);

                String title = newVolTitle.getText().toString();
                mVolNames.add(title);
                Volume newVol = project.addVolume(title);
                newVol.addPage();

                newVolTitle.getText().clear();
                alertDialog.getListView().setAdapter(new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, android.R.id.text1, mVolNames));
            }
        });

        TextView addVolButton = (TextView) volumeDialogView.findViewById(R.id.add_new_volume_button);
        addVolButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volumeDialogView.findViewById(R.id.add_new_volume_button).setVisibility(View.GONE);
                newVol.setVisibility(View.VISIBLE);
                newVolTitle.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(newVolTitle, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        alertDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_volumes)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // here you can add functions
                        dialog.dismiss();
                    }
                })
                .setAdapter(mVolAdapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        Volume vol = null;
                        ArrayList<Volume> vols = (ArrayList<Volume>) project.getVolumes();

                        Log.d("ProjectActivity", "looking for vol " + mVolNames.get(which));
                        for (Volume volume : vols) {
                            if (mVolNames.get(which).equals(volume.getTitle())) {
                                vol = volume;
                                Log.d("ProjectActivity", "found it");
                            }
                        }

                        Log.d("ProjectActivity", "passing to edit " + projectId + " " + vol.getId());

                        Intent editIntent = new Intent(getBaseContext(), EditActivity.class);
                        editIntent.putExtra(PROJECT, project.getId());
                        editIntent.putExtra(VOLUME, vol.getId());
                        startActivity(editIntent);
                    }
                })
                .setView(volumeDialogView)
                .create();

        alertDialog.show();

    }
}
