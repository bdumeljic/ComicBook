package com.bdumeljic.comicbook.Models;

import java.util.ArrayList;

/**
 * Created by bojana on 29/10/14.
 */
public class ProjectModel {

    public static ArrayList<Project> PROJECTS = new ArrayList<Project>();

    static {
        Project p = new Project(PROJECTS.size(), "Superman");
        addProject(p);
        p.addVolume(new VolumeModel.Volume(2, "Vol 2"));
        p.addVolume(new VolumeModel.Volume(3, "Vol 3"));

        Project p2 = new Project(PROJECTS.size(), "Grayson");
        addProject(p2);
        p2.addVolume(new VolumeModel.Volume(2, "Vol 2"));
        p.addVolume(new VolumeModel.Volume(3, "Vol 3"));
    }

    public static class Project {
        private int id;
        private String name;
        private ArrayList<VolumeModel.Volume> mVolumes;

        public Project(int id, String name) {
            this.id = id;
            this.name = name;
            this.mVolumes = new ArrayList<VolumeModel.Volume>();
            addVolume(new VolumeModel.Volume(1, "Vol 1"));
        }

        public String getProjectName() {
            return name;
        }

        public void addVolume(VolumeModel.Volume vol) {
            this.mVolumes.add(vol);
        }

        public ArrayList getVolumes() {
            return this.mVolumes;
        }

        public VolumeModel.Volume getVolume(int id) {
            return this.mVolumes.get(id);
        }


    }

    private static void addProject(Project project) {
        PROJECTS.add(project);

    }

    public static ArrayList<Project> getProjects() {
        return PROJECTS;
    }

    public static Project getProject(int id) {
        return PROJECTS.get(id);
    }
}
