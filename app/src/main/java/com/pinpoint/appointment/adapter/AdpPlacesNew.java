package com.pinpoint.appointment.adapter;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.pinpoint.appointment.R;
import com.pinpoint.appointment.api.PlaceAPI;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.models.Places;
import com.pinpoint.appointment.utils.Debug;

import java.util.ArrayList;

public class AdpPlacesNew extends BaseAdapter implements Filterable {
    ArrayList<Places> resultList;
    private Context mContext;
    private PlaceAPI mPlaceAPI = new PlaceAPI();

    public AdpPlacesNew(Context context,ArrayList<Places> resultList) {
        mContext = context;
        this.resultList=resultList;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public Places getItem(int position) {
        return resultList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        if (row == null) {
            row = LayoutInflater.from(mContext).inflate(R.layout.row_textview, null);
            ViewHolder holder = new ViewHolder(row);
            row.setTag(holder);
        }

        final ViewHolder holder = (ViewHolder) row.getTag();
        try {
            holder.place.setText(resultList.get(position).getDescription());
        }catch (Exception ex)
        {}
        return row;
    }




    public class ViewHolder {
        TextView place;
        TextView address;

        public ViewHolder(View root) {
            place = GenericView.findViewById(root, R.id.tv_txtTitle);
        }
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    Debug.trace("Constraint:" + constraint);
                    if (constraint.length() >= 1) {
                        ArrayList<Places> resultList1 = mPlaceAPI.autocomplete(constraint.toString());

                        if (resultList1 != null) {
                            filterResults.values = resultList1;
                            filterResults.count = resultList1.size();
                        }
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                try{
                    if (results != null && results.count > 0)
                    {
                        Debug.trace("Constraint:" + constraint + "FilterResult:" + results.toString());
                        resultList= (ArrayList<Places>) results.values;
                        notifyDataSetChanged();
                    }
                    else
                    {
                        notifyDataSetInvalidated();
                    }
                }catch (Exception ex)
                {
                    Log.e("Error",ex.toString());
                }
            }
        };
        return filter;
    }

}