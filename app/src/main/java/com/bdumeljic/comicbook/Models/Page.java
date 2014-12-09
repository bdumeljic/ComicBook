package com.bdumeljic.comicbook.Models;

import android.graphics.Path;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import org.json.JSONArray;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


public class Page extends SugarRecord<Page> {
    private long id;

    private long number;
    private long volumeId;
    private String panels;
    private String blueLines;

    @Ignore
    private ArrayList<Panel> mPanels = new ArrayList<Panel>();
    @Ignore
    private ArrayList<Path> mBlueLines = new ArrayList<Path>();;
    @Ignore
    private int mLayoutPreset = -1;

    public Page() {

    }

    public Page(int num, long volId) {
        this.number = num;
        this.volumeId = volId;

    }

    /*private void addPanel() {
        mPanels.add("lala");
    }*/

    public void loadPage() {
        Gson gson = new Gson();
        Type pathType = new TypeToken<ArrayList<Path>>() {}.getType();
        Type panelType = new TypeToken<ArrayList<Panel>>() {}.getType();

        Log.e("PAGE panels", "p " + panels);
        Log.e("PAGE bluelines", "b " + blueLines);

        /*if(panels != null && !panels.isEmpty()) {
            List<Panel> listPanels = gson.fromJson(panels, panelType);

            mPanels.clear();
            mPanels.addAll(listPanels);
            Log.e("PAGE", mBlueLines.toString());
        }*/
        if(blueLines != null && !blueLines.isEmpty()) {
            List<Path> listPaths = gson.fromJson(blueLines, pathType);

            mBlueLines.clear();
            mBlueLines.addAll(listPaths);
            Log.e("PAGE", mBlueLines.toString());
        }
    }

    public void savePage(ArrayList<Panel> panels, ArrayList<Path> blueLines) {
        Gson gson = new Gson();
        Type pathType = new TypeToken<ArrayList<Path>>() {}.getType();
        Type panelType = new TypeToken<ArrayList<Panel>>() {}.getType();

        //String panelsString = gson.toJson(panels, panelType);
        String blueLinesString = gson.toJson(blueLines, pathType);

        //this.panels = panelsString;
        this.blueLines = blueLinesString;
        this.save();

        Page page = Page.find(Page.class, "volume_id = ? and number = ?", new String[]{String.valueOf(volumeId), String.valueOf(number)}).get(0);
        Log.e("PAGE", page.toString());
    }

    public ArrayList<Panel> getPanels() {
        return mPanels;
    }
    public ArrayList<Path> getBlueLines() {
        return mBlueLines;
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
        return "Page of vol: " + volumeId + " num: " + number + " panels " + panels + " blue " + blueLines + " ::: " + mBlueLines.toString() + panels.toString();
    }*/
}
