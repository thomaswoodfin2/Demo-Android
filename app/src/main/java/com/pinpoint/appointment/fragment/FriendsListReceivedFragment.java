package com.pinpoint.appointment.fragment;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import com.pinpoint.appointment.activities.FriendLocationTracking;
import com.pinpoint.appointment.adapter.AdpFriendsReceivedRecyclerView;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.ResponseStatus;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.databinding.FragmentFriendsListBinding;
import com.pinpoint.appointment.enumeration.AppointMentStatus;
import com.pinpoint.appointment.enumeration.RequestStatus;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.AppointmentData;
import com.pinpoint.appointment.models.FriendDetails;
import com.pinpoint.appointment.models.TrackingFriend;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsListReceivedFragment extends Fragment implements ClickEvent {

    public static final String VIEW_PAGER_POSITION = "viewPagerPosition";
    private int selectedPosition;
    private HomeActivity parent;
    private LinearLayoutManager mLayoutManager;

    private Bundle bundle;

    private boolean isLoading = true;
    private boolean isLastPage = false;
    FragmentFriendsListBinding friendsListBinding;
    private AdpFriendsReceivedRecyclerView recyclerViewAdapter;
    private List<FriendDetails> friendList = new ArrayList<>();
    Context mContext;
    int end = 10, pagesize = 10;
    private int pageNo = 1;
    Dialog dialog, dialogButton;
    private boolean isVisible = false;
    private boolean isStarted = false;
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
                        FriendDetails.getFriendsData(parent, String.valueOf(pageNo), String.valueOf(end), bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
                    }
                }

            }
        }
    };

    public FriendsListReceivedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parent = (HomeActivity) getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        friendsListBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_friends_list, container, false);
        View rootView = friendsListBinding.getRoot();

//        parent.tv_header.setAllCaps(true);
        mContext = rootView.getContext();
        return rootView;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parent = (HomeActivity) getActivity();
        parent.iv_back.setVisibility(View.GONE);
        parent.iv_settings.setVisibility(View.VISIBLE);
        parent.tv_header.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_friends_header)));
//
        pageNo = BaseConstants.PAGE_NO;

        mLayoutManager = new LinearLayoutManager(parent.getApplicationContext());
        friendsListBinding.recyclerView.setLayoutManager(mLayoutManager);
        friendsListBinding.recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
        friendsListBinding.recyclerView.setVisibility(View.VISIBLE);

        friendsListBinding.tvNoData.setVisibility(View.GONE);
        friendsListBinding.tvNoData.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        friendsListBinding.tvNoData.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_no_record_found)));
        bundle = getArguments();

//        FriendDetails.getFriendsData(parent, String.valueOf(pageNo), String.valueOf(end),bundle.getInt(VIEW_PAGER_POSITION), dataObserver);


    }


    /**
     * bind dashboard list in recycler view
     *
     * @return void
     **/
    private void bindPreferredList() {
        if (friendList != null && friendList.size() > BaseConstants.ZERO) {
            setNoData(false);
            recyclerViewAdapter = (AdpFriendsReceivedRecyclerView) friendsListBinding.recyclerView.getAdapter();
            if (recyclerViewAdapter != null /*&& recyclerViewAdapter.getItemCount() > BaseConstant.ZERO*/) {
                recyclerViewAdapter.setData(friendList);
            } else {
                recyclerViewAdapter = new AdpFriendsReceivedRecyclerView(parent, parent, friendList);

//                recyclerViewAdapter = new AdpPreferredRecyclerView(getActivity(), parent, friendList);
                friendsListBinding.recyclerView.setAdapter(recyclerViewAdapter);
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
            friendsListBinding.recyclerView.setVisibility(View.GONE);
            friendsListBinding.tvNoData.setVisibility(View.VISIBLE);
            if (dialog.isShowing())
                dialog.dismiss();
        } else {
            friendsListBinding.recyclerView.setVisibility(View.VISIBLE);
            friendsListBinding.tvNoData.setVisibility(View.GONE);
        }

    }


    @Override
    public void onClickEvent(View view) {
        switch (view.getId()) {
            case R.id.bt_accept:
            case R.id.iv_imgDelete:
                selectedPosition = (int) view.getTag();
                if (friendList.get(selectedPosition).getRequest_status().equalsIgnoreCase(AppointMentStatus.PENDING.getType())) {
                    FriendDetails.acceptFriendRequest(parent, String.valueOf(friendList.get((int) view.getTag()).getUserid()), 0, dataObserver);
                } else if (friendList.get(selectedPosition).getRequest_status().equalsIgnoreCase(AppointMentStatus.ACCEPTED.getType())) {
                    CustomDialog.getInstance().showAlertWithButtonClick(parent, MessageHelper.getInstance().getAppMessage(getString(R.string.str_confirm_delete_friend)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();
//                            FriendDetails.denyFriendRequest(parent,String.valueOf(friendList.get(selectedPosition).getUserid()),0,dataObserver);
                            FriendDetails.deleteFriendRequest(parent, String.valueOf(friendList.get(selectedPosition).getUserid()), 0, dataObserver);

                        }
                    }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)), view1 -> CustomDialog.getInstance().hide(), true);
                }
                break;
//            case R.id.iv_imgLocation:
            case R.id.bt_Locate:
                selectedPosition = (int) view.getTag();
                if (friendList.get(selectedPosition).getRequest_status().equalsIgnoreCase(AppointMentStatus.PENDING.getType())) {
                    CustomDialog.getInstance().showAlertWithButtonClick(parent, MessageHelper.getInstance().getAppMessage(getString(R.string.str_confirm_deny)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();
                            FriendDetails.denyFriendRequest(parent, String.valueOf(friendList.get(selectedPosition).getUserid()), 0, dataObserver);
                        }
                    }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)), view12 -> CustomDialog.getInstance().hide(), true);

                } else if (friendList.get(selectedPosition).getRequest_status().equalsIgnoreCase(AppointMentStatus.ACCEPTED.getType())) {
                    showProgressBar(mContext);
                    TrackingFriend.getLastTrackingDetails(parent, friendList.get(selectedPosition).getUserid(), dataObserver, true);

                }
//                FriendDetails.denyFriendRequest(parent,String.valueOf(friendList.get((int) view.getTag()).getUserid()),0,dataObserver);
                break;
        }
    }


    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case friendslist:

                    if (object != null) {
                        if (pageNo == 1) {
                            friendList = new ArrayList<>();
                            isLastPage = false;
                        }
                        List<AppointmentData> data = (List<AppointmentData>) object;
                        if (data.size() < pagesize) {
                            isLastPage = true;
                        }
                        friendList.addAll((List<FriendDetails>) object);
//                        if (recyclerViewAdapter!=null&&recyclerViewAdapter.getItemCount() == PrefHelper.getInt(PrefHelper.KEY_TOTAL_RECORDS, 0))
//                            isLastPage = true;

                        isLoading = true;

                        bindPreferredList();
                    } else {
                        if (pageNo == 1) setNoData(true);
                        isLastPage = true;
                    }
                    break;

                case acceptfriendrequest:
                    CustomDialog.getInstance().hide();
                    friendList.get(selectedPosition).setRequest_status(RequestStatus.ACCEPTED.getType());
                    recyclerViewAdapter.setData(friendList);
                    String response = (String) object;
                    JSONObject mJobjResponse1 = null;
                    try {
                        mJobjResponse1 = new JSONObject(response);
                        String status = mJobjResponse1.getString(ApiList.KEY_STATUS);
                        String message = mJobjResponse1.getString(ApiList.KEY_MESSAGE);
                        ToastHelper.displayCustomToast(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case deletefriend:
                    CustomDialog.getInstance().hide();
                    friendList.remove(selectedPosition);
                    recyclerViewAdapter.notifyDataSetChanged();
                    String responseq = (String) object;
                    if (friendList.size() == 0) {
                        setNoData(true);
                    }

                    try {
                        JSONObject mJobjResponse = new JSONObject(responseq);

                        String status = mJobjResponse.getString(ApiList.KEY_STATUS);
                        String message = mJobjResponse.getString(ApiList.KEY_MESSAGE);
                        ToastHelper.displayCustomToast(message);
//                        CustomDialog.getInstance().showAlertWithButtonClick(parent, message, "Pin Point Appointment", "ok", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                CustomDialog.getInstance().hide();
//                            }
//                        }, "Cancel", new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                CustomDialog.getInstance().hide();
//                            }
//                        },false);
//                    }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

                case denyfriendrequest:
                    CustomDialog.getInstance().hide();
                    friendList.remove(selectedPosition);
                    recyclerViewAdapter.notifyDataSetChanged();
                    String response1 = (String) object;
                    JSONObject mJobjResponse = null;
                    try {
                        mJobjResponse = new JSONObject(response1);
                        String status = mJobjResponse.getString(ApiList.KEY_STATUS);
                        String message = mJobjResponse.getString(ApiList.KEY_MESSAGE);
                        ToastHelper.displayCustomToast(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    if (friendList.size() == 0) {
                        setNoData(true);
                    }

                    break;

                case getfriendlocation:
                    final List<TrackingFriend> trackDataList = (List<TrackingFriend>) object;
                    if (trackDataList != null && trackDataList.size() > 0 && trackDataList.get(0).getLatitude() != null) {
                        final TrackingFriend trackData = trackDataList.get(0);
                     /*   if (trackData.getOnlineStatus().equalsIgnoreCase("0")) {
                            CustomDialog.getInstance().hide();
                            if (dialog != null && dialog.isShowing())
                                dialog.dismiss();
                            CustomDialog.getInstance().showAlertWithButtonClick(parent, friendList.get(selectedPosition).getName() + " " + MessageHelper.getInstance().getAppMessage(getString(R.string.str_user_offline_message)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    CustomDialog.getInstance().hide();
                                }
                            }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    CustomDialog.getInstance().hide();
                                }
                            }, false);
                        } else {*/

                            Intent ina = new Intent(mContext, FriendLocationTracking.class);
                            ina.putExtra(BaseConstants.KEY_FROM, "friend");
                            ina.putExtra(BaseConstants.KEY_OBJECT, friendList.get(selectedPosition));
                            ina.putExtra(ApiList.KEY_FRIENDID, friendList.get(selectedPosition).getUserid());
                            startActivity(ina);
                            if (dialog != null && dialog.isShowing())
                                dialog.dismiss();
//                            CustomDialog.getInstance().hide();
                        //}
                    }
                    break;
            }
        }

        @Override
        public void OnFailure(RequestCode requestCode, final String errorCode, final String error) {

            switch (requestCode) {
                case getfriendlocation:
                    if (dialog != null && dialog.isShowing())
                        dialog.dismiss();

                    if (!errorCode.equalsIgnoreCase(ResponseStatus.STATUS_CRASH)) {
                        CustomDialog.getInstance().showAlertWithButtonClick(parent, errorCode, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomDialog.getInstance().hide();
//                              AppointmentData.getAppointmentData(parent, String.valueOf(pageNo), String.valueOf(end),bundle.getInt(VIEW_PAGER_POSITION), dataObserver);

                            }
                        }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomDialog.getInstance().hide();
                            }
                        }, false);
                    }

                    break;
                default:
//                    CustomDialog.getInstance().hide();
//                    if (dialog != null && dialog.isShowing())
//                        dialog.dismiss();
                    if (errorCode.equalsIgnoreCase(ResponseStatus.STATUS_CRASH)) {
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                if (dialog != null && dialog.isShowing())
                                    dialog.dismiss();

//                                CustomDialog.getInstance().hide();
                                if (Util.checkInternetConnectionConnected()) {
                                    showProgressBar(mContext);
                                    FriendDetails.getFriendsData(parent, String.valueOf(pageNo), String.valueOf(end), bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
                                } else {

                                    showAlertWithButtonClick(parent, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_server_error)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
//                                            CustomDialog.getInstance().hide();
                                            if (dialogButton != null && dialogButton.isShowing())
                                                dialogButton.dismiss();
                                            showProgressBar(mContext);
                                            FriendDetails.getFriendsData(parent, String.valueOf(pageNo), String.valueOf(end), bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
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

//                        CustomDialog.getInstance().showAlertWithButtonClick(parent, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_server_error)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                CustomDialog.getInstance().hide();
//                                FriendDetails.getFriendsData(parent, String.valueOf(pageNo), String.valueOf(end), bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
//                            }
//                        }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                CustomDialog.getInstance().hide();
//                            }
//                        },false);
                    } else if (friendList == null || friendList.size() == 0) {
                        if (dialog != null && dialog.isShowing())
                            dialog.dismiss();

//                        CustomDialog.getInstance().hide();
                        friendsListBinding.tvNoData.setText(error);
                        setNoData(true);
                    }


            }
        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {
            CustomDialog.getInstance().hide();
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            if (friendList == null || friendList.size() == 0) {
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

    @Override
    public void onStart() {
        super.onStart();
        isStarted = true;
        if (isVisible) {
            showProgressBar(mContext);
            FriendDetails.getFriendsData(parent, String.valueOf(pageNo), String.valueOf(end), bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;
        if (isVisible && isStarted) {
//            sendRequest();
            if (friendList.size() == 0)
                showProgressBar(mContext);

            pageNo = 1;
            FriendDetails.getFriendsData(parent, String.valueOf(pageNo), String.valueOf(end), bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
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
