package com.example.administrator.justfortest2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.administrator.justfortest2.db.FavouriteCity;
import com.example.administrator.justfortest2.util.MyApplication;

import org.litepal.crud.DataSupport;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("MyFault", "主活动onCreate: ");

        List<FavouriteCity> cities = DataSupport.where("name = ?", "默认")
                .find(FavouriteCity.class);

        if(!cities.isEmpty()){
            Intent intent = new Intent(this, Tabs.class);
            startActivity(intent);
            finish();
        }


        /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext());
        if (prefs.getString("weather", null) != null) {
            Intent intent = new Intent(this, Tabs.class);
            startActivity(intent);
            finish();
        }*/

    }
}
