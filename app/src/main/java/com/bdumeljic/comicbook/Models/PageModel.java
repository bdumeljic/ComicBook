package com.bdumeljic.comicbook.Models;

import java.util.ArrayList;

/**
 * Created by bojana on 29/10/14.
 */
public class PageModel {

    public static class Page {
        private int mPageNumber;
        private ArrayList<String> mPanels;
        private int mLayoutPreset = -1;

        public Page(int num) {
            this.mPageNumber = num;
        }

        private void addPanel() {
            mPanels.add("lala");
        }

        public ArrayList getPanels() {
            return mPanels;
        }
    }
}
