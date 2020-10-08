package com.pinpoint.appointment.adapter;

import android.app.Activity;
import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;

import com.pinpoint.appointment.databinding.ItemRowPlanBinding;
import com.pinpoint.appointment.models.PlanData;

import java.util.List;

/**
 * Created by admin on 28-Jul-17.
 */

public class AdpSubscriptionRecyclerView extends RecyclerView.Adapter<AdpSubscriptionRecyclerView.MyViewHolder> {

    private final Context context;
    private List<PlanData> lists;
    private Activity parent;
    private LayoutInflater layoutInflater;
    public AdpSubscriptionRecyclerView(Context context, Activity _parent, List<PlanData> lists) {
        this.context = context;
        this.lists = lists;
        parent = _parent;
    }

    @Override
    public AdpSubscriptionRecyclerView.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_friends, parent, false);
//        return new MyViewHolder(itemView);

        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        ItemRowPlanBinding binding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_row_plan, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(AdpSubscriptionRecyclerView.MyViewHolder holder, int position)
    {
        holder.mItemBinding.tvPlann.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        holder.mItemBinding.tvPlann.setText(lists.get(position).getPlanTitle());
        holder.mItemBinding.tvPlann.setTag(position);

    }

    public void setData(List<PlanData> lists) {
        this.lists = lists;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ItemRowPlanBinding mItemBinding;

        MyViewHolder(ItemRowPlanBinding itemBinding)
        {
            super(itemBinding.getRoot());
            mItemBinding=itemBinding;

        }
    }
}
