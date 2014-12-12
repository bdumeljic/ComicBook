package com.bdumeljic.comicbook.Models;

import android.content.Context;
import android.graphics.Path;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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


        //Log.e("PAGE", "loaded succesfully, returning");
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
