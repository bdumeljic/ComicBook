package com.bdumeljic.comicbook;

import android.os.AsyncTask;
import android.util.Log;

import com.bdumeljic.comicbook.Models.Page;
import com.bdumeljic.comicbook.Models.Panel;

import java.util.ArrayList;

public class PageLoader extends AsyncTask<Void, Void, EditSurfaceView> {
    private static final String TAG = "PageLoader";

    private Page page;
    private EditSurfaceView mEditSurfaceView;
    private ArrayList<Panel> panels;

    public PageLoader(Page page, EditSurfaceView view) {
        this.page = page;
        this.mEditSurfaceView = view;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        //mEditSurfaceView.getThread().setRunning(false);
    }

    @Override
    protected EditSurfaceView doInBackground(Void... params) {
        //panels = (ArrayList<Panel>) Panel.find(Panel.class, "page_id = ?", String.valueOf(page.getId()));
        return null;
    }

    @Override
    protected void onPostExecute(EditSurfaceView editSurfaceView) {
        super.onPostExecute(editSurfaceView);

        //mEditSurfaceView.getThread().setRunning(true);

        //mEditSurfaceView.setBluePaths();
        //mEditSurfaceView.setPanels();

       // mEditSurfaceView.invalidate();

        Log.e(TAG, "done loading");

    }
}
