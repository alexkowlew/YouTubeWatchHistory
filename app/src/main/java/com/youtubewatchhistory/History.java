package com.youtubewatchhistory;

public class History {

    private String title, thumbnailUrl;

    public History(String title, String bitmapUrl) {
        this.title = title;
        this.thumbnailUrl = bitmapUrl;
    }

    public History() {

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
