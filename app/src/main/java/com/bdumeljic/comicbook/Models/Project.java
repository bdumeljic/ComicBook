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

    public Volume addVolume(String title) {
        long vId = Volume.count(Volume.class, "project_id = ?", new String[]{String.valueOf(getId())});
        Volume volume = new Volume(vId, title, getId());
        volume.save();

        Log.e("PROJECT ADD VOL", "id " + volume.getId() + "added vol with " + String.valueOf(vId) + " title " + title + " to project " + getId());
        return volume;
    }

     public Volume getVolume(long volId) {
        return Volume.find(Volume.class, "project_id = ? and volume_id = ?", new String[]{String.valueOf(getId()), String.valueOf(volId)}).get(0);
    }


    public List<Volume> getVolumes() {
        Log.e("PROJECT", "getting volumes for project " + getId());
        return Volume.find(Volume.class, "project_id = ?", String.valueOf(getId()));
    }

    @Override
    public String toString() {
        return "Project with id: " + getId() + " with title: " + title + " and number or vols: " + this.getVolumes().size();
    }
}
