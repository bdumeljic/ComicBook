package com.bdumeljic.comicbook.Models;

import android.util.Log;

import com.orm.SugarRecord;

import java.util.List;

public class Project extends SugarRecord<Project> {
    private long id;

    private String title;

    public Project() {

    }

    public Project(String title) {
        this.title = title;
    }

    public String getProjectName() {
        return this.title;
    }

    /**
     * Add a volume to this project.
     * @param title Volume title
     * @return The created volume
     */
    public Volume addVolume(String title) {
        long vId = Volume.count(Volume.class, "project_id = ?", new String[]{String.valueOf(getId())});
        Volume volume = new Volume(vId, title, getId());
        volume.save();
        return volume;
    }

     public Volume getVolume(long volId) {
        return Volume.find(Volume.class, "project_id = ? and volume_id = ?", new String[]{String.valueOf(getId()), String.valueOf(volId)}).get(0);
    }

    /**
     * Retrieve all the volumes for this project.
     * @return An arraylist of volumes
     */
    public List<Volume> getVolumes() {
        return Volume.find(Volume.class, "project_id = ?", String.valueOf(getId()));
    }

    @Override
    public String toString() {
        return "Project with id: " + getId() + " with title: " + title + " and number or vols: " + this.getVolumes().size();
    }
}
