package com.bdumeljic.comicbook.Models;

import android.graphics.Path;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


public class Page extends SugarRecord<Page> {
    private long id;

    private long number;
    private long volumeId;
    private String panels;
    private String blueLines;

    @Ignore
    private ArrayList<Panel> mPanelsList;
    @Ignore
    private ArrayList<Path> mBlueLinesList;
    @Ignore
    private int mLayoutPreset = -1;

    public Page() {

    }

    public Page(int num, long volId) {
        this.number = num;
        this.volumeId = volId;

        this.mPanelsList = new ArrayList<Panel>();
        this.mBlueLinesList = new ArrayList<Path>();
        this.panels = "";
        this.blueLines = "";
    }

    /*private void addPanel() {
        mPanelsList.add("lala");
    }*/

    public void loadPageInfo() {
        Gson gson = new Gson();
        Type pathType = new TypeToken<ArrayList<Path>>() {}.getType();
        Type panelType = new TypeToken<ArrayList<Panel>>() {}.getType();

        Log.e("PAGE panels", "p " + panels);

        if(panels != null && !panels.isEmpty()) {
            Log.e("PAGE panels", "p " + panels);

            List<Panel> listPanels = gson.fromJson(panels, panelType);

            this.mPanelsList = new ArrayList<Panel>();
            for (Panel p : listPanels) {
                mPanelsList.add(p);
            }
            //mPanelsList.addAll(listPanels);
            Log.e("PAGE", "array of panels: " + mPanelsList.toString());
        }

        Log.e("PAGE bluelines", "b " + blueLines);

        if(blueLines != null && !blueLines.isEmpty()) {
            List<Path> listPaths = gson.fromJson(blueLines, pathType);

            this.mBlueLinesList = new ArrayList<Path>();
            for (Path p : listPaths) {
                mBlueLinesList.add(p);
            }
            //mBlueLinesList.addAll(listPaths);
            Log.e("PAGE", "array of paths: " + mBlueLinesList.toString());
        }

        Log.e("PAGE", "loaded succesfully, returning");
    }

    public void savePage(ArrayList<Panel> panels, ArrayList<Path> blueLines) {
        Gson gson = new Gson();
        Type pathType = new TypeToken<ArrayList<Path>>() {}.getType();
        Type panelType = new TypeToken<ArrayList<Panel>>() {}.getType();

        this.panels = gson.toJson(panels, panelType);
        this.blueLines = gson.toJson(blueLines, pathType);
        this.save();

        Page page = Page.find(Page.class, "volume_id = ? and number = ?", new String[]{String.valueOf(volumeId), String.valueOf(number)}).get(0);
        Log.e("PAGE", page.toString());
    }

    public ArrayList<Panel> getPanels() {
        return mPanelsList;
    }
    public ArrayList<Path> getBlueLines() {
        return mBlueLinesList;
    }

    public long getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getVolumeId() {
        return volumeId;
    }
/*
    @Override
    public String toString() {
        return "Page of vol: " + volumeId + " num: " + number + " panels " + panels + " blue " + blueLines + " ::: " + mBlueLinesList.toString() + panels.toString();
    }*/
}
