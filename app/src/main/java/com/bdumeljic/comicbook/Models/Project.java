package com.bdumeljic.comicbook.Models;

import com.orm.SugarRecord;

import java.util.ArrayList;

public class Project extends SugarRecord<Project> {
        private int id;
        private String title;
        private ArrayList<Volume> mVolumes;

        public Project() {

        }

        public Project(String title, String volume) {
            this.id = Project.listAll(Project.class).size();
            this.title = title;
            this.mVolumes = new ArrayList<Volume>();
            addVolume(new Volume(1, volume));
        }

        public String getProjectName() {
            return title;
        }

        public void addVolume(Volume vol) {
            this.mVolumes.add(vol);
        }

        public ArrayList getVolumes() {
            return new ArrayList<Volume>();
        }

        public Volume getVolume(int id) {
            return this.mVolumes.get(id);
        }

        public int getProjectId() {
            return this.id;
        }
    }
