package com.bdumeljic.comicbook;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Toast;


public class EditActivity extends Activity implements NavigationDrawerFragment.NavigationDrawerCallbacks, PagesPresetsFragment.OnFragmentInteractionListener, PagesFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    public static final int PAGES = 0;
    public static final int PRESETS = 1;
    public static final int SETTINGS = 2;

    PagesFragment pages;
    PagesPresetsFragment presets;
    SettingsFragment settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);


        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new EditFragment())
                    .commit();
        }

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        pages = new PagesFragment();
        presets = new PagesPresetsFragment();
        settings = new SettingsFragment();
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the drawer content by replacing fragments

        Fragment fragment = null;

        switch (position) {
            case PAGES:
                if(pages != null) {
                    fragment = pages;
                } else {
                    fragment = new PagesFragment();
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

        Toast.makeText(getBaseContext(), "Navigation item selected, number: " + String.valueOf(position), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onFragmentInteraction(String id) {

    }
}
