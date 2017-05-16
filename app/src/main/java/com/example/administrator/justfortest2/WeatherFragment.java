package com.example.administrator.justfortest2;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.justfortest2.db.FavouriteCity;
import com.example.administrator.justfortest2.gson.DailyTemp;
import com.example.administrator.justfortest2.gson.HourlyAndDaily;
import com.example.administrator.justfortest2.gson.HourlyTemp;
import com.example.administrator.justfortest2.gson.Result;
import com.example.administrator.justfortest2.gson.Weather;
import com.example.administrator.justfortest2.util.DateUtils;
import com.example.administrator.justfortest2.util.HourlyAdapter;
import com.example.administrator.justfortest2.util.HttpUtil;
import com.example.administrator.justfortest2.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/5/2.
 */

public class WeatherFragment extends Fragment {

    private List<HourlyInfo> hourlyList = new ArrayList<>();
    private ScrollView weatherLayout;

    public SwipeRefreshLayout swipeRefresh;
    public String mWeatherId;
    private LocalBroadcastManager localBroadcastManager;
    private LocalReceiver localReceiver;
    private IntentFilter intentFilter;

    private TextView updateTime;
    private TextView degreeText;
    private TextView weatherInfoText;

    private RecyclerView hourlyRecyclerView;
    private TextView hourlyTemp;
    private ImageView hourlyImage;
    private TextView hourlyDate;

    private LinearLayout forecastLayout;


    private TextView aqi;
    private TextView pm25;
    private TextView qlty;
    private TextView airInfo;

    private TextView comfortTitle;
    private TextView comfortInfo;
    private TextView clothTitle;
    private TextView clothInfo;
    private TextView travelTitle;
    private TextView travelInfo;
    private TextView sportTitle;
    private TextView sportInfo;

    //public String cityName;

    public WeatherFragment() {
    }


    public static WeatherFragment newInstance(String arg1, String arg2) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle bundle = new Bundle();
        bundle.putString("key", arg1);
        bundle.putString("key2", arg2);
        fragment.setArguments(bundle);
        return fragment;
    }

    String selectedWeather;
    String selectedCaiWeather;

    @Override
    public void onDestroy() {
        super.onDestroy();
        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_weather, null);
        weatherLayout = (ScrollView) view.findViewById(R.id.weather_layout);
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        updateTime = (TextView) view.findViewById(R.id.update_time);
        degreeText = (TextView) view.findViewById(R.id.degree_text);
        weatherInfoText = (TextView) view.findViewById(R.id.weather_info_text);

        hourlyRecyclerView = (RecyclerView) view.findViewById(R.id.hourly_recycler_view);
        hourlyTemp = (TextView) view.findViewById(R.id.hourly_temp);
        hourlyImage = (ImageView) view.findViewById(R.id.hourly_image);
        hourlyDate = (TextView) view.findViewById(R.id.hourly_date);

        forecastLayout = (LinearLayout) view.findViewById(R.id.forecast_layout);

        aqi = (TextView) view.findViewById(R.id.aqi);
        pm25 = (TextView) view.findViewById(R.id.pm25);
        qlty = (TextView) view.findViewById(R.id.qlty);
        airInfo = (TextView) view.findViewById(R.id.air_info);

        comfortTitle = (TextView) view.findViewById(R.id.comfort_title);
        comfortInfo = (TextView) view.findViewById(R.id.comfort_info);
        clothTitle = (TextView) view.findViewById(R.id.cloth_title);
        clothInfo = (TextView) view.findViewById(R.id.cloth_info);
        travelTitle = (TextView) view.findViewById(R.id.travel_title);
        travelInfo = (TextView) view.findViewById(R.id.travel_info);
        sportTitle = (TextView) view.findViewById(R.id.sport_title);
        sportInfo = (TextView) view.findViewById(R.id.sport_info);
        return view;
    }

    class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            swipeRefresh.setRefreshing(false);
        }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.administrator.justfortest2.STOP_REFRESH");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        selectedWeather = getArguments().getString("key");
        selectedCaiWeather = getArguments().getString("key2");
        if (selectedWeather == "") {
            //数据库里面是否有城市
            List<FavouriteCity> cities = DataSupport.findAll(FavouriteCity.class);
            if (!cities.isEmpty()) {
                //有缓存时直接解析天气数据
                Weather weather = Utility.handleWeatherResponse(cities.get(0).getWeather());
                HourlyAndDaily hourlyAndDaily = Utility.handleCaiWeatherResponse(cities.get(0).getCaiweather());
                mWeatherId = weather.basic.weatherId;
                showWeatherInfo(weather, hourlyAndDaily);
            } else {
                //无缓存时去服务器查询天气
                mWeatherId = getActivity().getIntent().getStringExtra("weather_id");
                weatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(mWeatherId);
            }

        } else {
            Weather weather = Utility.handleWeatherResponse(selectedWeather);
            HourlyAndDaily hourlyAndDaily = Utility.handleCaiWeatherResponse(selectedCaiWeather);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather, hourlyAndDaily);
        }


        // 注册下拉刷新事件
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Tabs activity = (Tabs) getActivity();
                activity.refresh(mWeatherId);
            }
        });

    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherId) {
        //应该增加一个根据天气ID从数据库查询经纬度
        String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" +
                weatherId + "&key=8c5ef408aec747eb956be39c65689b5f";
        //在utility中的解析方法中会根据返回数据的键进行处理

        showProgressDialog();

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);

                String jing = weather.basic.jing;
                String wei = weather.basic.wei;

                String caiWeatherUrl = "https://api.caiyunapp.com/v2/D99AfEnT96xj1fsy/" + jing +
                        "," + wei + "/forecast.json";

                HttpUtil.sendOkHttpRequest(caiWeatherUrl, new Callback() {

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String responseText2 = response.body().string();
                        Log.d("MyFault", "彩云请求成功");
                        final HourlyAndDaily hourlyAndDaily = Utility.handleCaiWeatherResponse(responseText2);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (weather != null && "ok".equals(weather.status) &&
                                        hourlyAndDaily != null && "ok".equals(hourlyAndDaily.status)) {
                                    //使用数据库保存天气信息
                                    FavouriteCity favouriteCity = new FavouriteCity();
                                    favouriteCity.setCaiweather(responseText2);
                                    favouriteCity.setWeather(responseText);
                                    favouriteCity.setWeatherId(weatherId);
                                    favouriteCity.setName(weather.basic.cityName);
                                    favouriteCity.save();
                                    mWeatherId = weather.basic.weatherId;
                                    showWeatherInfo(weather, hourlyAndDaily);
                                    closeProgressDialog();
                                } else {
                                    Log.d("MyFault", "weatherFragment中，处理失败 ");
                                    Log.d("MyFault", hourlyAndDaily.status);
                                }
                            }
                        });

                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Log.d("MyFault", "weatherFragment中，彩云请求失败 ");
                    }

                });

            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("MyFault", "weatherFragment中，请求失败 ");

            }

        });
    }

    /**
     * 处理并展示Weather实体类中的数据
     */
    public void showWeatherInfo(Weather weather, HourlyAndDaily hourlyAndDaily) {

        String[] strings = weather.basic.update.updateTime.split("-| ");
        StringBuilder ss = new StringBuilder();
        ss.append(strings[1]).append("月")
                .append(strings[2]).append("日").append(strings[3]).append("发布");
        updateTime.setText(ss.toString());
        String degree = weather.now.temperature + "°";
        degreeText.setText(degree);
        weatherInfoText.setText(weather.now.more.info);

        String str1;
        String str2;
        int imageId;
        List<HourlyTemp> hourlyTempList = hourlyAndDaily.result.hourly.temperature;
        List<Result.Skycon> hourlySkyconList = hourlyAndDaily.result.hourly.skycon;
        for (int i = 0; i < hourlyTempList.size(); i++) {
            str1 = String.valueOf(hourlyTempList.get(i).value) + "℃";
            str2 = hourlyTempList.get(i).datetime.split(" ")[1];
            switch (hourlySkyconList.get(i).value) {
                case "CLEAR_DAY":
                    imageId = R.drawable.sun;
                    break;
                case "CLEAR_NIGHT":
                    imageId = R.drawable.sun;
                    break;
                case "PARTLY_CLOUDY_DAY":
                    imageId = R.drawable.partly_clody;
                    break;
                case "PARTLY_CLOUDY_NIGHT":
                    imageId = R.drawable.partly_clody;
                    break;
                case "CLOUDY":
                    imageId = R.drawable.cloudy;
                    break;
                case "RAIN":
                    imageId = R.drawable.rain;
                    break;
                case "SNOW":
                    imageId = R.drawable.snow;
                    break;
                case "WIND":
                    imageId = R.drawable.wind;
                    break;
                case "FOG":
                    imageId = R.drawable.fog;
                    break;
                case "HAZE":
                    imageId = R.drawable.haze;
                    break;
                case "SLEET":
                    imageId = R.drawable.sleet;
                    break;
                default:
                    imageId = R.drawable.unknown;
            }
            HourlyInfo hourlyInfo = new HourlyInfo(str1, str2, imageId);
            hourlyList.add(hourlyInfo);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        hourlyRecyclerView.setLayoutManager(layoutManager);
        HourlyAdapter adapter = new HourlyAdapter(hourlyList);
        hourlyRecyclerView.setAdapter(adapter);

        forecastLayout.removeAllViews();
        List<DailyTemp> dailyTempList = hourlyAndDaily.result.daily.temperature;
        List<Result.Skycon> dailySkyconList = hourlyAndDaily.result.daily.skycon;
        for (int i = 0; i < dailyTempList.size(); i++) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.forecast_item,
                    forecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            ImageView forecastImage = (ImageView) view.findViewById(R.id.forecast_image);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView tempText = (TextView) view.findViewById(R.id.temp_text);
            dateText.setText(DateUtils.getWeek(dailyTempList.get(i).date));
            switch (dailySkyconList.get(i).value) {
                case "CLEAR_DAY":
                    forecastImage.setImageResource(R.drawable.sun);
                    infoText.setText("晴");
                    break;
                case "CLEAR_NIGHT":
                    forecastImage.setImageResource(R.drawable.sun);
                    infoText.setText("晴");
                    break;
                case "PARTLY_CLOUDY_DAY":
                    forecastImage.setImageResource(R.drawable.partly_clody);
                    infoText.setText("多云");
                    break;
                case "PARTLY_CLOUDY_NIGHT":
                    forecastImage.setImageResource(R.drawable.partly_clody);
                    infoText.setText("多云");
                    break;
                case "CLOUDY":
                    forecastImage.setImageResource(R.drawable.cloudy);
                    infoText.setText("阴");
                    break;
                case "RAIN":
                    forecastImage.setImageResource(R.drawable.rain);
                    infoText.setText("雨");
                    break;
                case "SNOW":
                    forecastImage.setImageResource(R.drawable.snow);
                    infoText.setText("雪");
                    break;
                case "WIND":
                    forecastImage.setImageResource(R.drawable.wind);
                    infoText.setText("有风");
                    break;
                case "FOG":
                    forecastImage.setImageResource(R.drawable.fog);
                    infoText.setText("雾");
                    break;
                case "HAZE":
                    forecastImage.setImageResource(R.drawable.haze);
                    infoText.setText("霾");
                    break;
                case "SLEET":
                    forecastImage.setImageResource(R.drawable.sleet);
                    infoText.setText("雨夹雪");
                    break;
                default:
                    forecastImage.setImageResource(R.drawable.unknown);
                    infoText.setText("未知");
            }

            StringBuilder dailyDegree = new StringBuilder();
            dailyDegree.append(dailyTempList.get(i).min).append("~").append(dailyTempList.get(i).max)
                    .append("℃");
            tempText.setText(dailyDegree.toString());
            forecastLayout.addView(view);
        }

        StringBuilder aqiSB = new StringBuilder();
        aqiSB.append(weather.aqi.city.aqi).append("μg/m³");
        StringBuilder pm25SB = new StringBuilder();
        pm25SB.append(weather.aqi.city.aqi).append("μg/m³");
        aqi.setText(aqiSB.toString());
        pm25.setText(pm25SB.toString());
        qlty.setText(weather.aqi.city.qlty);
        airInfo.setText(weather.suggestion.air.info);

        comfortTitle.setText(weather.suggestion.comfort.title);
        comfortInfo.setText(weather.suggestion.comfort.info);
        clothTitle.setText(weather.suggestion.cloth.title);
        clothInfo.setText(weather.suggestion.cloth.info);
        travelTitle.setText(weather.suggestion.travel.title);
        travelInfo.setText(weather.suggestion.travel.info);
        sportTitle.setText(weather.suggestion.sport.title);
        sportInfo.setText(weather.suggestion.sport.info);
    }

    private ProgressDialog progressDialog;

    /**
     * 显示进度对话框
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载....");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


}
