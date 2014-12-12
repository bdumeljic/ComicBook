package com.bdumeljic.comicbook.Models;

import android.util.Log;

import com.orm.SugarRecord;

import java.util.List;

public class Project extends SugarRecord<Project> {
    private long id;

    private long projectId;
    private String title;

    public Project() {

    }

    public Project(long projectId, String title, String volume) {
        this.projectId = projectId;
        this.title = title;
        Volume first = addVolume(volume);
        first.addPage();
        first.addPage();
        first.addPage();

    }

    public String getProjectName() {
        return this.title;
    }

    public Volume addVolume(String title) {
        long vId = Volume.count(Volume.class, "project_id = ?", new String[]{String.valueOf(projectId)});
        Volume volume = new Volume(vId, title, projectId);
        volume.save();

        Log.e("PROJECT ADD VOL", "id " + volume.getId() + "added vol with " + String.valueOf(vId) + " title " + title + " to project " + projectId);
        return volume;
    }

     public Volume getVolume(long volId) {
        return Volume.find(Volume.class, "project_id = ? and volume_id = ?", new String[]{String.valueOf(projectId), String.valueOf(volId)}).get(0);
    }


    public List<Volume> getVolumes() {
        Log.e("PROJECT", "getting volumes for project " + projectId);
        return Volume.find(Volume.class, "project_id = ?", String.valueOf(projectId));
    }

    public long getProjectId() {
        return projectId;
    }

    @Override
    public String toString() {
        return "Project with id: " + projectId + " with title: " + title + " and number or vols: " + this.getVolumes().size();
    }

    public static Project findProject(long projectId) {
        return Project.find(Project.class, "project_id = ?", String.valueOf(projectId)).get(0);
    }
}
