package com.jcanseco.radio.models;

import com.google.gson.annotations.SerializedName;

public class Dj {

    @SerializedName("djname")
    private String name;

    @SerializedName("id")
    private int id;


    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getAvatarUrl() {
        return String.format("http://r-a-d.io/api/dj-image/%s", id);
    }
}
