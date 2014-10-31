package com.bdumeljic.comicbook.Models;

import java.util.ArrayList;

/**
 * Created by bojana on 29/10/14.
 */
public class VolumeModel {

    public static class Volume {
        private int mVolNum;
        private String mVolName;
        private ArrayList<PageModel.Page> mPages;

        public Volume(int num, String name) {
            this.mVolNum = num;
            this.mVolName = name;
            this.mPages = new ArrayList<PageModel.Page>();

            PageModel.Page page = new PageModel.Page(mPages.size());
            this.addPage(page);
        }

        public String getVolName() {
            return mVolName;
        }

        private void addPage(PageModel.Page page) {
            mPages.add(page);
        }

        public ArrayList getPages() {
            return mPages;
        }

        public PageModel.Page getPage(int num) {
            return mPages.get(num);
        }

    }
}
