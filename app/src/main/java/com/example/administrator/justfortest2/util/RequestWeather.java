package com.example.administrator.justfortest2.util;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.administrator.justfortest2.gson.Weather;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

/**
 * Created by Administrator on 2017/5/4.
 *通过weatherId将获取的天气JSON数据存入缓存
 */

public class RequestWeather {

    public  int status ;

    private  ProgressDialog progressDialog;

    public  Weather weather;

    public  String mWeatherId;

    public  int request(String weatherId) {
        //应该增加一个根据天气ID从数据库查询经纬度
        String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" +
                weatherId + "&key=8c5ef408aec747eb956be39c65689b5f";
        Log.d("MyFault", "上面");
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String responseText = response.body().string();
                weather = Utility.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit();
                    //核心
                    editor.putString("weather", responseText);
                    editor.apply();
                    status = 1;
                    mWeatherId = weather.basic.weatherId;
                    Log.d("MyFault", "onResponse: true"+status);

                } else {
                    status = 0;
                    Log.d("MyFault", "onResponse: false");
                }

            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                status = 0;
                Log.d("MyFault", "onFailure: ");
            }

        });
        Log.d("MyFault", "request: "+status);
        closeProgressDialog();
        return status;
    }

    private void showProgressDialog(){
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(MyApplication.getContext());
            progressDialog.setMessage("正在加载....");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    /**
     * 关闭进度对话框
     */
    private void closeProgressDialog(){
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


}
