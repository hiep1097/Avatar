package com.example.avatar.models;

public class ItemCropImage {
    private String id;
    private String path;

    public ItemCropImage() {
    }

    public ItemCropImage(String id, String path) {
        this.id = id;
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
