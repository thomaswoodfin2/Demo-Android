package com.pinpoint.appointment.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pinpoint.appointment.BaseActivity;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.adapter.AdpSubscriptionRecyclerView;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.databinding.ActivityExtendSubscriptionBinding;
import com.pinpoint.appointment.enumeration.SubscriptionStatus;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.models.PlanData;
import com.pinpoint.appointment.util.IabHelper;
import com.pinpoint.appointment.util.IabResult;
import com.pinpoint.appointment.util.Inventory;
import com.pinpoint.appointment.util.Purchase;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ExtendSubscriptionActivity extends BaseActivity implements ClickEvent {

//    private static final String TAG ="com.ebookfrenzy.inappbilling";
private static final String TAG ="TAG:";
    IabHelper mHelper;
//    static final String ITEM_SKU = "android.test.purchased";
    static final String ITEM_SKU_PREMIUM = "pinpoint_premium";


    ExtendSubscriptionActivity parent;
//    int
    private int pageNo=1,pagesize=10;
    List<PlanData> planList=new ArrayList<>();
    private AdpSubscriptionRecyclerView recyclerViewAdapter;
    private boolean isLoading = true;
    private boolean isLastPage = false;
    PlanData planData;
    View view1,view2,view3,view4;
//    ActivityE subscriptionBinding;
    ActivityExtendSubscriptionBinding subscriptionBinding;
    Button btnSubscribe,buyButton;

    int selectedPosition=0;
    private LinearLayoutManager mLayoutManager;
    Dialog dialog;
    LinearLayout llCancelDate,llStartDate,llExpiryDate,ll_dateContainer,ll_container,ll_planData;
    TextView tvCancelDateLabel,tvExpiryDateLabel,tvStartDateLabel,tvCancelDateValue,tvExpiryDateValue,tvStartDateValue,tvPlan,tv_planAmountLabel,tv_planAmountValue;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subscriptionBinding = DataBindingUtil.setContentView(this, R.layout.activity_extend_subscription);
        setContentView(R.layout.activity_extend_subscription);
         if (Util.isTabletDevice(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
//        rlContainer.setVisibility(View.GONE);
        ImageView imgBack= GenericView.findViewById(this,R.id.iv_imgMenu_Back);
        ImageView imgSettings= GenericView.findViewById(this,R.id.iv_imgSetting);
        TextView tvHeader= GenericView.findViewById(this,R.id.tv_txtTitle);

        tvCancelDateLabel= GenericView.findViewById(this,R.id.tv_cancelDateLabel);
        tvExpiryDateLabel= GenericView.findViewById(this,R.id.tv_expiryDateLabel);
        tvStartDateLabel= GenericView.findViewById(this,R.id.tv_startDateLabel);
        tv_planAmountLabel= GenericView.findViewById(this,R.id.tv_planAmountLabel);
        tv_planAmountValue= GenericView.findViewById(this,R.id.tv_planAmountValue);

        tvCancelDateValue= GenericView.findViewById(this,R.id.tv_cancelDateValue);
        tvExpiryDateValue= GenericView.findViewById(this,R.id.tv_expiryDateValue);
        tvStartDateValue= GenericView.findViewById(this,R.id.tv_startDateValue);
        buyButton= GenericView.findViewById(this,R.id.buyButton);
        llStartDate= GenericView.findViewById(this,R.id.ll_startDate);
        llCancelDate= GenericView.findViewById(this,R.id.ll_cancelDate);
        llExpiryDate= GenericView.findViewById(this,R.id.ll_expiryDate);
        ll_dateContainer= GenericView.findViewById(this,R.id.ll_dateContainer);
        ll_container= GenericView.findViewById(this,R.id.rl_container);
        tvPlan= GenericView.findViewById(this,R.id.tv_Plan);
        view1= GenericView.findViewById(this,R.id.view1);
        view2= GenericView.findViewById(this,R.id.view2);
        view3 = GenericView.findViewById(this,R.id.view3);
        view4 = GenericView.findViewById(this,R.id.view4);
        ll_planData= GenericView.findViewById(this,R.id.ll_planData);

        ll_container.setVisibility(View.GONE);
        
        tvHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        tvHeader.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_extend_subscription)));

        buyButton.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        tvCancelDateLabel.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        tvExpiryDateLabel.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        tvStartDateLabel.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        tvCancelDateValue.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        tvExpiryDateValue.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        tvStartDateValue.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        tvPlan.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        tv_planAmountLabel.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        tv_planAmountValue.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        tvStartDateLabel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_startdate)));
        tvExpiryDateLabel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_expirydate)));
        tvCancelDateLabel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_canceldate)));
        tv_planAmountLabel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_plan_amount)));

        parent=ExtendSubscriptionActivity.this;

        imgBack.setImageResource(R.mipmap.ic_back);
        imgSettings.setVisibility(View.INVISIBLE);
        tvHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));


//        BaseConstants.base64EncodedPublicKey ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA38gj8JZHOeWccbPuXY4dZ8JpFKTASkfg8ilVglcF2JggLsXn8HyVwqsvrm+L7oF9zht/hDh7+rC0AW0r5k0/1kI6tfrqldquDglOOS3c7wCuyEXBhkY1DCU9iWFg5t8oTB5cs5cXwAfu5ePL9gpzlFw/VWKW1AuvPCcEWFeNKdTD3yvuiQGAOMboAjHZbAQcrpe+Tw0ipFDTeMeHdTNpFg27RCedJq0jql7EYv51PHJnPy2WJHwDXzKS6fo2fLFxpwddEWI8Fnu7KB3nrPftKbi+GHT5DQruKc9u0gxybUjeVgGZGKgtVqp45rnbAkKtETnP0wH3nyXnhap86fZAtQIDAQAB";

        mHelper = new IabHelper(this,  BaseConstants.base64EncodedPublicKey);

        mHelper.startSetup(new
                                   IabHelper.OnIabSetupFinishedListener() {
                                       @SuppressLint("LongLogTag")
                                       public void onIabSetupFinished(IabResult result)
                                       {
                                           if (!result.isSuccess()) {
                                               Log.d(TAG, "In-app Billing setup failed: " +
                                                       result);
                                           } else {
                                               Log.d(TAG, "In-app Billing is set up OK");
                                           }
                                       }
                                   });
        showProgressBar(this);
        PlanData.getSubscriptionPlans(parent,pageNo,pagesize,dataObserver);

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


    public void buttonClicked (View view)
    {

    }

    public void buyClick(View view) {
        try
        {
            mHelper.launchSubscriptionPurchaseFlow(this, ITEM_SKU_PREMIUM, 10001,
                    mPurchaseFinishedListener, "mypurchasetoken1");
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }
    public void buyClickFailed(View view) {
        try {
            mHelper.launchPurchaseFlow(this, ITEM_SKU_PREMIUM, 10001,
                    mPurchaseFinishedListener, "mypurchasetoken1");
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data)
    {
        if (!mHelper.handleActivityResult(requestCode,
                resultCode, data)) {
//            LeadDetails.setMessage(ExtendSubscriptionActivity.this,"onActivityResult ");
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result,
                                          Purchase purchase)
        {

            if (result.isFailure()) {

//                LeadDetails.setMessage(ExtendSubscriptionActivity.this,"OnPurchasedListener Failed");
                return;
            }
            else if (purchase.getSku().equals(ITEM_SKU_PREMIUM)) {
                consumeItem();
//                LeadDetails.setMessage(ExtendSubscriptionActivity.this,"OnPurchasedListener Success");
                PlanData.updatePaymentStatus(ExtendSubscriptionActivity.this,planList.get(selectedPosition),dataObserver);
//                buyButton.setEnabled(false);
            }

        }
    };

    public void consumeItem() {
        try {
            mHelper.queryInventoryAsync(mReceivedInventoryListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            if (result.isFailure()) {
//                LeadDetails.setMessage(ExtendSubscriptionActivity.this,"QueryInventoryFinishedListener Failed");
                // Handle failure
//                ToastHelper.displayCustomToast("Failed to subscribe");
            } else {
//                LeadDetails.setMessage(ExtendSubscriptionActivity.this,"QueryInventoryFinishedListener Success");
//                    mHelper.consumeAsync(inventory.getPurchase(ITEM_SKU_PREMIUM),
//                            mConsumeFinishedListener);

            }
        }
    };


    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {

//                        PlanData.updatePaymentStatus(ExtendSubscriptionActivity.this,planList.get(selectedPosition),dataObserver);
                    }
                    else
                    {
//                        ToastHelper.displayCustomToast(MessageHelper.getInstance().getAppMessage(getString(R.string.str_fail_subscribe)));
                        // handle error
                    }
                }
            };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            try {
                mHelper.dispose();
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
        mHelper = null;
    }


    DataObserver dataObserver=new DataObserver() {
        @Override
        public void OnSuccess(RequestCode requestCode, Object object) {
            switch (requestCode) {
                case getSubscriptionPlans:

                    if(object!=null)
                    {
                        try
                        {
                            planList= (List<PlanData>) object;
                            planData = (PlanData) planList.get(0);

                            ll_container.setVisibility(View.VISIBLE);
                      if(planData.getSubscriptionStatus().equalsIgnoreCase(SubscriptionStatus.UNSUBSCRIBED.getType()))
                      {
                          llCancelDate.setVisibility(View.GONE);
                          llStartDate.setVisibility(View.GONE);
                          llExpiryDate.setVisibility(View.GONE);
                          ll_planData.setVisibility(View.VISIBLE);
                          tv_planAmountValue.setText("$"+planData.getPaymentAmount());
                          view1.setVisibility(View.GONE);
                          view2.setVisibility(View.GONE);
                          view3.setVisibility(View.GONE);
                          buyButton.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_subscribe_button)));
                          tvPlan.setText(planData.getPlanTitle());
                      }
                      else if(planData.getSubscriptionStatus().equalsIgnoreCase(SubscriptionStatus.SUBSCRIBED.getType()))
                      {
                          llCancelDate.setVisibility(View.GONE);
                          view3.setVisibility(View.GONE);
                          llStartDate.setVisibility(View.VISIBLE);
                          llExpiryDate.setVisibility(View.VISIBLE);
                          ll_planData.setVisibility(View.VISIBLE);
                          tv_planAmountValue.setText("$"+planData.getPaymentAmount());
                          tvStartDateValue.setText(planData.getPaymentDate());
                          tvExpiryDateValue.setText(planData.getExpDate());
                          buyButton.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel_subscription)));
                          tvPlan.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel_subscription_message)));

                      }
                      else if(planData.getSubscriptionStatus().equalsIgnoreCase(SubscriptionStatus.CANCELLED.getType()))
                      {
                          llCancelDate.setVisibility(View.VISIBLE);
                          llStartDate.setVisibility(View.VISIBLE);
                          llExpiryDate.setVisibility(View.VISIBLE);
                          ll_planData.setVisibility(View.VISIBLE);
                          tv_planAmountValue.setText("$"+planData.getPaymentAmount());
                          tvStartDateValue.setText(planData.getPaymentDate());
                          tvExpiryDateValue.setText(planData.getExpDate());
                          tvCancelDateValue.setText(planData.getCancelDate());

                          buyButton.setVisibility(View.GONE);
                          tvPlan.setText(planData.getPlanTitle());
                          tvPlan.setVisibility(View.GONE);
                      }
                        }
                        catch (Exception ex)
                        {
                            Debug.trace(ex.toString());
                        }

//                        bindPreferredList();
                    }
                    if (dialog.isShowing())
                        dialog.dismiss();
                    break;
                case cancelsubscription:
                    CustomDialog.getInstance().hide();
                    String response1 = (String) object;

                    try
                    {
                        JSONObject mJobjResponse = new JSONObject(response1);
                        String status = mJobjResponse.getString(ApiList.KEY_STATUS);
                        String message = mJobjResponse.getString(ApiList.KEY_MESSAGE);
//                        ToastHelper.displayCustomToast(message);

                        CustomDialog.getInstance().showAlertWithButtonClick(ExtendSubscriptionActivity.this,message
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        CustomDialog.getInstance().hide();
                                        PrefHelper.setBoolean("isnotification", false);
                                        PlanData.getSubscriptionPlans(parent,pageNo,pagesize,dataObserver);

                                    }
                                }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CustomDialog.getInstance().hide();
                                    }
                                }, false
                        );

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
                case updatePaymentStatus:
                    CustomDialog.getInstance().hide();

                    String response = (String) object;
                    PrefHelper.setInt(BaseConstants.PAYMENT_STATUS, 1);
                    try
                    {
                        JSONObject mJobjResponse = new JSONObject(response);

                        String status = mJobjResponse.getString(ApiList.KEY_STATUS);
                        String message = mJobjResponse.getString(ApiList.KEY_MESSAGE);

                        CustomDialog.getInstance().showAlertWithButtonClick(ExtendSubscriptionActivity.this,message
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name))
                                , MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view)
                                    {
                                        CustomDialog.getInstance().hide();
                                        PrefHelper.setBoolean("isnotification", false);

                                        finish();

                                    }
                                }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_dismiss))
                                ,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        CustomDialog.getInstance().hide();
                                    }
                                }, false
                        );




                        CustomDialog.getInstance().showAlertWithButtonClick(parent, message, MessageHelper.getInstance().getAppMessage(getString(R.string.app_name)), MessageHelper.getInstance().getAppMessage(getString(R.string.str_ok)), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomDialog.getInstance().hide();
                            }
                        }, MessageHelper.getInstance().getAppMessage(getString(R.string.str_cancel)), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomDialog.getInstance().hide();
                            }
                        },false);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    break;
            }
        }

        @Override
        public void OnFailure(RequestCode requestCode, String errorCode, String error) {
            CustomDialog.getInstance().hide();

            CustomDialog.getInstance().showAlert(ExtendSubscriptionActivity.this,errorCode,error, Util.getAppKeyValue(ExtendSubscriptionActivity.this, R.string.lblOk), Util.getAppKeyValue(ExtendSubscriptionActivity.this, R.string.str_dismiss),false);
            if(planList==null||planList.size()==0) {
//                setNoData(true);
            }
        }

        @Override
        public void onOtherStatus(RequestCode requestCode, Object object) {
            CustomDialog.getInstance().hide();

            if(planList==null||planList.size()==0) {
//                setNoData(true);
            }
        }

        @Override
        public void onRetryRequest(RequestCode requestCode) {
            CustomDialog.getInstance().hide();
        }
    };



    @Override
    public void onClickEvent(View view) {
        switch (view.getId())
        {
            case R.id.buyButton:
                if(planData.getSubscriptionStatus().equalsIgnoreCase(SubscriptionStatus.UNSUBSCRIBED.getType()))
                {
                    buyClick(view);
                }
                else if(planData.getSubscriptionStatus().equalsIgnoreCase(SubscriptionStatus.SUBSCRIBED.getType()))
                {
                    CustomDialog.getInstance().showAlertWithButtonClick(parent, Util.getAppKeyValue(parent, R.string.str_ask_cancel_subscription), MessageHelper.getInstance().getAppMessage(getString(R.string.str_app_name)), Util.getAppKeyValue(parent, R.string.lblOk), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            CustomDialog.getInstance().hide();
                            PlanData.cancelSubscription(ExtendSubscriptionActivity.this,dataObserver);



                        }
                    }, Util.getAppKeyValue(parent, R.string.str_dismiss), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            CustomDialog.getInstance().hide();
                        }
                    }, true);


                }
                break;
            case R.id.iv_imgMenu_Back:
                finish();
                break;
        }
    }
}
