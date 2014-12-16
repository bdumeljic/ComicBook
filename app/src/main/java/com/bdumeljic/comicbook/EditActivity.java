package com.bdumeljic.comicbook;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

public class EditActivity extends ActionBarActivity {
    static String TAG = "EditActivity";

    EditView view;
    DrawModel model;
    EditSurfaceThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new DrawModel(this);
        view = new EditView(this);

        setContentView(R.layout.activity_edit);
        LinearLayout layout = (LinearLayout) findViewById(R.id.main);
        layout.addView(view);

        final FloatingActionButton actionBeautify = (FloatingActionButton) findViewById(R.id.beautify);
        actionBeautify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "starting beautify");
                model.beautify();
                ((FloatingActionsMenu) findViewById(R.id.multiple_actions)).collapse();
            }
        });

        final FloatingActionButton actionClear = (FloatingActionButton) findViewById(R.id.clear);
        actionClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "starting clear");
                model.clear();
                ((FloatingActionsMenu) findViewById(R.id.multiple_actions)).collapse();
            }
        });
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
