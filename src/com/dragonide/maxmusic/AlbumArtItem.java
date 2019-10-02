package com.dragonide.maxmusic;

import android.graphics.Bitmap;

/**
 * Created by Ankit on 4/11/2017.
 */

public class AlbumArtItem {
    private Bitmap bm;
    private String reso;
    private String url;

    public AlbumArtItem() {

    }

    public AlbumArtItem(Bitmap bm, String name, String url) {
        this.bm = bm;
        this.reso = name;
        this.url = url;
    }


    public Bitmap getBm() {
        return bm;
    }

    public void setBm(Bitmap bm) {
        this.bm = bm;
    }

    public String getReso() {
        return reso;
    }

    public void setReso(String reso) {
        this.reso = reso;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
