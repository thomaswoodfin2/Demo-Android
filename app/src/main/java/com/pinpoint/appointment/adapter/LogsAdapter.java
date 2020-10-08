package com.pinpoint.appointment.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pinpoint.appointment.R;
import com.pinpoint.appointment.helper.LogItem;

import java.util.List;


public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.MyViewHolder> {

    private List<LogItem> arrList;
    private int requestType;

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView tv_title;
        TextView tv_desc;

        LogItem item;
        int itemIndex;

        public MyViewHolder(View view, ViewGroup parent, int viewType) {
            super(view);
            tv_title = view.findViewById(R.id.tv_title);
            tv_desc = view.findViewById(R.id.tv_desc);
        }


        public void setData(LogItem item, int pos) {
            this.item = item;
            this.itemIndex = pos;
            tv_title.setText(item.getLog());
            tv_desc.setText(item.getTimestamp());
        }


        @Override
        public void onClick(View view) {
        }
    }

    public LogsAdapter(List<LogItem> arrList, int requestType) {
        this.arrList = arrList;
        this.requestType = requestType;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.log_list_cell, parent, false);

        return new MyViewHolder(itemView, parent, viewType);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        LogItem item = arrList.get(position);
        holder.setData(item, position);
    }

    @Override
    public int getItemCount() {
        return arrList.size();
    }


//    @Override
//    public int getItemViewType(int position) {
//        if(requestType == 2){
//            if(position == 0) return 2;
//        }
//        return 0;
//    }


    public void updateList(List<LogItem> list) {
        arrList = list;
        notifyDataSetChanged();
    }
}
