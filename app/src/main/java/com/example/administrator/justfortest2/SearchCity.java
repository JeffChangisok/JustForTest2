package com.example.administrator.justfortest2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.administrator.justfortest2.db.FavouriteCity;
import com.example.administrator.justfortest2.gson.Weather;
import com.example.administrator.justfortest2.util.HttpUtil;
import com.example.administrator.justfortest2.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SearchCity extends AppCompatActivity {

    EditText eSearch;
    ImageView ivDeleteText;
    ListView mListView;

    ArrayList<Map<String, Object>> mData = new ArrayList();
    ArrayList<String> mListTitle = new ArrayList();
    ArrayList<String> mListText = new ArrayList();
    ArrayList<String> mListId = new ArrayList();
    Weather weather ;
    SimpleAdapter adapter;
    Handler myhandler = new Handler();

    Runnable eChanged = new Runnable() {
        public void run() {
            String data = SearchCity.this.eSearch.getText().toString();
            SearchCity.this.mData.clear();
            SearchCity.this.getmDataSub(SearchCity.this.mData, data);
            SearchCity.this.adapter.notifyDataSetChanged();
        }
    };

    public SearchCity() {
    }

    private ProgressDialog progressDialog;
    /**
     * 显示进度对话框
     */
    private void showProgressDialog(){
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
    private void closeProgressDialog(){
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city);
        mListView = (ListView)findViewById(R.id.mListView);
        eSearch = (EditText) findViewById(R.id.etSearch);
        set_eSearch_TextChanged();
        set_ivDeleteText_OnClick();
        set_mListView_adapter();


        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String weatherId = mData.get(position).get("id").toString();
                final String cityName = mData.get(position).get("title").toString();


                final String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" +
                        weatherId + "&key=8c5ef408aec747eb956be39c65689b5f";
                showProgressDialog();

                HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final String responseText = response.body().string();
                        weather = Utility.handleWeatherResponse(responseText);
                        if (weather != null && "ok".equals(weather.status)) {
                            FavouriteCity favouriteCity = new FavouriteCity();
                            favouriteCity.setName(cityName);
                            favouriteCity.setWeather(responseText);
                            favouriteCity.setWeatherId(weatherId);
                            favouriteCity.save();
                            closeProgressDialog();
                            finish();
                        } else {
                            Log.d("MyFault", "onResponse: false");
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Log.d("MyFault", "onFailure: ");
                        Toast.makeText(SearchCity.this,"添加失败小伙子",Toast.LENGTH_SHORT).show();
                    }
                });

                Intent intent = new Intent(SearchCity.this,AddCity.class);
                intent.putExtra("cityName",mData.get(position).get("title").toString());
                setResult(RESULT_OK,intent);

            }
        });
    }

    private void set_mListView_adapter() {
        mListView.setVisibility(View.GONE);
        getmData(mData);
        adapter = new SimpleAdapter(this, mData, android.R.layout.simple_list_item_2,
                new String[]{"title", "text","id"},
                new int[]{android.R.id.text1, android.R.id.text2});
        mListView.setAdapter(adapter);
    }

    private void set_eSearch_TextChanged() {

        eSearch.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            public void afterTextChanged(Editable s) {
                if(s.length() == 0) {
                    SearchCity.this.ivDeleteText.setVisibility(View.GONE);
                    mListView.setVisibility(View.GONE);
                } else {
                    mListView.setVisibility(View.VISIBLE);

                    SearchCity.this.ivDeleteText.setVisibility(View.VISIBLE);
                }

                SearchCity.this.myhandler.post(SearchCity.this.eChanged);

            }
        });
    }

    private void getmDataSub(ArrayList<Map<String, Object>> mDataSubs, String data) {
        int length = this.mListTitle.size();

        for(int i = 0; i < length; ++i) {
            if(((String)this.mListTitle.get(i)).contains(data) || ((String)this.mListText.get(i)).contains(data)) {
                HashMap item = new HashMap();
                item.put("title", mListTitle.get(i));
                item.put("text", mListText.get(i));
                item.put("id", mListId.get(i));
                mDataSubs.add(item);
            }
        }

    }

    private void set_ivDeleteText_OnClick() {
        this.ivDeleteText = (ImageView)this.findViewById(R.id.ivDeleteText);
        this.ivDeleteText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                SearchCity.this.eSearch.setText("");
            }
        });
    }

    private void getmData(ArrayList<Map<String, Object>> mDatas) {
        HashMap item = new HashMap();

        mListTitle.add("武汉");
        mListText.add("湖北-武汉");
        mListId.add("CN101200101");
        item.put("title", mListTitle.get(0));
        item.put("text", mListText.get(0));
        item.put("id", mListId.get(0));
        mDatas.add(item);

        mListTitle.add("江夏");
        mListText.add("湖北-武汉");
        mListId.add("CN101200105");
        item = new HashMap();
        item.put("title", mListTitle.get(1));
        item.put("text", mListText.get(1));
        item.put("id", mListId.get(1));
        mDatas.add(item);

        mListTitle.add("武昌");
        mListText.add("湖北-武汉");
        mListId.add("CN101200106");
        item = new HashMap();
        item.put("title", mListTitle.get(2));
        item.put("text", mListText.get(2));
        item.put("id", mListId.get(2));
        mDatas.add(item);

    }

}
