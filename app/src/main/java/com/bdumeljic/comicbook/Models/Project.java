package com.bdumeljic.comicbook.Models;

import android.util.Log;

import com.orm.SugarRecord;

import java.util.ArrayList;
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
            addVolume(volume);
        }

        public String getProjectName() {
            return this.title;
        }

        public void addVolume(String title) {
            long vid = Volume.count(Volume.class, "project_id = ?", new String[]{String.valueOf(projectId)});
            Volume volume = new Volume(vid, title, projectId);
            volume.save();

            Log.e("PROJECT ADD VOL", "added vol with " + String.valueOf(vid) + " title " + title + " to project " + projectId);
            List<Volume> vols = Volume.listAll(Volume.class);
            if (vols.size() > 0){
                for (Volume vol : vols) {
                    Log.e("ADDED VOL", vol.toString());
                }
            }
        }

         public Volume getVolume(long volId) {
            return Volume.find(Volume.class, "project_id = ? and vol_id = ?", new String[]{String.valueOf(projectId), String.valueOf(volId)}).get(0);
        }


        public java.util.List<Volume> getVolumes() {
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
}
