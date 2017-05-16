package com.example.administrator.justfortest2.util;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.administrator.justfortest2.HourlyInfo;
import com.example.administrator.justfortest2.R;

import java.util.List;

/**
 * Created by Administrator on 2017/5/16.
 */

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.ViewHolder> {

    private List<HourlyInfo> mHourlyList;

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView hourlyTemp;
        ImageView hourlyImage;
        TextView hourlyDate;
        public ViewHolder(View view){
            super(view);
            hourlyTemp = (TextView) view.findViewById(R.id.hourly_temp);
            hourlyImage = (ImageView) view.findViewById(R.id.hourly_image);
            hourlyDate = (TextView) view.findViewById(R.id.hourly_date);
        }
    }

    public HourlyAdapter(List<HourlyInfo> hourlyList){
        mHourlyList = hourlyList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hourly_item,parent,false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HourlyInfo hourlyInfo = mHourlyList.get(position);
        holder.hourlyTemp.setText(hourlyInfo.getHourlyTemp());
        holder.hourlyDate.setText(hourlyInfo.getHourlyDate());
        holder.hourlyImage.setImageResource(hourlyInfo.getImageId());
    }

    @Override
    public int getItemCount() {
        return mHourlyList.size();
    }
}

