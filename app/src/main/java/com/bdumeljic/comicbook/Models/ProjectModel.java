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
        VolumeModel.Volume v = new VolumeModel.Volume(2, "Vol 2");
        v.addPage();
        v.addPage();
        v.addPage();
        v.addPage();

        p.addVolume(v);
        p.addVolume(new VolumeModel.Volume(3, "Vol 3"));

        Project p2 = new Project(PROJECTS.size(), "Grayson");
        addProject(p2);
        p2.addVolume(new VolumeModel.Volume(2, "Vol 2"));
        p2.addVolume(new VolumeModel.Volume(3, "Vol 3"));

        Project p3 = new Project(PROJECTS.size(), "Superman");
        addProject(p3);
        p3.addVolume(new VolumeModel.Volume(2, "Vol 2"));
        p3.addVolume(new VolumeModel.Volume(3, "Vol 3"));

        Project p4 = new Project(PROJECTS.size(), "Superman");
        addProject(p4);
        p4.addVolume(new VolumeModel.Volume(2, "Vol 2"));
        p4.addVolume(new VolumeModel.Volume(3, "Vol 3"));

        Project p5 = new Project(PROJECTS.size(), "Superman");
        addProject(p5);
        p5.addVolume(new VolumeModel.Volume(2, "Vol 2"));
        p5.addVolume(new VolumeModel.Volume(3, "Vol 3"));

        Project p6 = new Project(PROJECTS.size(), "Superman");
        addProject(p6);
        p6.addVolume(new VolumeModel.Volume(2, "Vol 2"));
        p6.addVolume(new VolumeModel.Volume(3, "Vol 3"));

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

        public int getProjectId() {
            return this.id;
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
