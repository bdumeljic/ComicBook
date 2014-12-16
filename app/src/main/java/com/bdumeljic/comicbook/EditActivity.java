package com.bdumeljic.comicbook;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.LinearLayout;

public class EditActivity extends ActionBarActivity {
    static String TAG = "EditActivity";

    EditView view;
    DrawModel model;
    EditSurfaceThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new DrawModel();
        view = new EditView(this);

        setContentView(R.layout.activity_edit);
        LinearLayout layout = (LinearLayout) findViewById(R.id.main);
        layout.addView(view);

        
    }

    public DrawModel getModel() {
        return model;
    }

    public void onResume() {
        super.onResume();
        thread = new EditSurfaceThread();
        thread.start();
    }

    public void onPause() {
        super.onPause();
        thread.running = false;
        try {
            thread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Thread used for managing the {@link com.bdumeljic.comicbook.EditView}.
     */
    class EditSurfaceThread extends Thread {
        /** Variable that keeps track if the surface thread is running or not. */
        private boolean running = true;

        @Override
        public void run() {
            while (running) {
                try {
                    view.postInvalidate();
                    Thread.sleep(50);
                } catch (Exception e) {
                    System.out.println("EditSurfaceThread: " + e);
                    e.printStackTrace();
                }
            }
        }
    }
}
