package com.bdumeljic.comicbook.Models;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.ArrayList;


public class Page extends SugarRecord<Page> {
    private long id;

    private long number;
    private long volumeId;

    @Ignore
    private ArrayList<Panel> mPanels;
    @Ignore
    private int mLayoutPreset = -1;

    public Page() {

    }

    public Page(int num, long volId) {
        this.number = num;
        this.volumeId = volId;

        this.mPanels = new ArrayList<Panel>();
    }

    public void addPanel(Panel pan) {
        pan.setPageId(getId());
        pan.save();
        mPanels.add(pan);
    }

    public void loadPageInfo() {
        //Log.e("PAGE", "loaded succesfully, returning");
    }

    public ArrayList<Panel> getPanels() {
        Log.e("PANEL", "starting getting panels: " + mPanels);
        if (mPanels != null) {
            return mPanels;
        }

        this.mPanels = (ArrayList<Panel>) Panel.find(Panel.class, "page_id = ?", String.valueOf(getId()));
        Log.e("PANEL", "got panels: " + mPanels.size());

        return mPanels;
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
