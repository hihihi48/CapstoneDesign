package com.example.weatherinfo;

import android.graphics.drawable.Drawable;

import java.util.Calendar;
import java.util.Date;

public class WeatherItem {
    int image;
    String tvTemp;
    String tvHumidity;
    String tvWeather;
    String tvDate;

    public WeatherItem(String tvDate, int image, String tvTemp, String tvHumidity, String tvWeather){
        this.tvDate=tvDate;
        this.image=image;
        this.tvTemp=tvTemp;
        this.tvHumidity=tvHumidity;
        this.tvWeather=tvWeather;
    }

    public String getTvDate(){
        return tvDate;
    }

    public int getImage() {
        return image;
    }

    public String getTvTemp() {
        return tvTemp;
    }

    public String getTvHumidity() {
        return tvHumidity;
    }

    public String getTvWeather() {
        return tvWeather;
    }

    public void setTvDate(String tvDate){this.tvDate=tvDate;}

    public void setImage(int image) {
        this.image = image;
    }

    public void setTvTemp(String tvTemp) {
        this.tvTemp = tvTemp;
    }

    public void setTvHumidity(String tvHumidity) {
        this.tvHumidity = tvHumidity;
    }

    public void setTvWeather(String tvTime) {
        this.tvWeather = tvTime;
    }
}
