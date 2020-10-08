package com.pinpoint.appointment.adapter;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.activities.HomeActivity;
import com.pinpoint.appointment.databinding.ItemRowNotificationBinding;
import com.pinpoint.appointment.models.NotificationDetails;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by admin on 28-Jul-17.
 */

public class AdpNotificationsRecyclerView extends RecyclerView.Adapter<AdpNotificationsRecyclerView.MyViewHolder> {

    private final Context context;
    private List<NotificationDetails> lists;
    private HomeActivity parent;
    private LayoutInflater layoutInflater;

    public AdpNotificationsRecyclerView(Context context, HomeActivity _parent, List<NotificationDetails> lists) {
        this.context = context;
        this.lists = lists;
        parent = _parent;
    }

    @Override
    public AdpNotificationsRecyclerView.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        ItemRowNotificationBinding binding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_row_notification, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(AdpNotificationsRecyclerView.MyViewHolder holder, int position) {

        holder.mItemBinding.tvTime.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        holder.mItemBinding.tvMessage.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        holder.mItemBinding.tvPhone.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        NotificationDetails notificationDetails=lists.get(position);

            if(notificationDetails.getStatus()==0) {
                holder.mItemBinding.llParent.setBackgroundColor(ContextCompat.getColor(context, R.color.notificationbackground));
            }
            else
            {
                holder.mItemBinding.llParent.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            }
//        }

        try {
            holder.mItemBinding.tvMessage.setText(Html.fromHtml(notificationDetails.getMessage()));
        }catch (Exception ex)
        {
            holder.mItemBinding.tvMessage.setText(notificationDetails.getMessage());
        }
        holder.mItemBinding.ivImgCancel.setTag(position);
        holder.mItemBinding.ivArrow.setTag(position);
        holder.mItemBinding.ivImgProfile.setTag(position);
        holder.mItemBinding.llNotificationData.setTag(position);


        holder.mItemBinding.tvPhone.setText(notificationDetails.getContact());
        holder.mItemBinding.tvTime.setText(notificationDetails.getNotidate()+" "+notificationDetails.getNotitime());
        Picasso.get().load(notificationDetails.getProfileimage()).noFade().placeholder(R.drawable.user1)
                .into(holder.mItemBinding.ivImgProfile);


    }

    public void setData(List<NotificationDetails> lists) {
        this.lists = lists;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ItemRowNotificationBinding mItemBinding;

        MyViewHolder(ItemRowNotificationBinding itemBinding) {
            super(itemBinding.getRoot());
            mItemBinding = itemBinding;

        }
    }
}
