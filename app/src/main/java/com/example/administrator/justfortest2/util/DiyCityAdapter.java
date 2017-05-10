package com.example.administrator.justfortest2.util;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.administrator.justfortest2.DiyCity;
import com.example.administrator.justfortest2.R;

import java.util.List;

/**
 * Created by Administrator on 2017/5/3.
 */

public class DiyCityAdapter extends RecyclerView.Adapter<DiyCityAdapter.ViewHolder>{

    private Context mContext;
    private List<DiyCity> mDiyCityList;


    /**
     * 重写自定义ViewHolder
     */
    static class ViewHolder extends RecyclerView.ViewHolder{
        CardView cardView;
        TextView cityName;

        public ViewHolder(View view){
            super(view);
            cardView = (CardView) view;
            cityName = (TextView) view.findViewById(R.id.city_name);
        }
    }

    public DiyCityAdapter(List<DiyCity> diyCityList){
        mDiyCityList = diyCityList;
    }

    /**
     * 给ViewHolder设置布局元素
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(mContext == null){
            mContext = parent.getContext();
        }
        View view = LayoutInflater.from(mContext).inflate(R.layout.city_item,parent,false);
        return new ViewHolder(view);
    }

    /**
     * ViewHolder设置元素
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        DiyCity diyCity = mDiyCityList.get(position);
        holder.cityName.setText(diyCity.getCityName());
    }

    @Override
    public int getItemCount() {
        return mDiyCityList.size();
    }
}
