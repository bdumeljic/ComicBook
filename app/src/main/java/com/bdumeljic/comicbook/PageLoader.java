package com.bdumeljic.comicbook;

import android.os.AsyncTask;
import android.util.Log;

import com.bdumeljic.comicbook.Models.Page;

public class PageLoader extends AsyncTask<Void, Void, EditSurfaceView> {
    private static final String TAG = "PageLoader";

    private Page page;
    private EditSurfaceView mEditSurfaceView;

    public PageLoader(Page page, EditSurfaceView view) {
        this.page = page;
        this.mEditSurfaceView = view;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        mEditSurfaceView.getThread().setRunning(false);
    }

    @Override
    protected EditSurfaceView doInBackground(Void... params) {

        page.loadPageInfo();
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
