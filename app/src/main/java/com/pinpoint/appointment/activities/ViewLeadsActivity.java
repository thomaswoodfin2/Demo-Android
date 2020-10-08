package com.pinpoint.appointment.activities;


import android.content.Intent;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinpoint.appointment.BaseActivity;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.adapter.AdpLeadsRecyclerView;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.ResponseStatus;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.databinding.FragmentLeadsListBinding;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.LeadDetails;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewLeadsActivity extends BaseActivity implements ClickEvent {


    private ViewLeadsActivity parent;
    private Bundle bundle;
    FragmentLeadsListBinding friendsListBinding;
    private LinearLayoutManager mLayoutManager;
    private AdpLeadsRecyclerView recyclerViewAdapter;
    private List<LeadDetails> leadslist = new ArrayList<>();
    int selectedPosition=0;
    private boolean isLoading = true;
    private boolean isLastPage = false;
    int end=10,pagesize=10;
    private int pageNo=1;
    int propertyId=0;
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
                        LeadDetails.getLeadList(parent, pageNo, end,propertyId, dataObserver);
                    }
                }

            }
        }
    };

    public ViewLeadsActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        friendsListBinding = DataBindingUtil.setContentView(this, R.layout.fragment_leads_list);

        ImageView imgBack= GenericView.findViewById(this,R.id.iv_imgMenu_Back);
        ImageView imgSettings= GenericView.findViewById(this,R.id.iv_imgSetting);
        TextView tvHeader= GenericView.findViewById(this,R.id.tv_txtTitle);
        tvHeader.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_view_leads_header)));
        imgSettings.setImageResource(R.mipmap.ic_add_user);
        imgBack.setImageResource(R.mipmap.ic_back);
        imgSettings.setVisibility(View.VISIBLE);
        tvHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        mLayoutManager = new LinearLayoutManager(this);
        friendsListBinding.recyclerView.setLayoutManager(mLayoutManager);
        friendsListBinding.recyclerView.setVisibility(View.VISIBLE);
        friendsListBinding.recyclerView.addOnScrollListener(recyclerViewOnScrollListener);
//        recyclerViewAdapter = new AdpLeadsRecyclerView(this, parent, leadslist);
//        friendsListBinding.recyclerView.setAdapter(recyclerViewAdapter);
        friendsListBinding.tvNoData.setVisibility(View.GONE);
        friendsListBinding.tvNoData.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        friendsListBinding.tvNoData.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_no_record_found)));
        parent = this;
        propertyId=getIntent().getIntExtra(BaseConstants.KEY_PROPERTY_ID,0);



    }



    DataObserver dataObserver=new DataObserver() {
    @Override
    public void OnSuccess(RequestCode requestCode, Object object) {
        switch (requestCode) {
            case getleads:

                if(object!=null)
                {
                    if (pageNo == 1) {
                        leadslist = new ArrayList<>();
                        isLastPage = false;
                    }
                    List<LeadDetails> data= (List<LeadDetails>) object;
                    if(data.size()<pagesize)
                    {
                        isLastPage=true;
                    }
                    leadslist.addAll((List<LeadDetails>) object);
//                        if (recyclerViewAdapter!=null&&recyclerViewAdapter.getItemCount() == PrefHelper.getInt(PrefHelper.KEY_TOTAL_RECORDS, 0))
//                            isLastPage = true;

                    isLoading = true;

                    bindPreferredList();
                }
                else
                {
                    if (pageNo == 1) setNoData(true);
                    isLastPage = true;
                }

                break;

            case deletelead:
                CustomDialog.getInstance().hide();
                leadslist.remove(selectedPosition);
                recyclerViewAdapter.setData(leadslist);
                String response = (String) object;
                if (leadslist.size() == 0) {
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
    public void OnFailure(RequestCode requestCode, String errorCode, String error)
    {
        switch (requestCode) {
            case getleads:

                CustomDialog.getInstance().hide();

                if(errorCode.equalsIgnoreCase(ResponseStatus.STATUS_CRASH))
                {
                    CustomDialog.getInstance().showAlertWithButtonClick(parent, error, MessageHelper.getInstance().getAppMessage(getString(R.string.str_server_error)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();
                            LeadDetails.getLeadList(parent, pageNo, end,propertyId, dataObserver);
                        }
                    }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();
                        }
                    },false);
                }
                else  if(leadslist==null||leadslist.size()==0) {
                    friendsListBinding.tvNoData.setText(error);
                    setNoData(true);
                }


                break;
        }
    }

    @Override
    public void onOtherStatus(RequestCode requestCode, Object object)
    {

        switch (requestCode) {
            case getleads:
                CustomDialog.getInstance().hide();
                if(leadslist==null||leadslist.size()==0) {
                    setNoData(true);
                }

                break;
        }
    }

    @Override
    public void onRetryRequest(RequestCode requestCode) {

    }
};
    private void bindPreferredList() {
        if (leadslist != null && leadslist.size() > BaseConstants.ZERO) {
            setNoData(false);
            recyclerViewAdapter = (AdpLeadsRecyclerView) friendsListBinding.recyclerView.getAdapter();
            if (recyclerViewAdapter != null /*&& recyclerViewAdapter.getItemCount() > BaseConstant.ZERO*/) {
                recyclerViewAdapter.setData(leadslist);
            } else {
                recyclerViewAdapter = new AdpLeadsRecyclerView(parent, parent, leadslist);
                friendsListBinding.recyclerView.setAdapter(recyclerViewAdapter);
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
            friendsListBinding.recyclerView.setVisibility(View.GONE);
            friendsListBinding.tvNoData.setVisibility(View.VISIBLE);
            CustomDialog.getInstance().hide();
        } else {
            friendsListBinding.recyclerView.setVisibility(View.VISIBLE);
            friendsListBinding.tvNoData.setVisibility(View.GONE);
        }

    }






    @Override
    public void onClickEvent(View view)
    {

        switch(view.getId())
        {
            case R.id.iv_imgSetting:
                Intent ina=new Intent(ViewLeadsActivity.this,AddLeadsActivity.class);
                ina.putExtra(BaseConstants.KEY_PROPERTY_ID,propertyId);
                startActivity(ina);
                break;

            case R.id.iv_imgMenu_Back:
                finish();
                break;


            case R.id.iv_imgDelete:
                selectedPosition= (int) view.getTag();
                CustomDialog.getInstance().showAlertWithButtonClick(parent, Util.getAppKeyValue(parent,R.string.str_lead_delete_confirm), Util.getAppKeyValue(parent,R.string.str_app_name), Util.getAppKeyValue(parent,R.string.str_ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomDialog.getInstance().hide();
                        LeadDetails.deleteLead(parent, Integer.parseInt(leadslist.get(selectedPosition).getId()),dataObserver);
                    }
                }, Util.getAppKeyValue(parent,R.string.str_cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomDialog.getInstance().hide();
                    }
                },true);

                break;
            case R.id.btn_View:
                selectedPosition= (int) view.getTag();
                Intent inb=new Intent(ViewLeadsActivity.this,ViewLeadDetailsActivity.class);
                inb.putExtra(BaseConstants.KEY_OBJECT,leadslist.get(selectedPosition));
                startActivity(inb);
                break;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        pageNo=1;
        LeadDetails.getLeadList(parent, pageNo, end,propertyId, dataObserver);
    }




}
