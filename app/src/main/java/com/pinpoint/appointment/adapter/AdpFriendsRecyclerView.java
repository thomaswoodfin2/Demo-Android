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
import com.pinpoint.appointment.databinding.ItemRowFriendsBinding;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.models.FriendDetails;
import com.pinpoint.appointment.models.LoginHelper;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by admin on 28-Jul-17.
 */

public class AdpFriendsRecyclerView extends RecyclerView.Adapter<AdpFriendsRecyclerView.MyViewHolder> {

    private final Context context;
    private List<FriendDetails> lists;
    private HomeActivity parent;
    private LayoutInflater layoutInflater;
    public AdpFriendsRecyclerView(Context context, HomeActivity _parent, List<FriendDetails> lists) {
        this.context = context;
        this.lists = lists;
        parent = _parent;
    }

    @Override
    public AdpFriendsRecyclerView.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        ItemRowFriendsBinding binding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_row_friends, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(AdpFriendsRecyclerView.MyViewHolder holder, int position) {


        holder.mItemBinding.tvEmail.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        holder.mItemBinding.tvName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        holder.mItemBinding.textView.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));

        FriendDetails data=lists.get(position);
        holder.mItemBinding.tvName.setText(data.getName());

        boolean isReceive = !data.getFriend_direction().equalsIgnoreCase("Create"); //!LoginHelper.getInstance().getEmail().equals(data.getEmail());

        if(data.getEmail().equalsIgnoreCase(""))
        {
            holder.mItemBinding.llEmail.setVisibility(View.GONE);

        }
        else
        {
            holder.mItemBinding.llEmail.setVisibility(View.VISIBLE);
            holder.mItemBinding.tvEmail.setText(data.getEmail());
        }
        holder.mItemBinding.textView.setText(data.getPhone());
        Picasso.get().load(data.getProfileimage()).noFade().placeholder(R.drawable.user1)
                .into(holder.mItemBinding.ivImgProfile);
        holder.mItemBinding.btAccept.setVisibility(View.GONE);

        if(isReceive) {
            if(data.getRequest_status().equalsIgnoreCase("Accepted"))
            {

                holder.mItemBinding.btLocate.setBackground(context.getResources().getDrawable(R.drawable.primary_btn_rounded_background));
                holder.mItemBinding.btLocate.setText(MessageHelper.getInstance().getAppMessage(context.getString(R.string.str_locate)));
                holder.mItemBinding.btAccept.setVisibility(View.GONE);
                holder.mItemBinding.ivImgDelete.setVisibility(View.VISIBLE);
            }
            else
            {
                holder.mItemBinding.btLocate.setBackground(context.getResources().getDrawable(R.drawable.red_btn_background));
                holder.mItemBinding.btLocate.setText(MessageHelper.getInstance().getAppMessage(context.getString(R.string.str_deny)));
                holder.mItemBinding.btAccept.setText(MessageHelper.getInstance().getAppMessage(context.getString(R.string.str_accept)));
                holder.mItemBinding.ivImgDelete.setVisibility(View.GONE);
                holder.mItemBinding.btAccept.setVisibility(View.VISIBLE);
            }
        }else{
            if (data.getRequest_status().equalsIgnoreCase("Accepted")) {
                holder.mItemBinding.btLocate.setText(MessageHelper.getInstance().getAppMessage(context.getString(R.string.str_locate)));
//            holder.mItemBinding.ivImgLocation.setImageResource((R.mipmap.ic_location));
            } else {
                holder.mItemBinding.btLocate.setText(MessageHelper.getInstance().getAppMessage(context.getString(R.string.str_resend)));
//            holder.mItemBinding.ivImgLocation.setImageResource((R.mipmap.ic_resend));
            }
        }

        if(data.getFriend_direction().equalsIgnoreCase("Receive"))
            holder.mItemBinding.tvStatus.setText("Received");
        else
            holder.mItemBinding.tvStatus.setText("Sent");

        holder.mItemBinding.ivImgDelete.setTag(position);
        holder.mItemBinding.ivImgLocation.setTag(position);
        holder.mItemBinding.btAccept.setTag(position);
        holder.mItemBinding.btLocate.setTag(position);
    }

    public void setData(List<FriendDetails> lists) {
        this.lists = lists;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ItemRowFriendsBinding mItemBinding;

        MyViewHolder(ItemRowFriendsBinding itemBinding)
        {
            super(itemBinding.getRoot());
            mItemBinding=itemBinding;

        }
    }
}
