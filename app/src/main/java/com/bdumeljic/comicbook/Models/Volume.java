package com.bdumeljic.comicbook.Models;

import com.orm.SugarRecord;

import java.util.ArrayList;

public class Volume extends SugarRecord<Volume> {
    private String mVolName;
    private ArrayList<PageModel.Page> mPages;

    public Volume() {

    }

    public Volume(String title) {
        this.mVolName = title;
        this.mPages = new ArrayList<PageModel.Page>();

        this.addPage();
    }

    public String getVolName() {
        return mVolName;
    }


    public void addPage() {
        mPages.add(new PageModel.Page());
    }

    public ArrayList getPages() {
        return mPages;
    }

    public PageModel.Page getPage(int num) {
        return mPages.get(num);
    }

}

