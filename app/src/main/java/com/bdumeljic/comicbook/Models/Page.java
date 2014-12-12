package com.bdumeljic.comicbook.Models;

import android.util.Log;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.ArrayList;

/**
 * Page Model
 * </p>
 * Represents one page in a volume.
 */
public class Page extends SugarRecord<Page> {
    private long id;

    private long number;
    private long volumeId;

    @Ignore
    public ArrayList<Panel> mPanels;
    @Ignore
    private int mLayoutPreset = -1;

    public Page() {

    }

    /**
     * Creat a new page using the page number and the volume id
     * @param num
     * @param volId
     */
    public Page(int num, long volId) {
        this.number = num;
        this.volumeId = volId;

        this.mPanels = new ArrayList<Panel>();
    }

    public void addPanel(Panel pan) {
        pan.setPageId(getId());
        pan.save();
    }


    public ArrayList<Panel> getPanels() {
        if (mPanels != null) {
            return mPanels;
        }

        this.mPanels = (ArrayList<Panel>) Panel.find(Panel.class, "page_id = ?", String.valueOf(getId()));

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

}
