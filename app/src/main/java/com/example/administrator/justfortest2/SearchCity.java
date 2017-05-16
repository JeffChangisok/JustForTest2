package com.example.administrator.justfortest2;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
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
import com.example.administrator.justfortest2.gson.HourlyAndDaily;
import com.example.administrator.justfortest2.gson.Weather;
import com.example.administrator.justfortest2.util.HttpUtil;
import com.example.administrator.justfortest2.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SearchCity extends AppCompatActivity {

    EditText eSearch;
    ImageView ivDeleteText;
    ListView mListView;

    ArrayList<Map<String, Object>> mData = new ArrayList();
    List<String> mListTitle = new ArrayList();
    List<String> mListText = new ArrayList();
    List<String> mListId = new ArrayList();
    Weather weather;
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
    private void showProgressDialog() {
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
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city);
        mListView = (ListView) findViewById(R.id.mListView);
        eSearch = (EditText) findViewById(R.id.etSearch);
        set_eSearch_TextChanged();
        set_ivDeleteText_OnClick();
        set_mListView_adapter();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                final String weatherId = mData.get(position).get("id").toString();
                final String cityName = mData.get(position).get("title").toString();

                List<FavouriteCity> list = DataSupport.where("name = ?", cityName)
                        .find(FavouriteCity.class);

                if (list.isEmpty()) {
                    final String weatherUrl = "https://free-api.heweather.com/v5/weather?city=" +
                            weatherId + "&key=8c5ef408aec747eb956be39c65689b5f";
                    showProgressDialog();

                    HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
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
                                    HourlyAndDaily hourlyAndDaily = Utility.handleCaiWeatherResponse(responseText2);
                                    if (weather != null && "ok".equals(weather.status) &&
                                            hourlyAndDaily != null && "ok".equals(hourlyAndDaily.status)) {
                                        FavouriteCity favouriteCity = new FavouriteCity();
                                        favouriteCity.setName(cityName);
                                        favouriteCity.setWeather(responseText);
                                        favouriteCity.setWeatherId(weatherId);
                                        favouriteCity.setCaiweather(responseText2);
                                        favouriteCity.save();
                                        closeProgressDialog();
                                        finish();
                                    } else {
                                        Log.d("MyFault", "onResponse: false");
                                        Log.d("MyFault", weather.status);
                                        closeProgressDialog();
                                    }

                                }

                                @Override
                                public void onFailure(Call call, IOException e) {

                                }

                            });


                        }

                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                            Log.d("MyFault", "onFailure: ");
                            closeProgressDialog();
                        }
                    });

                    Intent intent = new Intent(SearchCity.this, AddCity.class);
                    intent.putExtra("cityName", mData.get(position).get("title").toString());
                    setResult(RESULT_OK, intent);

                } else {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(SearchCity.this);
                    dialog.setTitle("这是一个意外(°ー°〃)");
                    dialog.setMessage("请检查你是否已经添加过该城市了");
                    dialog.setCancelable(false);
                    dialog.setPositiveButton("好吧", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dialog.show();
                }

            }
        });
    }

    private void set_mListView_adapter() {
        mListView.setVisibility(View.GONE);
        getmData(mData);
        adapter = new SimpleAdapter(this, mData, android.R.layout.simple_list_item_2,
                new String[]{"title", "text", "id"},
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
                if (s.length() == 0) {
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

        for (int i = 0; i < length; ++i) {
            if (((String) this.mListTitle.get(i)).contains(data) || ((String) this.mListText.get(i)).contains(data)) {
                HashMap item = new HashMap();
                item.put("title", mListTitle.get(i));
                item.put("text", mListText.get(i));
                item.put("id", mListId.get(i));
                mDataSubs.add(item);
            }
        }

    }

    private void set_ivDeleteText_OnClick() {
        this.ivDeleteText = (ImageView) this.findViewById(R.id.ivDeleteText);
        this.ivDeleteText.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                SearchCity.this.eSearch.setText("");
            }
        });
    }



    /*private List<String> putData(String files[]) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            for (int i = 0; i < 8; i++) {
                InputStream in = getAssets().open(files[i]);
                int size = in.available();
                byte[] buffer = new byte[size];
                in.read(buffer);
                in.close();
                String str = new String(buffer,"utf8");
                stringBuilder.append(str);
            }
            return Arrays.asList(stringBuilder.toString().split("\n"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }*/

    private void putData(String files[], List<String> list) {
        boolean flag = false;
        if ("cityid_1.txt".equals(files[0])) {
            flag = true;
        }
        try {
            for (int i = 0; i < 8; i++) {
                InputStreamReader inputReader = new InputStreamReader(getAssets().open(files[i]));
                BufferedReader bufReader = new BufferedReader(inputReader);
                String line = "";
                if (flag) {
                    while ((line = bufReader.readLine()) != null) {
                        list.add("CN" + line);
                    }
                } else {
                    while ((line = bufReader.readLine()) != null) {
                        list.add(line);
                    }
                }

                inputReader.close();
                bufReader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void getmData(ArrayList<Map<String, Object>> mDatas) {
        HashMap item = new HashMap();
        String cityIdFiles[] = {"cityid_1.txt", "cityid_2.txt", "cityid_3.txt", "cityid_4.txt",
                "cityid_5.txt", "cityid_6.txt", "cityid_7.txt", "cityid_8.txt"};

        String titleFiles[] = {"title_1.txt", "title_2.txt", "title_3.txt", "title_4.txt",
                "title_5.txt", "title_6.txt", "title_7.txt", "title_8.txt"};

        String textFiles[] = {"text_1.txt", "text_2.txt", "text_3.txt", "text_4.txt",
                "text_5.txt", "text_6.txt", "text_7.txt", "text_8.txt"};

        /*putData(cityIdFiles);
        putData(titleFiles);
        putData(textFiles);*/


        putData(cityIdFiles, mListId);
        putData(titleFiles, mListTitle);
        putData(textFiles, mListText);

        for (int i = 0; i < mListId.size(); i++) {
            item.put("id", mListId.get(i));
            item.put("title", mListTitle.get(i));
            item.put("text", mListText.get(i));
            //Log.d("MyFault", mListId.get(i));
            mDatas.add(item);
        }

        /*try {
                InputStream in = getAssets().open(cityIdFiles[0]);
                int size = in.available();
                byte[] buffer = new byte[size];
                in.read(buffer);
                in.close();
                String str = new String(buffer,"utf8");
                Log.d("MyFault",str);
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        /*mListTitle.add("武汉");
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
        mDatas.add(item);*/

    }

}
