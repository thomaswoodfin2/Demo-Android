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
import androidx.recyclerview.widget.DefaultItemAnimator;
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
import com.pinpoint.appointment.activities.AddLeadsActivity;
import com.pinpoint.appointment.activities.AddPropertyActivity;
import com.pinpoint.appointment.activities.HomeActivity;
import com.pinpoint.appointment.activities.ViewLeadsActivity;
import com.pinpoint.appointment.adapter.AdpOpenhouseRecyclerView;

import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.ResponseStatus;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.databinding.FragmentOpenhouseListBinding;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.PropertyData;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class OpenHouseRegistryFragment extends Fragment implements ClickEvent {


    private int selectedPosition;
    private HomeActivity parent;
    private LinearLayoutManager mLayoutManager;

    private Bundle bundle;
    private int pageNo = 1, limit = 10;
    ;

    private boolean isLoading = true;
    private boolean isLastPage = false;
    FragmentOpenhouseListBinding openhouseListBinding;
    Dialog dialog, dialogButton;

    private AdpOpenhouseRecyclerView recyclerViewAdapter;
    private List<PropertyData> propertyList = new ArrayList<>();
    Context mContext;

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
                        PropertyData.getPropertyData(parent, pageNo, limit, dataObserver);
                    }
                }
            }
        }
    };

    DataObserver dataObserver = new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case propertylisting:
                    if (object != null) {
                        if (pageNo == 1) {
                            propertyList = new ArrayList<>();
                            isLastPage = false;
                        }
                        propertyList.addAll((List<PropertyData>) object);
                        if (recyclerViewAdapter != null && recyclerViewAdapter.getItemCount() == PrefHelper.getInt(PrefHelper.KEY_TOTAL_RECORDS, 0))
                            isLastPage = true;

                        isLoading = true;

                        bindPreferredList();
                    } else {
                        if (pageNo == 1) setNoData(true);
                        isLastPage = true;
                    }
                    break;
                case deleteproperty:
                    CustomDialog.getInstance().hide();
                    propertyList.remove(selectedPosition);
                    recyclerViewAdapter.setData(propertyList);
                    String response = (String) object;
                    if (propertyList.size() == 0) {
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
            }
        }

        @Override
        public void OnFailure(RequestCode requestCode, final String errorCode, final String error) {


            switch (requestCode) {
                case propertylisting:
                    if (errorCode.equalsIgnoreCase(ResponseStatus.STATUS_CRASH)) {
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {

                                CustomDialog.getInstance().hide();
                                if (Util.checkInternetConnectionConnected()) {

                                    PropertyData.getPropertyData(parent, pageNo, limit, dataObserver);
//                                  FriendDetails.getFriendsData(parent, String.valueOf(pageNo), String.valueOf(end), bundle.getInt(VIEW_PAGER_POSITION), dataObserver);
                                } else {
                                    showAlertWithButtonClick(parent, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_server_error)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            CustomDialog.getInstance().hide();
                                            if (dialogButton != null && dialogButton.isShowing())
                                                dialogButton.dismiss();
                                            PropertyData.getPropertyData(parent, pageNo, limit, dataObserver);
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


                    } else if (propertyList == null || propertyList.size() == 0) {
                        CustomDialog.getInstance().hide();
                        openhouseListBinding.tvNoData.setText(error);
                        setNoData(true);
                    }

//                    if (propertyList == null || propertyList.size() == 0) {
//                        openhouseListBinding.tvNoData.setText(error);
//                        setNoData(true);
//                    }
                    break;
                case deleteproperty:
                    CustomDialog.getInstance().hide();
                    CustomDialog.getInstance().showAlert(parent, error, errorCode, Util.getAppKeyValue(parent, R.string.lblOk), Util.getAppKeyValue(parent, R.string.str_dismiss), false);
                    break;
            }

        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {
            CustomDialog.getInstance().hide();
            if (propertyList == null || propertyList.size() == 0) {
                setNoData(true);
            }
        }

        @Override
        public void onRetryRequest(RequestCode requestCode) {
            CustomDialog.getInstance().hide();
        }
    };


    public OpenHouseRegistryFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parent = (HomeActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        openhouseListBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_openhouse_list, container, false);
        View rootView = openhouseListBinding.getRoot();

        mContext = rootView.getContext();

        return rootView;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        parent = (HomeActivity) getActivity();
        parent.iv_back.setVisibility(View.GONE);
        parent.iv_settings.setVisibility(View.GONE);
        parent.tv_addNew.setVisibility(View.VISIBLE);
        parent.tv_header.setText(Util.getAppKeyValue(mContext, R.string.str_open_house_registry));
        pageNo = BaseConstants.PAGE_NO;
        mLayoutManager = new LinearLayoutManager(parent.getApplicationContext());
        openhouseListBinding.recyclerView.setLayoutManager(mLayoutManager);
        openhouseListBinding.recyclerView.setItemAnimator(new DefaultItemAnimator());
        openhouseListBinding.recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
        openhouseListBinding.recyclerView.setVisibility(View.VISIBLE);
        recyclerViewAdapter = new AdpOpenhouseRecyclerView(mContext, parent, propertyList);
        openhouseListBinding.recyclerView.setAdapter(recyclerViewAdapter);
        openhouseListBinding.tvNoData.setVisibility(View.GONE);
        openhouseListBinding.tvNoData.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        openhouseListBinding.tvNoData.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_no_record_found)));
        bundle = getArguments();
        PropertyData.getPropertyData(parent, pageNo, limit, dataObserver);
    }

    private void bindPreferredList() {
        if (propertyList != null && propertyList.size() > BaseConstants.ZERO) {
            setNoData(false);
            recyclerViewAdapter = (AdpOpenhouseRecyclerView) openhouseListBinding.recyclerView.getAdapter();
            if (recyclerViewAdapter != null /*&& recyclerViewAdapter.getItemCount() > BaseConstant.ZERO*/) {
                recyclerViewAdapter.setData(propertyList);
            } else {
                recyclerViewAdapter = new AdpOpenhouseRecyclerView(getActivity(), parent, propertyList);
                openhouseListBinding.recyclerView.setAdapter(recyclerViewAdapter);
            }
        } else {
            setNoData(true);
        }
        CustomDialog.getInstance().hide();
    }

    /**
     * Bind client list to adapter
     *
     * @param isSetNoData (boolean) is data available or need to display no data text
     * @return (void)
     */
    private void setNoData(boolean isSetNoData) {
        if (isSetNoData) {
            openhouseListBinding.recyclerView.setVisibility(View.GONE);
            openhouseListBinding.tvNoData.setVisibility(View.VISIBLE);
            CustomDialog.getInstance().hide();
        } else {
            openhouseListBinding.recyclerView.setVisibility(View.VISIBLE);
            openhouseListBinding.tvNoData.setVisibility(View.GONE);
        }

    }


    @Override
    public void onClickEvent(View view) {
        final int position = (int) view.getTag();
        selectedPosition = position;
        switch (view.getId()) {
            case R.id.bt_edit:
                Intent ina = new Intent(parent, AddPropertyActivity.class);
                ina.putExtra(BaseConstants.KEY_OBJECT, propertyList.get(position));
                startActivity(ina);
                break;
            case R.id.bt_delete:
                CustomDialog.getInstance().showAlertWithButtonClick(parent, Util.getAppKeyValue(parent, R.string.str_property_delete_confirm), Util.getAppKeyValue(parent, R.string.str_app_name), Util.getAppKeyValue(parent, R.string.str_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomDialog.getInstance().hide();
                        PropertyData.deleteProperty(parent, Integer.parseInt(propertyList.get(position).getId()), dataObserver);
                    }
                }, Util.getAppKeyValue(parent, R.string.str_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomDialog.getInstance().hide();
                    }
                }, true);
                break;
            case R.id.bt_leads:
                Intent inl = new Intent(parent, ViewLeadsActivity.class);
                inl.putExtra(BaseConstants.KEY_PROPERTY_ID, Integer.parseInt(propertyList.get(position).getId()));
                startActivity(inl);

                break;
            case R.id.bt_start:
                Intent inb = new Intent(parent, AddLeadsActivity.class);
                inb.putExtra(BaseConstants.KEY_PROPERTY_ID, Integer.parseInt(propertyList.get(position).getId()));
                startActivity(inb);

                break;
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
