package com.bdumeljic.comicbook.Models;

import java.util.ArrayList;


public class PageModel {

    public static class Page {
        private ArrayList<String> mPanels;
        private int mLayoutPreset = -1;

        public Page() {
            this.mPanels = new ArrayList<String>();
        }

        private void addPanel() {
            mPanels.add("lala");
        }

        public ArrayList getPanels() {
            return mPanels;
        }
    }
}
