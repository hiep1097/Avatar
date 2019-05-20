package com.example.avatar.models;

public class ItemFullImage {
    private String url;
    private String name;
    private String size;

    public ItemFullImage() {
    }

    public ItemFullImage(String url, String name, String size) {
        this.url = url;
        this.name = name;
        this.size = size;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
