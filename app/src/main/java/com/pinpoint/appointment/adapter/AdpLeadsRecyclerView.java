package com.pinpoint.appointment.adapter;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.activities.ViewLeadsActivity;
import com.pinpoint.appointment.databinding.ItemRowLeadsBinding;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.models.LeadDetails;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by admin on 28-Jul-17.
 */

public class AdpLeadsRecyclerView extends RecyclerView.Adapter<AdpLeadsRecyclerView.MyViewHolder> {

    private final Context context;
    private List<LeadDetails> lists;
    private ViewLeadsActivity parent;
    private LayoutInflater layoutInflater;
    public AdpLeadsRecyclerView(Context context, ViewLeadsActivity _parent, List<LeadDetails> lists) {
        this.context = context;
        this.lists = lists;
        parent = _parent;
    }

    @Override
    public AdpLeadsRecyclerView.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        ItemRowLeadsBinding binding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_row_leads, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(AdpLeadsRecyclerView.MyViewHolder holder, int position) {
        holder.mItemBinding.tvEmail.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        holder.mItemBinding.tvName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        holder.mItemBinding.textView.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        holder.mItemBinding.btnView.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        holder.mItemBinding.tvTime.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        LeadDetails leadDetails=lists.get(position);

        holder.mItemBinding.tvEmail.setText(leadDetails.getEmail());
        holder.mItemBinding.tvName.setText(leadDetails.getName());
        holder.mItemBinding.textView.setText(leadDetails.getContact());
//        holder.mItemBinding.tvTime.setText(leadDetails.getDate());
        holder.mItemBinding.tvTime.setText(leadDetails.getDateFormat());

        holder.mItemBinding.btnView.setText(MessageHelper.getInstance().getAppMessage(context.getResources().getString(R.string.str_view)));

        Picasso.get().load(leadDetails.getProfileImage()).noFade().placeholder(R.drawable.user1)
                .into(holder.mItemBinding.ivImgProfile);
        holder.mItemBinding.ivImgDelete.setTag(position);
        holder.mItemBinding.btnView.setTag(position);
    }

    public void setData(List<LeadDetails> lists) {
        this.lists = lists;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ItemRowLeadsBinding mItemBinding;

        MyViewHolder(ItemRowLeadsBinding itemBinding)
        {
            super(itemBinding.getRoot());
            mItemBinding=itemBinding;
        }
    }
}
