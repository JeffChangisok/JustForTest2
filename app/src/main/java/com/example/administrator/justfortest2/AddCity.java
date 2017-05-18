package com.example.administrator.justfortest2;


import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.administrator.justfortest2.db.FavouriteCity;
import com.example.administrator.justfortest2.util.DiyCityAdapter;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;


public class AddCity extends AppCompatActivity implements DiyCityAdapter.RecyItemOnClick, View.OnClickListener {

    public List<DiyCity> diyCityList = new ArrayList<>();

    public DiyCityAdapter adapter;

    private FloatingActionButton addBtn;

    private RecyclerView recyclerView;

    public Toolbar toolbar;

    private Button edit;

    private Button yes;

    private Button backBtn;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_edit:
                adapter = new DiyCityAdapter(diyCityList, AddCity.this, 1, this);
                recyclerView.setAdapter(adapter);
                adapter.setRecyItemOnClick(this);
                edit.setVisibility(View.GONE);
                yes.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_yes:
                adapter = new DiyCityAdapter(diyCityList, AddCity.this, 0, this);
                recyclerView.setAdapter(adapter);
                adapter.setRecyItemOnClick(this);
                edit.setVisibility(View.VISIBLE);
                yes.setVisibility(View.GONE);
                break;
            case R.id.backBtn_addCity:
                finish();
                break;
            case R.id.btn_add:
                Intent intent = new Intent(AddCity.this, SearchCity.class);
                startActivityForResult(intent, 1);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<FavouriteCity> list = DataSupport.findAll(FavouriteCity.class);
        if (list.size() - 1 > diyCityList.size()) {
            for (int i = 1; i < list.size(); i++) {
                diyCityList.add(new DiyCity(list.get(i).getName()));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcity);
        backBtn = (Button) findViewById(R.id.backBtn_addCity);
        edit = (Button) findViewById(R.id.btn_edit);
        yes = (Button) findViewById(R.id.btn_yes);
        addBtn  = (FloatingActionButton) findViewById(R.id.btn_add);
        edit.setOnClickListener(this);
        yes.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DiyCityAdapter(diyCityList, AddCity.this, 0, this);
        recyclerView.setAdapter(adapter);
        adapter.setRecyItemOnClick(this);
    }

    @Override
    public void onItemOnClick(View view, int index) {
        Intent intent = new Intent(AddCity.this, Tabs.class);
        intent.putExtra("position", index + 1);
        startActivity(intent);
        finish();
    }

    /**
     * 这个方法可以用在tabs和这个活动之间
     * <p>
     * 回到TABS再进来数据就丢失了
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    String cityName = data.getStringExtra("cityName");
                    if (cityName != null) {
                        diyCityList.add(new DiyCity(cityName));
                        //recyclerView.scrollToPosition(adapter.getItemCount()-1);
                        adapter.notifyDataSetChanged();
                    }
                }
                break;
            default:
        }
    }
}
