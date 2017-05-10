package com.example.administrator.justfortest2.util;


import android.content.Context;

import org.litepal.LitePalApplication;


/**
 * Created by Administrator on 2017/5/4.
 */

public class MyApplication extends LitePalApplication {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
    public static Context getContext(){
        return context;
    }
}
