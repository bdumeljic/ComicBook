package com.bdumeljic.comicbook.Models;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.ArrayList;

public class Volume extends SugarRecord<Volume> {
    public long id;
    public long volId;
    private String title;
    private long projectId;

    @Ignore
    private ArrayList<PageModel.Page> mPages;

    public Volume() {

    }

    public Volume(long volId, String title, long projectID) {
        this.volId = volId;
        this.title = title;
        this.projectId = projectID;
        this.mPages = new ArrayList<PageModel.Page>();

        this.addPage();
    }

    public String getTitle() {
        return title;
    }

    public long getVolId() {
        return volId;
    }

    public long getProjectId() {
        return projectId;
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

    @Override
    public String toString() {
        return "Volume id: " + String.valueOf(volId) + " with title: " + title + " of project: " + projectId;
    }
}