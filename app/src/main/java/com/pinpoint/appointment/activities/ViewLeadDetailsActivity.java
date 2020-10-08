package com.pinpoint.appointment.activities;


import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinpoint.appointment.BaseActivity;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.databinding.ActivityLeadDetailsBinding;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.models.LeadDetails;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class ViewLeadDetailsActivity extends BaseActivity implements ClickEvent{


    private ViewLeadDetailsActivity parent;

    ActivityLeadDetailsBinding activityLeadDetailsBinding;
    LeadDetails leadDetails;


    public ViewLeadDetailsActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityLeadDetailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_lead_details);
        ImageView imgBack= GenericView.findViewById(this,R.id.iv_imgMenu_Back);
        ImageView imgSettings= GenericView.findViewById(this,R.id.iv_imgSetting);
        TextView tvHeader= GenericView.findViewById(this,R.id.tv_txtTitle);
        tvHeader.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_lead_details)));
        parent=ViewLeadDetailsActivity.this;

        imgBack.setImageResource(R.mipmap.ic_back);
        imgSettings.setVisibility(View.INVISIBLE);
        tvHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));

        leadDetails= (LeadDetails) getIntent().getSerializableExtra(BaseConstants.KEY_OBJECT);

        activityLeadDetailsBinding.tvCompanyLabel.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        activityLeadDetailsBinding.tvCompanyValue.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        activityLeadDetailsBinding.tvPhoneLabel.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        activityLeadDetailsBinding.tvPhoneValue.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        activityLeadDetailsBinding.tvEmailLabel.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        activityLeadDetailsBinding.tvEmailValue.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        activityLeadDetailsBinding.tvTime.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        activityLeadDetailsBinding.tvTimeValue.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        activityLeadDetailsBinding.tvTimeFrameLabel.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        activityLeadDetailsBinding.tvTimeFrameValue.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        activityLeadDetailsBinding.tvNameLabel.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        activityLeadDetailsBinding.tvNameValue.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        activityLeadDetailsBinding.tvCompanyLabel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_company)));
        activityLeadDetailsBinding.tvNameLabel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_name1)));
        activityLeadDetailsBinding.tvPhoneLabel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_phone_number)));
        activityLeadDetailsBinding.tvEmailLabel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_email1)));
        activityLeadDetailsBinding.tvTime.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_lead_time)));
        activityLeadDetailsBinding.tvTimeFrameLabel.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_buying_timeframe)));


        if(leadDetails!=null)
        {
            activityLeadDetailsBinding.tvNameValue.setText(leadDetails.getName());
            activityLeadDetailsBinding.tvPhoneValue.setText(leadDetails.getContact());
            activityLeadDetailsBinding.tvEmailValue.setText(leadDetails.getEmail());
            activityLeadDetailsBinding.tvTimeValue.setText(leadDetails.getDate());
            if(leadDetails.getCompany()!=null&&!leadDetails.getCompany().equalsIgnoreCase(""))
            {
                activityLeadDetailsBinding.llCompany.setVisibility(View.VISIBLE);
                activityLeadDetailsBinding.tvCompanyLabel.setVisibility(View.VISIBLE);
                activityLeadDetailsBinding.tvCompanyValue.setVisibility(View.VISIBLE);
                activityLeadDetailsBinding.view5.setVisibility(View.VISIBLE);

                activityLeadDetailsBinding.tvCompanyValue.setText(leadDetails.getCompany());
            }
//            1=0-3, 2=3-6, 3=6+ aa rite moklavjo
            try {
                switch (Integer.parseInt(leadDetails.getDuration())) {
                    case 1:
                        activityLeadDetailsBinding.tvTimeFrameValue.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_03months)));
                        break;
                    case 2:
                        activityLeadDetailsBinding.tvTimeFrameValue.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_36months)));
                        break;
                    case 3:
                        activityLeadDetailsBinding.tvTimeFrameValue.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_6months)));
                        break;
                }
            }catch (Exception ex)
            {}

        }
        Picasso.get().load(leadDetails.getProfileImage()).placeholder(R.mipmap.app_icon)
                .into(activityLeadDetailsBinding.imagess);
//        activityLeadDetailsBinding.tvFriends.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));


//        viewProfileBinding..setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));





//        viewProfileBinding.tvAppointment.setText(MessageHelper.getInstance().getAppMessage(getString(R.string.str_appointments)));



    }


    @Override
    public void onClickEvent(View view) {
       switch (view.getId()) {
           case R.id.iv_imgMenu_Back:
               finish();
               break;
       }
    }
}
