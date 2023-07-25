package com.example.zenaparty.models;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("current")
    private WeatherData current;

    // Constructor, getters, and setters

    public WeatherData getCurrent() {
        return current;
    }

    public void setCurrent(WeatherData current) {
        this.current = current;
    }
}
