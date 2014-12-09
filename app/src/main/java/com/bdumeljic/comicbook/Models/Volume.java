package com.bdumeljic.comicbook.Models;

import android.util.Log;

import com.orm.SugarRecord;

import java.util.List;

public class Volume extends SugarRecord<Volume> {
    public long id;
    public long volumeId;
    private String title;
    private long projectId;

    public Volume() {

    }

    public Volume(long volId, String title, long projectID) {
        this.volumeId = volId;
        this.title = title;
        this.projectId = projectID;
        addPage();
        addPage();
        addPage();

    }

    public String getTitle() {
        return title;
    }

    public long getVolumeId() {
        return volumeId;
    }

    public long getProjectId() {
        return projectId;
    }

    public void addPage() {
        int pageNum = (int) Page.count(Page.class, "volume_id = ?", new String[]{String.valueOf(volumeId)});
        Page page = new Page(pageNum + 1, volumeId);
        page.save();

        Log.e("VOL ADD PAGE", "added page with num " + String.valueOf(pageNum) + " to vol " + volumeId);

    }

    public List<Page> getPages() {
        return Page.find(Page.class, "volume_id = ?", String.valueOf(volumeId));
    }

    public Page getPage(long num) {
        return Page.find(Page.class, "volume_id = ? and number = ?", new String[]{String.valueOf(volumeId), String.valueOf(num)}).get(0);
    }

    @Override
    public String toString() {
        return "Volume id: " + String.valueOf(volumeId) + " with title: " + title + " of project: " + projectId;
    }
}