package com.pinpoint.appointment.fragment;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.activities.HomeActivity;
import com.pinpoint.appointment.activities.PanicLocationTracking;
import com.pinpoint.appointment.adapter.AdpNotificationsRecyclerView;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.ResponseStatus;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.databinding.FragmentNotificationListBinding;
import com.pinpoint.appointment.enumeration.BottomTabsTablet;
import com.pinpoint.appointment.enumeration.NotificationType;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.NotificationDetails;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationListFragment extends Fragment implements ClickEvent {

    public static final String VIEW_PAGER_POSITION = "viewPagerPosition";
    private int selectedPosition;
    private HomeActivity parent;
    private LinearLayoutManager mLayoutManager;
    private Bundle bundle;
    private int pageNo = 1, limit = 10;
    public int selectedItemCount = 0;
    private boolean isLoading = true;
    private boolean isLastPage = false;
    FragmentNotificationListBinding notificationListBinding;

    private AdpNotificationsRecyclerView recyclerViewAdapter;
    private List<NotificationDetails> notificationList = new ArrayList<>();
    private List<NotificationDetails> notificationListAfterDelete = new ArrayList<>();
    Context mContext;
    boolean isMultiSelect = false;
    ActionMode mActionMode;
    Dialog dialog, dialogButton;

    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (dy > BaseConstants.ZERO) //check for scroll down
            {
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int pastVisibleItems = mLayoutManager.findFirstVisibleItemPosition();

                if ((visibleItemCount + pastVisibleItems) >= totalItemCount) {
                    if (isLoading && !isLastPage) {
                        isLoading = false;
                        pageNo++;
                        //TODO: Call API
                        showProgressBar(mContext);
                        NotificationDetails.getFriendsDataNotificationData(parent, pageNo, limit, dataObserver, false);
//                        PreferedAndNonPreferedList.getPreferNonPreferedList_Transporter(parent, dataObserver, bundle.getInt(VIEW_PAGER_POSITION), ++pageNo);
                    }
                }

            }
        }
    };


    public NotificationListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    public void multi_select(int position) {

        if (notificationList.get(position).isSeleceted()) {
            notificationList.get(position).setSeleceted(false);

            selectedItemCount--;
        } else {
            notificationList.get(position).setSeleceted(true);

            selectedItemCount++;
        }
        if (selectedItemCount == 0) {
            isMultiSelect = false;
        }

        recyclerViewAdapter.setData(notificationList);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        notificationListBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_notification_list, container, false);
        View rootView = notificationListBinding.getRoot();

//      parent.tv_header.setAllCaps(true);
        mContext = rootView.getContext();

        return rootView;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parent = (HomeActivity) getActivity();

        if (!Util.isTabletDevice(parent)) {
            parent.clearNotifications();
        } else {

        }
        parent.iv_back.setVisibility(View.GONE);
        parent.iv_settings.setVisibility(View.GONE);
        parent.tv_header.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_alerts)));
        parent.iv_settings.setImageResource(R.mipmap.ic_delete_white);
        pageNo = BaseConstants.PAGE_NO;
        mLayoutManager = new LinearLayoutManager(parent.getApplicationContext());
        notificationListBinding.recyclerView.setLayoutManager(mLayoutManager);
        notificationListBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        notificationListBinding.recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
        notificationListBinding.recyclerView.setVisibility(View.VISIBLE);

        notificationListBinding.tvNoData.setVisibility(View.GONE);
        notificationListBinding.tvNoData.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        notificationListBinding.tvNoData.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_no_record_found)));
        bundle = getArguments();
        showProgressBar(mContext);
        NotificationDetails.getFriendsDataNotificationData(parent, pageNo, limit, dataObserver, false);
    }


    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case getnotification:

                    if (object != null) {
                        if (pageNo == 1) {
                            notificationList = new ArrayList<>();
                            isLastPage = false;
                        }
                        notificationList.addAll((List<NotificationDetails>) object);
                        if (recyclerViewAdapter != null && recyclerViewAdapter.getItemCount() == PrefHelper.getInt(PrefHelper.KEY_TOTAL_RECORDS, 0))
                            isLastPage = true;

                        isLoading = true;

                        bindPreferredList();
                    } else {
                        if (pageNo == 1) setNoData(true);
                        isLastPage = true;
                    }
                    parent.setupNotificationCount();
                    break;
                case deletenotification:
                    CustomDialog.getInstance().hide();
                    if (notificationList.get(selectedPosition).getStatus() == 0) {
                        BaseConstants.NOTIFICATION_COUNT--;
                        PrefHelper.setInt(BaseConstants.COUNT, BaseConstants.NOTIFICATION_COUNT);
                    }
                    parent.setupNotificationCount();
                    notificationList.remove(selectedPosition);
                    recyclerViewAdapter.setData(notificationList);

//                    notificationList=notificationListAfterDelete;
//                    recyclerViewAdapter.setData(notificationList);
//                    pageNo=1;
                    pageNo++;
                    NotificationDetails.getFriendsDataNotificationData(parent, pageNo, limit, dataObserver, false);
                    String response = (String) object;
                    if (notificationList.size() == 0) {
                        setNoData(true);
                    }

                    try {
                        JSONObject mJobjResponse = new JSONObject(response);
                        String status = mJobjResponse.getString("status");
                        String message = mJobjResponse.getString("message");
                        ToastHelper.displayCustomToast(message);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case readnotification:
                    CustomDialog.getInstance().hide();
                    String response1 = (String) object;
                    notificationList.get(selectedPosition).setStatus(1);
                    recyclerViewAdapter.setData(notificationList);
                    BaseConstants.NOTIFICATION_COUNT--;
                    PrefHelper.setInt(BaseConstants.COUNT, BaseConstants.NOTIFICATION_COUNT);

                    parent.setupNotificationCount();
                    if (Util.isTabletDevice(parent)) {

                        if (notificationList.get(selectedPosition).getNote_flag() == NotificationType.APPOINTMENT.getType()) {
                            BaseConstants.FROM_NOTIFICATION = true;
                            if (notificationList.get(selectedPosition).getPost().equalsIgnoreCase("1") || notificationList.get(selectedPosition).getPost().equalsIgnoreCase("9")) {
                                BaseConstants.SELECT_SENT = false;
                            } else {
                                BaseConstants.SELECT_SENT = true;
                            }
                            parent.setCurrentTabFragmentTablet(BottomTabsTablet.APPOINTMENTS.getType());
                        } else if (notificationList.get(selectedPosition).getNote_flag() == NotificationType.FRIEND.getType()) {
                            BaseConstants.FROM_NOTIFICATION = true;
                            if (notificationList.get(selectedPosition).getPost().equalsIgnoreCase("4") || notificationList.get(selectedPosition).getPost().equalsIgnoreCase("7")) {
                                BaseConstants.SELECT_SENT_FRIEND = true;
                            } else {
                                BaseConstants.SELECT_SENT_FRIEND = false;
                            }
                            parent.setCurrentTabFragmentTablet(BottomTabsTablet.FRIENDS.getType());
                        } else if (notificationList.get(selectedPosition).getNote_flag() == NotificationType.PANIC.getType()) {
                            if (checkCurrentDate(notificationList.get(selectedPosition).getTrackingEndTime())) {
                                Intent ina = new Intent(parent, PanicLocationTracking.class);
                                ina.putExtra(BaseConstants.KEY_FROM, "notification");

                                ina.putExtra(BaseConstants.KEY_OBJECT, notificationList.get(selectedPosition));
                                mContext.startActivity(ina);
                            }
                        }
                    } else {
                        TabLayout.Tab tab;
                        if (notificationList.get(selectedPosition).getNote_flag() == NotificationType.APPOINTMENT.getType()) {
                            BaseConstants.FROM_NOTIFICATION = true;
                            if (notificationList.get(selectedPosition).getPost().equalsIgnoreCase("1") || notificationList.get(selectedPosition).getPost().equalsIgnoreCase("9")) {
                                BaseConstants.SELECT_SENT = false;
                            } else {
                                BaseConstants.SELECT_SENT = true;
                            }

                            tab = parent.tabLayout.getTabAt(0);


                            if (tab != null) {
                                tab.select();
                            }
                        } else if (notificationList.get(selectedPosition).getNote_flag() == NotificationType.FRIEND.getType()) {
                            BaseConstants.FROM_NOTIFICATION = true;
                            if (notificationList.get(selectedPosition).getPost().equalsIgnoreCase("4") || notificationList.get(selectedPosition).getPost().equalsIgnoreCase("7")) {
                                BaseConstants.SELECT_SENT_FRIEND = true;
                            } else {
                                BaseConstants.SELECT_SENT_FRIEND = false;
                            }
                            tab = parent.tabLayout.getTabAt(3);
                            if (tab != null) {
                                tab.select();
                            }
                        } else if (notificationList.get(selectedPosition).getNote_flag() == NotificationType.PANIC.getType()) {
                            if (checkCurrentDate(notificationList.get(selectedPosition).getTrackingEndTime())) {
                                Intent ina = new Intent(parent, PanicLocationTracking.class);
                                ina.putExtra(BaseConstants.KEY_FROM, "notification");

                                ina.putExtra(BaseConstants.KEY_OBJECT, notificationList.get(selectedPosition));
                                mContext.startActivity(ina);
                            }
                        }
                    }
                    ShortcutBadger.applyCount(parent, BaseConstants.NOTIFICATION_COUNT);


                    break;
            }
        }

        @Override
        public void OnFailure(RequestCode requestCode, final String errorCode, final String error) {

            if (errorCode != null && errorCode.equalsIgnoreCase(ResponseStatus.STATUS_CRASH)) {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (dialog != null && dialog.isShowing())
                            try{ dialog.dismiss();} catch (IllegalArgumentException e){e.printStackTrace();}

//                        CustomDialog.getInstance().hide();
                        if (Util.checkInternetConnectionConnected()) {
                            showProgressBar(parent);
                            NotificationDetails.getFriendsDataNotificationData(parent, pageNo, limit, dataObserver, false);
//                                    FriendDetails.getFriendsData(parent, String.valueOf(pageNo), String.valueOf(end), bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
                        } else {
                            showAlertWithButtonClick(parent, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_server_error)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
//                                    CustomDialog.getInstance().hide();
                                    if (dialogButton != null && dialogButton.isShowing())
                                        dialogButton.dismiss();
                                    showProgressBar(parent);
                                    NotificationDetails.getFriendsDataNotificationData(parent, pageNo, limit, dataObserver, false);
                                }
                            }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    CustomDialog.getInstance().hide();
                                }
                            }, false);
                        }

                    }
                }, 5000);


            } else if (notificationList == null || notificationList.size() == 0) {
                CustomDialog.getInstance().hide();
                if (dialog != null && dialog.isShowing())
                    dialog.dismiss();
                if (error != null && !error.equalsIgnoreCase("")) {
                    notificationListBinding.tvNoData.setText(error);
                }
                setNoData(true);
            }


        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {
            CustomDialog.getInstance().hide();
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            if (notificationList == null || notificationList.size() == 0) {
                setNoData(true);
            }
        }

        @Override
        public void onRetryRequest(RequestCode requestCode) {
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            CustomDialog.getInstance().hide();
        }
    };

    /**
     * bind dashboard list in recycler view
     *
     * @return void
     **/
    private void bindPreferredList() {
        if (notificationList != null && notificationList.size() > BaseConstants.ZERO) {
            setNoData(false);
            recyclerViewAdapter = (AdpNotificationsRecyclerView) notificationListBinding.recyclerView.getAdapter();
            if (recyclerViewAdapter != null /*&& recyclerViewAdapter.getItemCount() > BaseConstant.ZERO*/) {
                recyclerViewAdapter.setData(notificationList);
            } else {
                recyclerViewAdapter = new AdpNotificationsRecyclerView(getActivity(), parent, notificationList);
                notificationListBinding.recyclerView.setAdapter(recyclerViewAdapter);
            }
        } else {
            setNoData(true);
        }

        if (dialog.isShowing())
            dialog.dismiss();

    }

    /**
     * Bind client list to adapter
     *
     * @param isSetNoData (boolean) is data available or need to display no data text
     * @return (void)
     */
    private void setNoData(boolean isSetNoData) {
        if (isSetNoData) {
            notificationListBinding.recyclerView.setVisibility(View.GONE);
            notificationListBinding.tvNoData.setVisibility(View.VISIBLE);
            if (dialog.isShowing())
                dialog.dismiss();
        } else {
            notificationListBinding.recyclerView.setVisibility(View.VISIBLE);
            notificationListBinding.tvNoData.setVisibility(View.GONE);

        }


    }


    @Override
    public void onClickEvent(View view) {

        switch (view.getId()) {
            case R.id.ll_parent:
//                Intent ina=new Intent(parent, PanicLocationTracking.class);
//                ina.putExtra("from","notification");
//                startActivity(ina);
                break;
            case R.id.iv_imgCancel:


                selectedPosition = (int) view.getTag();
                JSONArray selectedNotificationIds = new JSONArray();
                selectedNotificationIds.put(notificationList.get(selectedPosition).getN_id());

                NotificationDetails.deleteNotification(parent, selectedNotificationIds, dataObserver);

//                ToastHelper.displayCustomToast("Event catched");
                break;
            case R.id.ll_notificationData:
            case R.id.iv_imgProfile:
            case R.id.iv_Arrow:
                selectedPosition = (int) view.getTag();
                readNotificatin(selectedPosition);
                break;

        }
    }

    void readNotificatin(int position) {
        selectedPosition = position;


        if (notificationList.get(position).getStatus() == 0) {
            NotificationDetails.readNotification(parent, notificationList.get(position).getN_id(), dataObserver);

        } else {
            if (Util.isTabletDevice(parent)) {
                if (notificationList.get(selectedPosition).getNote_flag() == NotificationType.APPOINTMENT.getType()) {
                    BaseConstants.FROM_NOTIFICATION = true;
                    if (notificationList.get(selectedPosition).getPost().equalsIgnoreCase("1") || notificationList.get(selectedPosition).getPost().equalsIgnoreCase("9")) {
                        BaseConstants.SELECT_SENT = false;
                    } else {
                        BaseConstants.SELECT_SENT = true;
                    }
                    parent.setCurrentTabFragmentTablet(BottomTabsTablet.APPOINTMENTS.getType());
                } else if (notificationList.get(selectedPosition).getNote_flag() == NotificationType.FRIEND.getType()) {
                    BaseConstants.FROM_NOTIFICATION = true;
                    if (notificationList.get(selectedPosition).getPost().equalsIgnoreCase("4") || notificationList.get(selectedPosition).getPost().equalsIgnoreCase("7")) {
                        BaseConstants.SELECT_SENT_FRIEND = true;
                    } else {
                        BaseConstants.SELECT_SENT_FRIEND = false;
                    }
                    parent.setCurrentTabFragmentTablet(BottomTabsTablet.FRIENDS.getType());
                } else if (notificationList.get(selectedPosition).getNote_flag() == NotificationType.PANIC.getType()) {
                    if (checkCurrentDate(notificationList.get(selectedPosition).getTrackingEndTime())) {
                        Intent ina = new Intent(parent, PanicLocationTracking.class);
                        ina.putExtra(BaseConstants.KEY_FROM, "notification");

                        ina.putExtra(BaseConstants.KEY_OBJECT, notificationList.get(selectedPosition));
                        mContext.startActivity(ina);
                    }
                }
//                parent.setCurrentTabFragmentTablet(BottomTabs.APPOINTMENTS.getType());
            } else {
                TabLayout.Tab tab;
                if (notificationList.get(selectedPosition).getNote_flag() == NotificationType.APPOINTMENT.getType()) {
                    BaseConstants.FROM_NOTIFICATION = true;
                    if (notificationList.get(position).getPost().equalsIgnoreCase("1") || notificationList.get(selectedPosition).getPost().equalsIgnoreCase("9")) {
                        BaseConstants.SELECT_SENT = false;
                    } else {
                        BaseConstants.SELECT_SENT = true;
                    }

                    tab = parent.tabLayout.getTabAt(0);

                    if (tab != null) {
                        tab.select();
                    }

                } else if (notificationList.get(selectedPosition).getNote_flag() == NotificationType.FRIEND.getType()) {
                    BaseConstants.FROM_NOTIFICATION = true;

                    if (notificationList.get(selectedPosition).getPost().equalsIgnoreCase("4") || notificationList.get(selectedPosition).getPost().equalsIgnoreCase("7")) {
                        BaseConstants.SELECT_SENT_FRIEND = true;
                    } else {
                        BaseConstants.SELECT_SENT_FRIEND = false;
                    }
                    tab = parent.tabLayout.getTabAt(3);
                    if (tab != null) {
                        tab.select();
                    }
                } else if (notificationList.get(position).getPost().equalsIgnoreCase("5")) {
                    if (checkCurrentDate(notificationList.get(selectedPosition).getTrackingEndTime())) {

                        Intent ina = new Intent(parent, PanicLocationTracking.class);
                        ina.putExtra(BaseConstants.KEY_FROM, "notification");
                        ina.putExtra(BaseConstants.KEY_OBJECT, notificationList.get(selectedPosition));
                        mContext.startActivity(ina);
                    }
                }
            }
        }

    }

    private boolean checkCurrentDate(String strendDate) {
        SimpleDateFormat format1 = new SimpleDateFormat(BaseConstants.PAYMENTTIME);
        try {
            Date endDate = format1.parse(strendDate);//appointment time
            Date currentdATE = format1.parse(String.valueOf(format1.format(new Date())));//current time


            if (currentdATE.after(endDate)) {
                CustomDialog.getInstance().showAlert(parent, MessageHelper.getInstance().getAppMessage(getString(R.string.str_panic_tracking_error)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_appointments_header)), false);
                return false;
            } else {
                return true;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void showProgressBar(Context context) {
        dialog = new Dialog(context, R.style.DialogTheme);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.custom_progressbar);

        try {
            if (dialog != null) {
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void showAlertWithButtonClick(final Context context, String msg, String header, String positiveButton, View.OnClickListener onClickListener1, String negativeButton, View.OnClickListener onClickListener2, boolean isShowNegative) {
        try {
            dialogButton = new Dialog(context, R.style.DialogTheme);
            @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert_new, null);
            TextView txtHeader = view.findViewById(R.id.txtHeader);
            TextView txtMessage = view.findViewById(R.id.tv_txtMessage);
            TextView txtNegative = view.findViewById(R.id.tv_txtNegative);
            ImageView iv_header = (ImageView) view.findViewById(R.id.iv_header);

            txtHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
            txtMessage.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
            txtNegative.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

            if (header.equalsIgnoreCase("Alert")) {
                iv_header.setImageResource(R.drawable.ic_call_black_24dp);
            } else if (header.equalsIgnoreCase("Logout")) {
                iv_header.setImageResource(R.drawable.ic_info_outline_black_24dp);
            }
            Button btn_positive = view.findViewById(R.id.btn_positive);
            btn_positive.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
            txtHeader.setText(header);
            txtMessage.setText(msg);
            btn_positive.setText(positiveButton);
            txtNegative.setText(negativeButton);
            if (isShowNegative) {
                txtNegative.setVisibility(View.VISIBLE);
            }
//        mTxtYes.setTag(buttonText);*/
            btn_positive.setOnClickListener(onClickListener1);
            txtNegative.setOnClickListener(onClickListener2);

            dialogButton.setCanceledOnTouchOutside(false);
            dialogButton.setContentView(view);
            try {
                if (dialogButton != null) {
                    if (!dialogButton.isShowing()) {
                        dialogButton.show();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
        }
    }

}


