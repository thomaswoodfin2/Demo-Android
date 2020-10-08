package com.pinpoint.appointment.activities;

import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.pinpoint.appointment.R;
import com.pinpoint.appointment.adapter.LogsAdapter;
import com.pinpoint.appointment.customviews.ItemDecoration;
import com.pinpoint.appointment.helper.DBHelper;
import com.pinpoint.appointment.helper.LogItem;

import java.util.ArrayList;
import java.util.List;

public class LogsActivity extends GlobalActivity implements View.OnClickListener{

  RecyclerView rv_list;
    ImageView iv_del;

    private LogsAdapter mAdapter;
    private List<LogItem> itemList;
    private String hdTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        rv_list = findViewById(R.id.rv_list);
        iv_del = findViewById(R.id.iv_del);
        itemList = new ArrayList<>();
        buildUI();
        populateData();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    //populateData
    private void populateData()
    {
//        tv_header.setText(hdTitle);
        loadData();
    }

    //buildUI
    private void buildUI()
    {
        findViewById(R.id.iv_imgMenu_Back).setOnClickListener(this);
        iv_del.setOnClickListener(this);

        mAdapter = new LogsAdapter(itemList, 1);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getCurrContext());
        rv_list.setLayoutManager(mLayoutManager);
        rv_list.setItemAnimator(new DefaultItemAnimator());
        rv_list.addItemDecoration(new ItemDecoration(0, 0, 0, 1));
        rv_list.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.iv_imgMenu_Back:
                finish();
                break;
            case R.id.iv_del:
                delLogs();
                break;
        }
    }

    //updateListUI
    private void updateListUI(List<LogItem> arrayList)
    {
        itemList.clear();
        itemList.addAll(arrayList);
        mAdapter.notifyDataSetChanged();
    }


    //delLogs
    private void delLogs()
    {
        DBHelper dbHelper = new DBHelper(getCurrContext());
        dbHelper.deleteAll();

        loadData();
    }

    //loadData
    private void loadData()
    {
        DBHelper dbHelper = new DBHelper(getCurrContext());
        updateListUI(dbHelper.getAllLogs());
    }

}

