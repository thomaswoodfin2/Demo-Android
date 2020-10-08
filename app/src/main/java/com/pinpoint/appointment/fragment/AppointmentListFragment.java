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
import com.pinpoint.appointment.activities.LocationTracking;
import com.pinpoint.appointment.adapter.AdpAppointmentsRecyclerView;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.ResponseStatus;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.databinding.FragmentAppointmentlistBinding;
import com.pinpoint.appointment.enumeration.AppointMentStatus;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.AppointmentData;
import com.pinpoint.appointment.models.TrackingFriend;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AppointmentListFragment extends Fragment implements ClickEvent {

    public static final String VIEW_PAGER_POSITION = "viewPagerPosition";
    private int selectedPosition;
    private HomeActivity parent;
    private LinearLayoutManager mLayoutManager;

    private Bundle bundle;

    private boolean isLoading = true;
    private boolean isLastPage = false;
    FragmentAppointmentlistBinding appointmentlistBinding;
    private AdpAppointmentsRecyclerView recyclerViewAdapter;
    private List<AppointmentData> appointmentList = new ArrayList<>();
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
                        //TODO: Call API
                        pageNo++;
                        showProgressBar(parent);
                        AppointmentData.getAppointmentData(parent, String.valueOf(pageNo), String.valueOf(end), bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
//                        PreferedAndNonPreferedList.getPreferNonPreferedList_Transporter(parent, dataObserver, bundle.getInt(VIEW_PAGER_POSITION), ++pageNo);
                    }


                }
            }
        }
    };

    public AppointmentListFragment() {
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

        appointmentlistBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_appointmentlist, container, false);
        View rootView = appointmentlistBinding.getRoot();


//        parent.tv_header.setAllCaps(true);

        return rootView;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parent = (HomeActivity) getActivity();
        parent.iv_back.setVisibility(View.GONE);
        parent.iv_settings.setVisibility(View.VISIBLE);
        parent.tv_header.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_appointments_header)));

//
        pageNo = BaseConstants.PAGE_NO;

        mLayoutManager = new LinearLayoutManager(parent.getApplicationContext());
        appointmentlistBinding.recyclerView.setLayoutManager(mLayoutManager);
        appointmentlistBinding.recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
        appointmentlistBinding.recyclerView.setVisibility(View.VISIBLE);

        appointmentlistBinding.tvNoData.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        appointmentlistBinding.tvNoData.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_no_record_found)));
        bundle = getArguments();
//        AppointmentData.getAppointmentData(parent, String.valueOf(pageNo), String.valueOf(end),bundle.getInt(VIEW_PAGER_POSITION), dataObserver);


    }


    /**
     * bind dashboard list in recycler view
     *
     * @return void
     **/
    private void bindPreferredList() {
        if (appointmentList != null && appointmentList.size() > BaseConstants.ZERO) {
            setNoData(false);
            recyclerViewAdapter = (AdpAppointmentsRecyclerView) appointmentlistBinding.recyclerView.getAdapter();
            if (recyclerViewAdapter != null /*&& recyclerViewAdapter.getItemCount() > BaseConstant.ZERO*/) {
                recyclerViewAdapter.setData(appointmentList);
            } else {
                recyclerViewAdapter = new AdpAppointmentsRecyclerView(getActivity(), parent, appointmentList);
                appointmentlistBinding.recyclerView.setAdapter(recyclerViewAdapter);
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
            appointmentlistBinding.recyclerView.setVisibility(View.GONE);
            appointmentlistBinding.tvNoData.setVisibility(View.VISIBLE);
            if (dialog.isShowing())
                dialog.dismiss();
        } else {
            appointmentlistBinding.recyclerView.setVisibility(View.VISIBLE);
            appointmentlistBinding.tvNoData.setVisibility(View.GONE);
        }

    }


    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case appointmentlist:

                    if (object != null) {
                        if (pageNo == 1) {
                            appointmentList = new ArrayList<>();
                            isLastPage = false;
                        }
                        List<AppointmentData> data = (List<AppointmentData>) object;
                        if (data.size() < pagesize) {
                            isLastPage = true;
                        }
                        appointmentList.addAll((List<AppointmentData>) object);
//                        if (recyclerViewAdapter!=null&&recyclerViewAdapter.getItemCount() == PrefHelper.getInt(PrefHelper.KEY_TOTAL_RECORDS, 0))
//                            isLastPage = true;

                        isLoading = true;

                        bindPreferredList();
                    } else {
                        if (pageNo == 1) setNoData(true);
                        isLastPage = true;
                    }
                    break;
                case deleteappointment:
                    CustomDialog.getInstance().hide();
                    appointmentList.remove(selectedPosition);
                    recyclerViewAdapter.notifyDataSetChanged();
                    String response = (String) object;
                    if (appointmentList.size() == 0) {
                        setNoData(true);
                    }

                    try {
                        JSONObject mJobjResponse = new JSONObject(response);
                        String status = mJobjResponse.getString(ApiList.KEY_STATUS);
                        String message = mJobjResponse.getString(ApiList.KEY_MESSAGE);
                        ToastHelper.displayCustomToast(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case resendappointment:
                    CustomDialog.getInstance().hide();
                    recyclerViewAdapter.notifyDataSetChanged();
                    String response1 = (String) object;

                    try {
                        JSONObject mJobjResponse = new JSONObject(response1);
                        String status = mJobjResponse.getString(ApiList.KEY_STATUS);
                        String message = mJobjResponse.getString(ApiList.KEY_MESSAGE);
                        CustomDialog.getInstance().showAlert(parent, message, MessageHelper.getInstance().getAppMessage(parent.getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(parent.getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(parent.getString(R.string.str_dismiss)), false);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;

                case getfriendlocation:

                    final List<TrackingFriend> trackDataList = (List<TrackingFriend>) object;
                    if (trackDataList != null && trackDataList.size() > 0 && trackDataList.get(0).getLatitude() != null) {
                        final TrackingFriend trackData = trackDataList.get(0);
                        if (trackData.getOnlineStatus().equalsIgnoreCase("0")) {
                            if (dialog != null && dialog.isShowing())
                                dialog.dismiss();

                            CustomDialog.getInstance().hide();
                            CustomDialog.getInstance().showAlertWithButtonClick(parent, appointmentList.get(selectedPosition).getName() + " " + MessageHelper.getInstance().getAppMessage(getString(R.string.str_user_offline_message)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
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
                        } else {
                            Intent ina = new Intent(parent, LocationTracking.class);
                            ina.putExtra(BaseConstants.KEY_FROM, "appointment");
                            appointmentList.get(selectedPosition).setAppointDirection("sent");
                            ina.putExtra(BaseConstants.KEY_OBJECT, appointmentList.get(selectedPosition));
                            startActivity(ina);

                            if (dialog != null && dialog.isShowing())
                                dialog.dismiss();

                            CustomDialog.getInstance().hide();
                        }
                    }
                    break;


            }
        }

        @Override
        public void OnFailure(RequestCode requestCode, final String errorCode, final String error) {
//            CustomDialog.getInstance().hide();
            switch (requestCode) {
                case getfriendlocation:
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    CustomDialog.getInstance().hide();
                    if (!errorCode.equalsIgnoreCase(ResponseStatus.STATUS_CRASH)) {
                        CustomDialog.getInstance().showAlertWithButtonClick(parent, error, errorCode, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomDialog.getInstance().hide();
//                            AppointmentData.getAppointmentData(parent, String.valueOf(pageNo), String.valueOf(end),bundle.getInt(VIEW_PAGER_POSITION), dataObserver);

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

                    if (errorCode.equalsIgnoreCase(ResponseStatus.STATUS_CRASH)) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                if (dialog != null && dialog.isShowing())
                                    dialog.dismiss();

//                                CustomDialog.getInstance().hide();
                                if (Util.checkInternetConnectionConnected()) {
                                    showProgressBar(parent);
                                    AppointmentData.getAppointmentData(parent, String.valueOf(pageNo), String.valueOf(end), bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
//                                    FriendDetails.getFriendsData(parent, String.valueOf(pageNo), String.valueOf(end), bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
                                } else {
                                    showAlertWithButtonClick(parent, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_server_error)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
//                                            CustomDialog.getInstance().hide();
                                            if (dialogButton != null && dialogButton.isShowing())
                                                dialogButton.dismiss();
                                            showProgressBar(parent);
//                                          AppointmentData.getAppointmentData(parent, String.valueOf(pageNo), String.valueOf(end),bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
                                            AppointmentData.getAppointmentData(parent, String.valueOf(pageNo), String.valueOf(end), bundle.getInt(VIEW_PAGER_POSITION), dataObserver);

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
//                        @Override
//                        public void onClick(View view) {
//                            CustomDialog.getInstance().hide();
////                            AppointmentData.getAppointmentData(parent, String.valueOf(pageNo), String.valueOf(end),bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
//                            AppointmentData.getAppointmentData(parent, String.valueOf(pageNo), String.valueOf(end),bundle.getInt(VIEW_PAGER_POSITION), dataObserver);

//                        }
//                    }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            CustomDialog.getInstance().hide();
//                        }
//                    },false);
                    } else if (appointmentList == null || appointmentList.size() == 0) {
//                        CustomDialog.getInstance().hide();
                        if (dialog != null && dialog.isShowing())
                            dialog.dismiss();
                        appointmentlistBinding.tvNoData.setText(error);
                        setNoData(true);
                    }
                    break;
            }


        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {
            CustomDialog.getInstance().hide();
            if (dialog != null && dialog.isShowing())
                dialog.dismiss();
            if (appointmentList == null || appointmentList.size() == 0) {
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

    @Override
    public void onClickEvent(View view) {
        switch (view.getId()) {
            case R.id.iv_imgDelete:
                selectedPosition = (int) view.getTag();
                CustomDialog.getInstance().showAlertWithButtonClick(parent, MessageHelper.getInstance().getAppMessage(getString(R.string.str_confirm_delete_appointment)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomDialog.getInstance().hide();
                        AppointmentData.deleteAppointment(parent, String.valueOf(appointmentList.get(selectedPosition).getAppointmentId()), 0, dataObserver);
                    }
                }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomDialog.getInstance().hide();
                    }
                }, true);
                break;

            case R.id.bt_locate:
                selectedPosition = (int) view.getTag();
                AppointmentData details = appointmentList.get(selectedPosition);
                if (selectedPosition < appointmentList.size()) {
                    if (appointmentList.get(selectedPosition).getAppointmentStatus().equalsIgnoreCase(AppointMentStatus.ACCEPTED.getType())) {
                        if (checkCurrentDate(details.getStartTime(), details.getEndTime())) {

                            showProgressBar(parent);
                            TrackingFriend.getLastTrackingDetails(parent, appointmentList.get(selectedPosition).getUserid(), dataObserver, true);
                        }
                    } else {
                        AppointmentData.resendAppointment(parent, String.valueOf(appointmentList.get(selectedPosition).getAppointmentId()), dataObserver);
                    }
                }

//                details.getAppointDate(), details.getAppointTime()

                break;
        }
    }

    private boolean checkCurrentDate(String strstartDate, String strendDate) {
        SimpleDateFormat format1 = new SimpleDateFormat(BaseConstants.PAYMENTTIME);
        try {
            Date startDate = format1.parse(strstartDate);//appointment time
            Date endDate = format1.parse(strendDate);//appointment time
            Date currentdATE = format1.parse(String.valueOf(format1.format(new Date())));//current time

            if (currentdATE.before(startDate)) {
                CustomDialog.getInstance().showAlert(parent, MessageHelper.getInstance().getAppMessage(getString(R.string.str_appointment_tracking_notstarted)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_appointments_header)), false);
                return false;
            } else if (currentdATE.after(endDate)) {
                CustomDialog.getInstance().showAlert(parent, MessageHelper.getInstance().getAppMessage(getString(R.string.str_appointment_tracking_error)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_appointments_header)), false);
                return false;
            } else {
                return true;
            }


        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        isStarted = true;
        if (isVisible) {
            showProgressBar(parent);
            AppointmentData.getAppointmentData(parent, String.valueOf(pageNo), String.valueOf(end), bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        isVisible = isVisibleToUser;
        if (isVisible && isStarted) {
//            sendRequest();
            if (appointmentList.size() == 0)
                showProgressBar(parent);

            pageNo = 1;
            AppointmentData.getAppointmentData(parent, String.valueOf(pageNo), String.valueOf(end), bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
        }
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
