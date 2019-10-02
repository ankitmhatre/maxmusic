package com.dragonide.maxmusic;

/**
 * Created by Ankit on 5/26/2017.
 */

public class SongModel {
    String title, artist, album, path;
    String time;

    public SongModel(String title, String album, String artist, String time, String path) {
        this.title = title;
        this.time = time;
        this.album = album;
        this.artist = artist;
        this.path = path;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
