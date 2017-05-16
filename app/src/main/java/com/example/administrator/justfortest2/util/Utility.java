package com.example.administrator.justfortest2.util;

import android.text.TextUtils;
import android.util.Log;

import com.example.administrator.justfortest2.HourlyInfo;
import com.example.administrator.justfortest2.db.City;
import com.example.administrator.justfortest2.db.County;
import com.example.administrator.justfortest2.db.Province;
import com.example.administrator.justfortest2.gson.HourlyAndDaily;
import com.example.administrator.justfortest2.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 解析和处理服务器返回的JSON格式的省县市数据
 * Created by Administrator on 2017/4/23.
 */

public class Utility {
    /**
     * 解析和处理服务器返回的省级数据
     * */
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i<allProvinces.length(); i++){
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceICode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return  false;
    }
    /**
     * 解析和处理服务器返回的市级数据
     */
    public static boolean handleCityResponse(String response, int provinceId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i<allCities.length(); i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return  false;
    }
    /**
     * 解析和处理服务器返回的县级数据
     */
    public static boolean handleCountyResponse(String response, int cityId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i<allCounties.length(); i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return  false;
    }
    /**
     * 将返回的JSON数据解析成Weather实体类
     */
    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response); // 得到返回的完整的JSON数据信息
            //通过名称"HeWeather5"获取值
            //又因为其值是数组类型，所以获取方法是JSONArray()，返回类型也是JSONArray
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather5");
            //该数组里面只有一个"值"，所以我们取0号位
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 将返回的彩云JSON数据解析成Weather实体类
     */
    public static HourlyAndDaily handleCaiWeatherResponse(String response){
        try{
            return new Gson().fromJson(response,HourlyAndDaily.class);
        } catch (Exception e){
            e.printStackTrace();
            Log.d("MyFault", e.getMessage());
        }
        return null;
    }

}
