package com.example.administrator.justfortest2;

/**
 * Created by Administrator on 2017/5/16.
 */

public class HourlyInfo {
    private String hourlyDate;
    private String hourlyTemp;
    private int imageId;

    public HourlyInfo(String hourlyTemp, String hourlyDate, int imageId) {
        this.hourlyDate = hourlyDate;
        this.hourlyTemp = hourlyTemp;
        this.imageId = imageId;
    }

    public String getHourlyDate() {
        return hourlyDate;
    }

    public void setHourlyDate(String hourlyDate) {
        this.hourlyDate = hourlyDate;
    }

    public String getHourlyTemp() {
        return hourlyTemp;
    }

    public void setHourlyTemp(String hourlyTemp) {
        this.hourlyTemp = hourlyTemp;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}
