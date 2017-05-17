package com.example.administrator.justfortest2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administrator.justfortest2.db.FavouriteCity;
import com.example.administrator.justfortest2.gson.HourlyAndDaily;
import com.example.administrator.justfortest2.gson.Weather;
import com.example.administrator.justfortest2.util.HttpUtil;
import com.example.administrator.justfortest2.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static org.litepal.LitePalApplication.getContext;

public class Tabs extends AppCompatActivity {

    public TextView textView;

    public ImageView bingPicImg;

    public DrawerLayout drawerLayout;

    public LinearLayout ll;

    private LocalBroadcastManager localBroadcastManager;

    public SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public ViewPager mViewPager;

    public List<WeatherFragment> mFragments = new ArrayList<>();

    public Weather weather;

    public HourlyAndDaily hourlyAndDaily;

    public String mWeatherId;

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

    List<FavouriteCity> savedList;

    Button preSelectedBtn;

    ProgressDialog progressDialog ;


    /**
     * 加载必应每日一图
     */

    public void initBtn(Button btn) {

        btn.setLayoutParams(new ViewGroup.LayoutParams(15, 15));

        btn.setBackgroundResource(R.drawable.dot);

        ll.addView(btn);

    }

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
                        .getDefaultSharedPreferences(getContext())
                        .edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(Tabs.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

    public void refresh(final String weatherId) {

        final int currentItem = mViewPager.getCurrentItem();
        final String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" +
                weatherId + "&key=8c5ef408aec747eb956be39c65689b5f";

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            Intent intent = new Intent("com.example.administrator.justfortest2.STOP_REFRESH");

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                weather = Utility.handleWeatherResponse(responseText);
                String jing = weather.basic.jing;
                String wei = weather.basic.wei;
                String caiWeatherUrl = "https://api.caiyunapp.com/v2/D99AfEnT96xj1fsy/" + jing +
                        "," + wei + "/forecast.json";

                HttpUtil.sendOkHttpRequest(caiWeatherUrl, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String responseText2 = response.body().string();
                        hourlyAndDaily = Utility.handleCaiWeatherResponse(responseText2);
                        if (weather != null && "ok".equals(weather.status) &&
                                hourlyAndDaily != null && "ok".equals(hourlyAndDaily.status)) {
                            FavouriteCity favouriteCity = new FavouriteCity();
                            favouriteCity.setWeather(responseText);
                            favouriteCity.setCaiweather(responseText2);
                            favouriteCity.updateAll("weatherId = ?", weatherId);
                            WeatherFragment fragment = WeatherFragment.newInstance(responseText,responseText2);
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
                            Toast.makeText(getContext(), "更新失败", Toast.LENGTH_SHORT).show();

                        }
                        localBroadcastManager.sendBroadcast(intent);

                    }
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Toast.makeText(getContext(), "更新失败", Toast.LENGTH_SHORT).show();
                        localBroadcastManager.sendBroadcast(intent);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("MyFault", "onFailure: ");
                Toast.makeText(getContext(), "更新失败", Toast.LENGTH_SHORT).show();
                localBroadcastManager.sendBroadcast(intent);
            }
        });

        loadBingPic();

    }

    public void setWeatherOnPosition0(final String weatherId) {

        final String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" +
                weatherId + "&key=8c5ef408aec747eb956be39c65689b5f";
        savedList = DataSupport.findAll(FavouriteCity.class);

        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                weather = Utility.handleWeatherResponse(responseText);
                String jing =weather.basic.jing;
                String wei = weather.basic.wei;
                String caiWeatherUrl = "https://api.caiyunapp.com/v2/D99AfEnT96xj1fsy/" + jing +
                        "," + wei + "/forecast.json";

                HttpUtil.sendOkHttpRequest(caiWeatherUrl, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String responseText2 = response.body().string();
                        hourlyAndDaily = Utility.handleCaiWeatherResponse(responseText2);

                        if (weather != null && "ok".equals(weather.status) &&
                                hourlyAndDaily != null && "ok".equals(hourlyAndDaily.status)) {
                            FavouriteCity favouriteCity = new FavouriteCity();
                            favouriteCity.setWeather(responseText);
                            favouriteCity.setWeatherId(weatherId);
                            favouriteCity.setCaiweather(responseText2);
                            favouriteCity.setName(weather.basic.cityName);
                            favouriteCity.updateAll("id = ?", "1");
                            mWeatherId = weather.basic.weatherId;
                            final WeatherFragment fragment = WeatherFragment.newInstance(responseText,responseText2);
                            mFragments.set(0, fragment);
                            savedList.get(0).setName(weather.basic.cityName);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    initView();
                                    if (preSelectedBtn != null) {
                                        preSelectedBtn.setBackgroundResource(R.drawable.dot);
                                        Button currentBtn = (Button) ll.getChildAt(0);
                                        currentBtn.setBackgroundResource(R.drawable.selected_dot);
                                        preSelectedBtn = currentBtn;
                                    }
                                    textView.setText(weather.basic.cityName);
                                    closeProgressDialog();
                                }
                            });
                        } else {
                            Log.d("MyFault", "setWeatherOnPosition0解析彩云失败");
                            Log.d("MyFault",hourlyAndDaily.status);
                            Log.d("MyFault",weather.status);
                        }

                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("MyFault", "setWeatherOnPosition0请求彩云失败");
                    }

                });


            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("MyFault", "setWeatherOnPosition0.onFailure: 请求失败");
            }
        });

    }

    public void initView() {

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), mFragments);
        // Set up the ViewPager with the sections adapter

        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        savedList = DataSupport.findAll(FavouriteCity.class);
        /*Log.d("MyFault", "前savedList.size()="+String.valueOf(savedList.size()));
        Log.d("MyFault", "前mFragments.size()="+String.valueOf(mFragments.size()));*/

        if (savedList.size() != 0) {
            preSelectedBtn = new Button(this);
            mFragments.clear();
            ll.removeAllViews();
            for (int i = 0; i < savedList.size(); i++) {
                String weatherInfo = savedList.get(i).getWeather();
                String caiWeatherInfo = savedList.get(i).getCaiweather();
                WeatherFragment fragment = WeatherFragment.newInstance(weatherInfo,caiWeatherInfo);
                mFragments.add(fragment);
                initBtn(new Button(this));
            }
            initView();
            Button firstBtn = (Button) ll.getChildAt(0);
            firstBtn.setBackgroundResource(R.drawable.selected_dot);
            if(mFragments.size()==1){
                firstBtn.setVisibility(View.GONE);
            }else{
                firstBtn.setVisibility(View.VISIBLE);
            }
            preSelectedBtn = firstBtn;
            textView.setText(savedList.get(0).getName());
        }
        if (savedList.size() == 1) {
            textView.setText(savedList.get(0).getName());
        }
        //从缓存加载图片
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImg);
        } else {
            loadBingPic();
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mViewPager.setCurrentItem(intent.getIntExtra("position", 0));
        intent.removeExtra("position");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);
        ll = (LinearLayout) findViewById(R.id.ll_dot);
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        textView = (TextView) findViewById(R.id.title_name);
        //bingPicImg = (ImageView) findViewById(R.id.bing_pic_img) ;
        bingPicImg = (ImageView) findViewById(R.id.bing_pic_img);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mViewPager = (ViewPager) findViewById(R.id.container);
        // Log.d("MyFault", "onCreate: 实例化一个fragment的上面");
        WeatherFragment fragment = WeatherFragment.newInstance("","");
        mFragments.add(fragment);

        String firstName = getIntent().getStringExtra("firstName");
        if (firstName != null) {
            textView.setText(firstName);
            getIntent().removeExtra("firstName");
        }

        //打开侧滑菜单按钮
        Button button1 = (Button) findViewById(R.id.open);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);

            }
        });

        Button button2 = (Button) findViewById(R.id.btn_manager);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Tabs.this, AddCity.class);
                startActivity(intent);

            }
        });


        initView();

        //页面切换监听
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

                if (preSelectedBtn != null) {
                    preSelectedBtn.setBackgroundResource(R.drawable.dot);
                    Button currentBtn = (Button) ll.getChildAt(position);
                    currentBtn.setBackgroundResource(R.drawable.selected_dot);
                    preSelectedBtn = currentBtn;
                }
                textView.setText(savedList.get(position).getName());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    //适配器
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
     * 显示进度对话框
     */
    public void showProgressDialog(){
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("正在加载....");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
    /**
     * 关闭进度对话框
     */
    public void closeProgressDialog(){
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


}
