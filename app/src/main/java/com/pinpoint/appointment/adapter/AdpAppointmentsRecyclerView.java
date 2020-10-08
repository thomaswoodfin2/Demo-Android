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
import com.pinpoint.appointment.databinding.ItemRowAppointmentsBinding;
import com.pinpoint.appointment.enumeration.AppointMentStatus;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.models.AppointmentData;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by admin on 28-Jul-17.
 */

public class AdpAppointmentsRecyclerView extends RecyclerView.Adapter<AdpAppointmentsRecyclerView.MyViewHolder> {

    private final Context context;
    private List<AppointmentData> lists;
    private HomeActivity parent;
    private LayoutInflater layoutInflater;

    public AdpAppointmentsRecyclerView(Context context, HomeActivity _parent, List<AppointmentData> lists) {
        this.context = context;
        this.lists = lists;
        parent = _parent;
    }

    @Override
    public AdpAppointmentsRecyclerView.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (layoutInflater == null) {
            layoutInflater = LayoutInflater.from(parent.getContext());
        }
        ItemRowAppointmentsBinding binding =
                DataBindingUtil.inflate(layoutInflater, R.layout.item_row_appointments, parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(AdpAppointmentsRecyclerView.MyViewHolder holder, int position) {

        holder.mItemBinding.tvName.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        holder.mItemBinding.tvAddress.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        holder.mItemBinding.tvPhone.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        holder.mItemBinding.textView.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        holder.mItemBinding.tvStatus.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        holder.mItemBinding.btLocate.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        holder.mItemBinding.btAccep.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        AppointmentData data=lists.get(position);
        holder.mItemBinding.tvName.setText(data.getName());
        holder.mItemBinding.tvAddress.setText(data.getAddress());
        holder.mItemBinding.tvPhone.setText(data.getPhone());
        holder.mItemBinding.textView.setText(data.getAppointDate()+"  "+data.getAppointTime());
        Picasso.get().load(data.getProfileimage()).noFade().placeholder(R.drawable.user1)
                .into(holder.mItemBinding.ivImgProfile);
        holder.mItemBinding.ivImgDelete.setTag(position);
        holder.mItemBinding.btLocate.setTag(position);
//        holder.mItemBinding.ivMgView.setTag(position);
        holder.mItemBinding.tvStatus.setText(data.getAppointmentStatus());
        if(data.getAppointmentStatus().equalsIgnoreCase(AppointMentStatus.ACCEPTED.getType()))
        {
            holder.mItemBinding.ivImgDelete.setVisibility(View.VISIBLE);
            holder.mItemBinding.ivImgLocation.setVisibility(View.GONE);
            holder.mItemBinding.ivImgLine.setVisibility(View.GONE);
            holder.mItemBinding.btLocate.setVisibility(View.VISIBLE);
            holder.mItemBinding.btAccep.setVisibility(View.GONE);
            holder.mItemBinding.btLocate.setText(MessageHelper.getInstance().getAppMessage(context.getResources().getString(R.string.str_locate)));
            holder.mItemBinding.tvStatus.setTextColor(context.getResources().getColor(R.color.light_green));
        }
        else if(data.getAppointmentStatus().equalsIgnoreCase(AppointMentStatus.PENDING.getType()))
            {
                holder.mItemBinding.ivImgDelete.setVisibility(View.VISIBLE);
                holder.mItemBinding.ivImgLocation.setVisibility(View.GONE);
                holder.mItemBinding.ivImgLine.setVisibility(View.GONE);
                holder.mItemBinding.btLocate.setVisibility(View.VISIBLE);
                holder.mItemBinding.btAccep.setVisibility(View.GONE);
                holder.mItemBinding.btLocate.setText(MessageHelper.getInstance().getAppMessage(context.getResources().getString(R.string.str_resend)));
                holder.mItemBinding.tvStatus.setTextColor(context.getResources().getColor(R.color.notificationphone));
            }
        else if (data.getAppointmentStatus().equalsIgnoreCase(AppointMentStatus.EXPIRED.getType())) {

            holder.mItemBinding.ivImgDelete.setVisibility(View.VISIBLE);
            holder.mItemBinding.ivImgLocation.setVisibility(View.GONE);
            holder.mItemBinding.ivImgLine.setVisibility(View.GONE);

            holder.mItemBinding.btLocate.setVisibility(View.GONE);
            holder.mItemBinding.btAccep.setVisibility(View.GONE);

            holder.mItemBinding.tvStatus.setTextColor(context.getResources().getColor(R.color.light_red));
        }
            else
        {
            holder.mItemBinding.ivImgDelete.setVisibility(View.VISIBLE);
            holder.mItemBinding.ivImgLocation.setVisibility(View.GONE);
            holder.mItemBinding.ivImgLine.setVisibility(View.GONE);
            holder.mItemBinding.tvStatus.setTextColor(context.getResources().getColor(R.color.red));
        }

    }

    public void setData(List<AppointmentData> lists) {
        this.lists = lists;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        ItemRowAppointmentsBinding mItemBinding;

        MyViewHolder(ItemRowAppointmentsBinding itemBinding) {
            super(itemBinding.getRoot());
            mItemBinding = itemBinding;

        }
    }
}
