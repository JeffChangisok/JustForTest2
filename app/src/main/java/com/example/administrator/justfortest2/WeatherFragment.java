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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.administrator.justfortest2.db.FavouriteCity;
import com.example.administrator.justfortest2.gson.Forecast;
import com.example.administrator.justfortest2.gson.Weather;
import com.example.administrator.justfortest2.service.AutoUpdateService;
import com.example.administrator.justfortest2.util.HttpUtil;
import com.example.administrator.justfortest2.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/5/2.
 */

public class WeatherFragment extends Fragment {

    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView aqiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    public SwipeRefreshLayout swipeRefresh;
    public String mWeatherId;
    private LocalBroadcastManager localBroadcastManager;
    private LocalReceiver localReceiver;
    private IntentFilter intentFilter;
    public String cityName;

    public WeatherFragment() {
    }


    public static WeatherFragment newInstance(String arg) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle bundle = new Bundle();
        bundle.putString("key", arg);
        fragment.setArguments(bundle);
        return fragment;
    }

    String selectedWeather;

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
        titleCity = (TextView) view.findViewById(R.id.title_city);
        titleUpdateTime = (TextView) view.findViewById(R.id.title_update_time);
        degreeText = (TextView) view.findViewById(R.id.degree_text);
        weatherInfoText = (TextView) view.findViewById(R.id.weather_info_text);
        forecastLayout = (LinearLayout) view.findViewById(R.id.forecast_layout);
        aqiText = (TextView) view.findViewById(R.id.aqi_text);
        pm25Text = (TextView) view.findViewById(R.id.pm25_text);
        comfortText = (TextView) view.findViewById(R.id.comfort_text);
        carWashText = (TextView) view.findViewById(R.id.car_wash_text);
        sportText = (TextView) view.findViewById(R.id.sport_text);
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
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
        if (selectedWeather == "") {
            //数据库里面是否有城市
            List<FavouriteCity> cities = DataSupport.findAll(FavouriteCity.class);
            if (!cities.isEmpty()) {
                //有缓存时直接解析天气数据
                Weather weather = Utility.handleWeatherResponse(cities.get(0).getWeather());
                mWeatherId = weather.basic.weatherId;
                showWeatherInfo(weather);
            } else {
                //无缓存时去服务器查询天气
                mWeatherId = getActivity().getIntent().getStringExtra("weather_id");
                weatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(mWeatherId);
            }

        } else {
            Weather weather = Utility.handleWeatherResponse(selectedWeather);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
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
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            //使用数据库保存天气信息
                            FavouriteCity favouriteCity = new FavouriteCity();
                            favouriteCity.setWeather(responseText);
                            favouriteCity.setWeatherId(weatherId);
                            favouriteCity.setName(weather.basic.cityName);
                            favouriteCity.save();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                            closeProgressDialog();
                        } else {
                            Log.d("MyFault", "weatherFragment中，处理失败 ");
                        }
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
    public void showWeatherInfo(Weather weather) {
        if (weather != null && "ok".equals(weather.status)) {
            cityName = weather.basic.cityName;
            String updateTime = weather.basic.update.updateTime.split(" ")[1];
            String weatherInfo = weather.now.more.info;
            String degree = weather.now.temperature + "℃";
            titleCity.setText(cityName);
            titleUpdateTime.setText(updateTime);
            degreeText.setText(degree);
            weatherInfoText.setText(weatherInfo);
            forecastLayout.removeAllViews();
            for (Forecast forecast : weather.forecastList) {
                View view = LayoutInflater.from(getActivity()).inflate(R.layout.forecast_item,
                        forecastLayout, false);
                TextView dateText = (TextView) view.findViewById(R.id.date_text);
                TextView infoText = (TextView) view.findViewById(R.id.info_text);
                TextView maxText = (TextView) view.findViewById(R.id.max_text);
                TextView minText = (TextView) view.findViewById(R.id.min_text);
                dateText.setText(forecast.date);
                infoText.setText(forecast.more.info);
                maxText.setText(forecast.temperature.max);
                minText.setText(forecast.temperature.min);
                forecastLayout.addView(view);
            }
            if (weather.aqi != null) {
                aqiText.setText(weather.aqi.city.aqi);
                pm25Text.setText(weather.aqi.city.pm25);
            }
            String comfort = "舒适度：" + weather.suggestion.comfort.info;
            String carWash = "洗车指数：" + weather.suggestion.carWash.info;
            String sport = "运动建议" + weather.suggestion.sport.info;
            comfortText.setText(comfort);
            carWashText.setText(carWash);
            sportText.setText(sport);
            weatherLayout.setVisibility(View.VISIBLE);
            Intent intent = new Intent(getActivity(), AutoUpdateService.class);
            getActivity().startService(intent);
        } else {
            Toast.makeText(getActivity(), "获取天气信息失败", Toast.LENGTH_SHORT).show();
        }

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
