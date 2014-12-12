package com.bdumeljic.comicbook.Models;

import android.util.Log;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;
import com.orm.query.Condition;
import com.orm.query.Select;

import java.util.ArrayList;
import java.util.List;

/**
 * Model that represents a volume.
 */
public class Volume extends SugarRecord<Volume> {
    /** Overall volume id from the database. */
    private long id;

    /** Id of volume within a project. */
    public long volumeId;
    /** Volume title */
    private String title;
    /** Id of the project this volume belongs to. */
    private long projectId;

    public Volume() {

    }

    /**
     * Creates a new volume by providing the project id, volume id withing that project and title.
     * @param volId
     * @param title
     * @param projectID
     */
    public Volume(long volId, String title, long projectID) {
        this.volumeId = volId;
        this.title = title;
        this.projectId = projectID;
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

    /**
     * Add a page to this volume.
     */
    public void addPage() {
        int pageNum = (int) Page.count(Page.class, "volume_id = ?", new String[]{String.valueOf(getId())});
        Page page = new Page(pageNum + 1, getId());
        page.save();

        Log.e("VOL ADD PAGE", "added page with num " + String.valueOf(pageNum + 1) + " to vol " + getId() + " which is vol " + volumeId + " in project");

    }

    /**
     * Retrieve all the pages from this volume.
     * @return An arraylist of pages
     */
    public ArrayList<Page> getPages() {
        return (ArrayList<Page>) Page.find(Page.class, "volume_id = ?", String.valueOf(this.getId()));
    }

    /**
     * Get a specific page from this volume.
     * @param num Number of the page
     * @return The page
     */
    public Page getPage(long num) {
        Log.e("VOL", "getting page for vol" + volumeId + "with id " + getId());
        Select specificPageQuery = Select.from(Page.class)
                .where(Condition.prop("volume_id").eq(getId()),
                        Condition.prop("number").eq(num))
                .limit(String.valueOf(1));

        Page page = (Page) specificPageQuery.first();
        //return Page.find(Page.class, "volume_id = ? and number = ?", new String[]{String.valueOf(this.id), String.valueOf(num)}).get(0);
        return page;
    }

    @Override
    public String toString() {
        return "Volume id: " + String.valueOf(getId()) + " volId: " + String.valueOf(volumeId) + " with title: " + title + " of project: " + projectId;
    }
}