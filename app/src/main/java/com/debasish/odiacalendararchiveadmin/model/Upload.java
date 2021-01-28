package com.debasish.odiacalendararchiveadmin.model;

import com.google.firebase.database.Exclude;

//model for adapter
public class Upload {
    private String name;
    private String month;
    private int year;
    private String imageUrl;
    private String key;

    public Upload() {
        //empty constructor important for fetching data from database
    }

    public Upload(String name, String month, int year, String imageUrl) {
        this.name = name;
        this.month = month;
        this.year = year;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    //Exclude these items while fetching from adapter
    @Exclude
    public String getKey() {
        return key;
    }

    @Exclude
    public void setKey(String key) {
        this.key = key;
    }
}
