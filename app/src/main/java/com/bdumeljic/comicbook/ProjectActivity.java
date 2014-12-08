package com.bdumeljic.comicbook;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
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
     * @param project Selected project
     */
    @Override
    public void onFragmentInteraction(final Project project) {
        mVolNames = new ArrayList<String>();

        mVolNames.add("fkfkf");
        mVolNames.add("fsgg");
        for (Volume vol : (ArrayList<Volume>) project.getVolumes()) {
            if (vol.getVolName() != null) {
                mVolNames.add(vol.getVolName());
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

                mVolNames.add(newVolTitle.getText().toString());
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

        showVolumeDialog(project, volumeDialogView);

    }

    public void showVolumeDialog(final Project project, View view) {
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

                        Intent editIntent = new Intent(getBaseContext(), EditActivity.class);
                        editIntent.putExtra(PROJECT, project.getProjectId());
                        editIntent.putExtra(VOLUME, which);
                        startActivity(editIntent);
                    }
                })
                .setView(view)
                .create();

        alertDialog.show();
    }

}
