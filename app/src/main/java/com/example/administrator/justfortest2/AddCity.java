package com.example.administrator.justfortest2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.administrator.justfortest2.db.FavouriteCity;
import com.example.administrator.justfortest2.util.DiyCityAdapter;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;


public class AddCity extends AppCompatActivity {

    private List<DiyCity> diyCityList = new ArrayList<>();

    private DiyCityAdapter adapter;

    private RecyclerView recyclerView;


    @Override
    protected void onResume() {
        super.onResume();

        List<FavouriteCity> list = DataSupport.findAll(FavouriteCity.class);
        if(list.size()-1>diyCityList.size()){
            for(int i=1;i<list.size();i++){
                diyCityList.add(new DiyCity(list.get(i).getName()));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_addcity);
        Button backBtn = (Button) findViewById(R.id.back_button2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new DiyCityAdapter(diyCityList);
        recyclerView.setAdapter(adapter);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar,menu);
        return true;
    }


    /**
     * 这个方法可以用在tabs和这个活动之间
     *
     * 回到TABS再进来数据就丢失了
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode){
            case 1:
                if(resultCode==RESULT_OK){
                    String cityName = data.getStringExtra("cityName");
                    if(cityName!=null){
                        diyCityList.add(new DiyCity(cityName));
                        //recyclerView.scrollToPosition(adapter.getItemCount()-1);
                        adapter.notifyDataSetChanged();
                    }
                }
                break;
            default:
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.search:
                Intent intent = new Intent(AddCity.this,SearchCity.class);
               startActivityForResult(intent,1);
                break;
            case R.id.edit:
                if(adapter.getItemCount()!=0){
                    diyCityList.remove(adapter.getItemCount()-1);
                    adapter.notifyDataSetChanged();
                }
                break;
            default:
        }
        return true;
    }

}
