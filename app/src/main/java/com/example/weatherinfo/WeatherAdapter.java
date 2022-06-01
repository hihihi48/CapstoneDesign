package com.example.weatherinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WeatherAdapter extends RecyclerView.Adapter<myViewHolder> {
    List<WeatherItem> list;

    public WeatherAdapter(List<WeatherItem> list){
        this.list=list;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subweather_item,parent,false);
        return new myViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, final int position){
        holder.date.setText(list.get(position).tvDate);
        holder.image.setImageResource(list.get(position).image);
        holder.huminity.setText(list.get(position).tvHumidity);
        holder.temp.setText(list.get(position).tvTemp);
        holder.weather.setText(list.get(position).tvWeather);
    }

    @Override
    public int getItemCount(){
        return list.size();
    }
}

class myViewHolder extends RecyclerView.ViewHolder{
    ImageView image;
    TextView temp, huminity, date, weather;

    public myViewHolder(@NonNull View itemView){
        super(itemView);
        date = itemView.findViewById(R.id.tvDate);
        image = itemView.findViewById(R.id.imgWeather);
        temp = itemView.findViewById(R.id.tvTemp);
        huminity = itemView.findViewById(R.id.tvHumidity);
        weather = itemView.findViewById(R.id.tvWeather);
    }

    public void getWeatherImage(String sky){

    }
}
