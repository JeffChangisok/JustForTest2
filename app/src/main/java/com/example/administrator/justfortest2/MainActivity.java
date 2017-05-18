package com.example.administrator.justfortest2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.administrator.justfortest2.db.FavouriteCity;

import org.litepal.crud.DataSupport;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        List<FavouriteCity> cities = DataSupport.findAll(FavouriteCity.class);
        if (!cities.isEmpty()) {
            Intent intent = new Intent(this, Tabs.class);
            startActivity(intent);
            finish();
        }

    }
}
