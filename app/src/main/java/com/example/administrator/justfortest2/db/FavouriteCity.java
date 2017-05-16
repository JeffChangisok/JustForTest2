package com.example.administrator.justfortest2.db;

import org.litepal.crud.DataSupport;

/**
 * Created by Administrator on 2017/5/8.
 */

public class FavouriteCity extends DataSupport{
    private int id;
    private String name;
    private String weather;
    private String weatherId;
    private String caiweather;

    public String getCaiweather() {
        return caiweather;
    }

    public void setCaiweather(String caiweather) {
        this.caiweather = caiweather;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }
}
