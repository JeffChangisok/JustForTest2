package com.example.administrator.justfortest2;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administrator.justfortest2.db.FavouriteCity;
import com.example.administrator.justfortest2.gson.Forecast;
import com.example.administrator.justfortest2.gson.Weather;
import com.example.administrator.justfortest2.service.AutoUpdateService;
import com.example.administrator.justfortest2.util.HttpUtil;
import com.example.administrator.justfortest2.util.MyApplication;
import com.example.administrator.justfortest2.util.RequestWeather;
import com.example.administrator.justfortest2.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by Administrator on 2017/5/2.
 */

public class WeatherFragment extends Fragment {

    private ImageView bingPicImg;

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

    public WeatherFragment() {
    }


    public static WeatherFragment newInstance(String arg) {
        WeatherFragment fragment = new WeatherFragment();
        Bundle bundle = new Bundle();
        bundle.putString("key", arg);
        fragment.setArguments(bundle);
        return fragment;
    }


    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());


    String selecctedWeather;

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
        bingPicImg = (ImageView) view.findViewById(R.id.bing_pic_img);
        swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        return view;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        /*if (weatherString != null) {
            //有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            mWeatherId = getActivity().getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }*/

        selecctedWeather = getArguments().getString("key");
        if (selecctedWeather == "") {
            //数据库里面是否有默认城市
            List<FavouriteCity> cities = DataSupport.where("name = ?", "默认")
                    .find(FavouriteCity.class);

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
            Log.d("MyFault", "测试点");
            Weather weather = Utility.handleWeatherResponse(selecctedWeather);
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


        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }

    }

    /**
     * 根据天气id请求城市天气信息
     */
    public void requestWeather(final String weatherId) {
        //应该增加一个根据天气ID从数据库查询经纬度
        String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" +
                weatherId + "&key=8c5ef408aec747eb956be39c65689b5f";
        //在utility中的解析方法中会根据返回数据的键进行处理


        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                //Log.d("inRequest", "onResponse: "+responseText);
                final Weather weather = Utility.handleWeatherResponse(responseText);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            //使用数据库保存天气信息
                            FavouriteCity favouriteCity = new FavouriteCity();
                            favouriteCity.setWeather(responseText);
                            favouriteCity.setWeatherId(weatherId);
                            favouriteCity.setName("默认");
                            favouriteCity.save();
                            /*SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(MyApplication.getContext())
                                    .edit();
                            editor.putString("weather", responseText);
                            editor.apply();*/
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(getActivity(), "处理获取的天气信息失败",
                                    Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "获取天气信息失败",
                                Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

        });
        loadBingPic();

    }

    /**
     * 处理并展示Weather实体类中的数据
     */
    public void showWeatherInfo(Weather weather) {
        if (weather != null && "ok".equals(weather.status)) {
            String cityName = weather.basic.cityName;
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


    /**
     * 加载必应每日一图
     */

    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(MyApplication.getContext())
                        .edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(getActivity()).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }


}
