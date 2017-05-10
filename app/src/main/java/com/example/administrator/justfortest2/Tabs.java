package com.example.administrator.justfortest2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administrator.justfortest2.db.FavouriteCity;
import com.example.administrator.justfortest2.db.Province;
import com.example.administrator.justfortest2.gson.Weather;
import com.example.administrator.justfortest2.util.HttpUtil;
import com.example.administrator.justfortest2.util.MyApplication;
import com.example.administrator.justfortest2.util.RequestWeather;
import com.example.administrator.justfortest2.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Tabs extends AppCompatActivity {

    //public ImageView bingPicImg;

    public DrawerLayout drawerLayout;

    public SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public ViewPager mViewPager;

    public List<WeatherFragment> mFragments = new ArrayList<>();

    private LinearLayout ll;

    private Button mPreSelectedBt;

    public Weather weather;

    public String mWeatherId;

    private Toolbar toolbar;


    public void refresh(final String weatherId) {

        final int currentItem = mViewPager.getCurrentItem();
        final String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" +
                weatherId + "&key=8c5ef408aec747eb956be39c65689b5f";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                weather = Utility.handleWeatherResponse(responseText);
                Log.d("MyFault", weather.status);
                if (weather != null && "ok".equals(weather.status)) {
                    FavouriteCity favouriteCity = new FavouriteCity();
                    favouriteCity.setWeather(responseText);
                    favouriteCity.updateAll("weatherId = ?", weatherId);
                    WeatherFragment fragment = WeatherFragment.newInstance(responseText);
                    mFragments.set(currentItem, fragment);



                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initView();
                            mViewPager.setCurrentItem(currentItem);
                        }
                    });
                } else {
                    Log.d("MyFault", "onResponse: false");
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("MyFault", "onFailure: ");
            }
        });


    }

    public void setWeatherOnPosition0(final String weatherId) {

        final String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" +
                weatherId + "&key=8c5ef408aec747eb956be39c65689b5f";


        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                weather = Utility.handleWeatherResponse(responseText);
                if (weather != null && "ok".equals(weather.status)) {

                    FavouriteCity favouriteCity = new FavouriteCity();
                    favouriteCity.setWeather(responseText);
                    favouriteCity.setWeatherId(weatherId);
                    favouriteCity.updateAll("id = ?", "1");
                    mWeatherId = weather.basic.weatherId;
                    WeatherFragment fragment = WeatherFragment.newInstance(responseText);
                    mFragments.set(0, fragment);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initView();
                            if (mFragments.size() > 0) {
                                Button firstBtn = (Button) ll.getChildAt(0);
                                mPreSelectedBt.setBackgroundResource(R.drawable.dot);
                                firstBtn.setBackgroundResource(R.drawable.selected_dot);
                                mPreSelectedBt = firstBtn;
                            }
                        }
                    });
                } else {
                    Log.d("MyFault", "onResponse: false");
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("MyFault", "onFailure: ");
            }
        });

    }

    public void initView() {

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), mFragments);
        // Set up the ViewPager with the sections adapter

        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    /**
     * @param btn 一个新的按钮
     */

    public void initBtn(Button btn) {
        btn.setLayoutParams(new ViewGroup.LayoutParams(15, 15));
        btn.setBackgroundResource(R.drawable.dot);
        ll.addView(btn);
    }

    @Override
    protected void onResume() {
        super.onResume();

        List<FavouriteCity> savedList = DataSupport.findAll(FavouriteCity.class);

        if ((mFragments.size()) < savedList.size()) {
            mFragments.clear();
            for (int i = 0; i < savedList.size(); i++) {
                String weatherInfo = savedList.get(i).getWeather();
                WeatherFragment fragment = WeatherFragment.newInstance(weatherInfo);
                mFragments.add(fragment);
                initBtn(new Button(this));
            }
            Button firstBtn = (Button) ll.getChildAt(0);
            firstBtn.setVisibility(View.VISIBLE);
            firstBtn.setBackgroundResource(R.drawable.selected_dot);
            mPreSelectedBt = firstBtn;
            initView();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //bingPicImg = (ImageView) findViewById(R.id.bing_pic_img) ;
        mPreSelectedBt = new Button(this);
        ll = (LinearLayout) findViewById(R.id.ll_pager_num);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mViewPager = (ViewPager) findViewById(R.id.container);
        // Log.d("MyFault", "onCreate: 实例化一个fragment的上面");
        WeatherFragment fragment = WeatherFragment.newInstance("");
        mFragments.add(fragment);


        //打开侧滑菜单按钮
        Button button = (Button) findViewById(R.id.test_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        initView();

        mPreSelectedBt = new Button(this);
        initBtn(mPreSelectedBt);
        mPreSelectedBt.setVisibility(View.GONE);

        //注册悬浮按钮点击事件
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Tabs.this, AddCity.class);
                startActivity(intent);
            }
        });

        //注册页面切换监听事件
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                if (mPreSelectedBt != null) {
                    mPreSelectedBt.setBackgroundResource(R.drawable.dot);
                }

                /*if (mFragments.size() > 0) {
                    Button btn = (Button) ll.getChildAt(0);
                    btn.setVisibility(View.VISIBLE);
                }*/

                Button currentBtn = (Button) ll.getChildAt(position);
                currentBtn.setBackgroundResource(R.drawable.selected_dot);
                mPreSelectedBt = currentBtn;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }


    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        List<WeatherFragment> fragmentList;

        public SectionsPagerAdapter(FragmentManager fm, List<WeatherFragment> fragmentList) {
            super(fm);
            this.fragmentList = fragmentList;
        }

        /**
         * @param position 当前所在页
         * @return 当前页要显示的内容
         */
        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return fragmentList.get(position);
        }

        //设置显示的总页数
        @Override
        public int getCount() {
            // Show 3 total pages.
            return fragmentList.size();
        }

    }

    /**
     * 加载必应每日一图
     */
    //利用广播


}
