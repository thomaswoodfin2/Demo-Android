package com.pinpoint.appointment.adapter;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.activities.HomeActivity;
import com.pinpoint.appointment.databinding.ItemRowOpenhouseBinding;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.models.PropertyData;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by admin on 28-Jul-17.
 */

public class AdpOpenhouseRecyclerView extends RecyclerView.Adapter<AdpOpenhouseRecyclerView.MyViewHolder> {

    private final Context context;
    private List<PropertyData> lists;
    private HomeActivity parent;
    private LayoutInflater layoutInflater;
    public AdpOpenhouseRecyclerView(Context context, HomeActivity _parent, List<PropertyData> lists) {
        this.context = context;
        this.lists = lists;
        parent = _parent;
    }

    @Override
    public AdpOpenhouseRecyclerView.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        ItemRowOpenhouseBinding binding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_row_openhouse, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(AdpOpenhouseRecyclerView.MyViewHolder holder, int position) {


        holder.mItemBinding.tvAddress.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        holder.mItemBinding.btLeads.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        holder.mItemBinding.btDelete.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        holder.mItemBinding.btStart.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        holder.mItemBinding.btEdit.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        holder.mItemBinding.tvAddress.setText(lists.get(position).getAddress());
        Picasso.get().load(lists.get(position).getPropertyImage()).noFade().placeholder(R.mipmap.app_icon)
                .into(holder.mItemBinding.ivImgProfile);

        if(lists.get(position).getLeadCount()>0) {
            holder.mItemBinding.btLeads.setVisibility(View.VISIBLE);
            holder.mItemBinding.btLeads.setText(MessageHelper.getInstance().getAppMessage(context.getString(R.string.str_leads)) + " (" + lists.get(position).getLeadCount() + ")");
        }
        else
            {
                holder.mItemBinding.btLeads.setVisibility(View.GONE);
            }
        holder.mItemBinding.btEdit.setTag(position);
        holder.mItemBinding.btStart.setTag(position);
        holder.mItemBinding.btDelete.setTag(position);
        holder.mItemBinding.btLeads.setTag(position);
    }

    public void setData(List<PropertyData> lists) {
        this.lists = lists;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ItemRowOpenhouseBinding mItemBinding;

        MyViewHolder(ItemRowOpenhouseBinding itemBinding)
        {
            super(itemBinding.getRoot());
            mItemBinding=itemBinding;

        }
    }
}
