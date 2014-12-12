package com.bdumeljic.comicbook;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.bdumeljic.comicbook.Models.Project;

/**
 * Activity that controls the editing process. It holds all the sliding drawer fragments used in {@link com.bdumeljic.comicbook.NavigationDrawerFragment} as well as the {@link com.bdumeljic.comicbook.EditSurfaceView} that is used for drawing.
 */
public class EditActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        PagesPresetsFragment.OnFragmentInteractionListener,
        SettingsFragment.OnFragmentInteractionListener,
        PagesFragment.OnFragmentInteractionListener {


/**
 * Fragment managing the selection of the current page, page layout preset selection and drawing settings.
 */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    String TAG = "EditActivity";

    public final static String PROJECT = "param_project";
    public final static String VOLUME = "param_volume";

    public static final int PAGES = 0;
    public static final int PRESETS = 1;
    public static final int SETTINGS = 2;

    public static final int BLUE = 0;
    public static final int BLACK = 1;
    public static final int CLEAR = 2;
    public static final int SAVE = 3;


    /** Fragment used for switching between volume pages. */
    PagesFragment pages;
    /** Fragment used for selecting page layout presets. */
    PagesPresetsFragment presets;
    /** Fragment used for drawing settings. */
    SettingsFragment settings;

    /** Project that is currently open and being edited. */
    public long mProjectId;
    /** Volume that is currently open and being edited. */
    public long mVolId;

    /**
     * Open the project and volume. Set the pages of the sliding drawer {@link com.bdumeljic.comicbook.PagesFragment}.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Get the project and volume IDs that are being edited
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null)
        {
            mProjectId = extras.getLong(PROJECT);
            mVolId = extras.getLong(VOLUME);
        }

        Project p = Project.find(Project.class, "project_id = ?", String.valueOf(mProjectId)).get(0);
        getSupportActionBar().setTitle("Editing " + p.getProjectName() + ", " + "Vol. " + String.valueOf(mVolId + 1) + " " + p.getVolume(mVolId).getTitle());

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);


        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, EditFragment.newInstance(mProjectId, mVolId))
                    .commit();
        }

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        pages = PagesFragment.newInstance(mProjectId, mVolId);
        presets = new PagesPresetsFragment();
        settings = new SettingsFragment();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit, menu);
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
    }

    /**
     * Manage the fragment changes in the sliding drawer.
     * @param position
     */
    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the drawer content by replacing fragments

        Fragment fragment = null;

        switch (position) {
            case PAGES:
                if(pages != null) {
                    fragment = pages;
                } else {
                    fragment = PagesFragment.newInstance(mProjectId, mVolId);
                }
                break;
            case PRESETS:
                if(presets != null) {
                    fragment = presets;
                } else {
                    fragment = new PagesPresetsFragment();
                }
                break;
            case SETTINGS:
                if(settings != null) {
                    fragment = settings;
                } else {
                    fragment = new SettingsFragment();
                }
                break;
        }

        if(fragment != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.container_drawer, fragment)
                    .commit();
        }

        //Toast.makeText(getBaseContext(), "Navigation item selected, number: " + String.valueOf(position), Toast.LENGTH_SHORT).show();
    }

    /**
     * Manage the interactions that happened in the {@link com.bdumeljic.comicbook.NavigationDrawerFragment}.
     *
     * @param type Setting that was changed
     * @param bool
     */
    @Override
    public void onFragmentInteraction(int type, Boolean bool) {
        EditFragment editFragment = (EditFragment) getFragmentManager().findFragmentById(R.id.container);

        switch (type) {
            case BLUE:
                editFragment.getSurfaceView().toggleVisibilityBlue(bool);
                break;
            case BLACK:
                editFragment.getSurfaceView().toggleVisibilityBlack(bool);
                break;
            case CLEAR:
                editFragment.getSurfaceView().clearPage();
                break;
            case SAVE:
                editFragment.savePage();
                break;
        }
    }

    /**
     * On preset selected.
     * @param position
     */
    public void onPresetSelected(int position) {
        EditFragment editFragment = (EditFragment) getFragmentManager().findFragmentById(R.id.container);
        editFragment.getSurfaceView().computePreset(position);
    }

    /**
     * Called upon interaction with pages fragment.
     * @param num
     */
    @Override
    public void onFragmentInteraction(long num) {
        Log.e(TAG, "starting change page with num: " + num);
        EditFragment editFragment = (EditFragment) getFragmentManager().findFragmentById(R.id.container);
        editFragment.changePage(num);
    }
}
