package com.huihui.cache;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.GridView;

public class MainActivity extends AppCompatActivity {

    private GridView mGridView;
    private PhotoWallAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mGridView = ((GridView) findViewById(R.id.photo_wall));


         adapter=new PhotoWallAdapter(this,R.layout.photo_layout,Images.imageThumbUrls,mGridView);


        mGridView.setAdapter(adapter);

    }

    @Override
    protected void onPause() {
        super.onPause();


        adapter.fluchCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        adapter.cancelAllTasks();
    }
}
