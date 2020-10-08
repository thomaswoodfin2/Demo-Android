package com.pinpoint.appointment.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.pinpoint.appointment.BaseActivity;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.customviews.GenericView;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.interfaces.ClickEvent;
import com.pinpoint.appointment.utils.Util;


public class WebViewDisplayActivity extends BaseActivity implements ClickEvent {

    private WebView mWebViewFileDisplay;
    private TextView mTxtTitle;
    private ImageView mImgSetting, mImgBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_display);
        if (Util.isTabletDevice(this)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        mTxtTitle = GenericView.findViewById(this, R.id.tv_txtTitle);
        mImgSetting = GenericView.findViewById(this, R.id.iv_imgSetting);
        mImgBack = GenericView.findViewById(this, R.id.iv_imgMenu_Back);
        mTxtTitle.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));

        mImgSetting.setVisibility(View.GONE);
        mImgBack.setImageResource(R.mipmap.ic_back);

        Intent intent = getIntent();
        String url="",title="";
        if(intent.hasExtra(BaseConstants.KEY_ADDRESS)) {
             url = intent.getExtras().getString(BaseConstants.KEY_ADDRESS, "");
        }
        if(intent.hasExtra(BaseConstants.KEY_ADDRESS)) {
             title = intent.getExtras().getString(ApiList.KEY_NAME, "");
        }
        mTxtTitle.setText(title);
        mWebViewFileDisplay = GenericView.findViewById(this, R.id.wv_FileDisplay);
        mWebViewFileDisplay.getSettings().setJavaScriptEnabled(true);
        mWebViewFileDisplay.getSettings().setBuiltInZoomControls(true);
        mWebViewFileDisplay.setWebChromeClient(new WebChromeClient());
        mWebViewFileDisplay.setWebViewClient(new WebViewClient() {
            ProgressDialog progressDialog;

            @Override
            public void onLoadResource(WebView view, String url) {
                if (progressDialog == null) {
                    // in standard case YourActivity.this
                    progressDialog = new ProgressDialog(WebViewDisplayActivity.this);
                    progressDialog.setMessage(MessageHelper.getInstance().getAppMessage(getString(R.string.str_loading)));
                    progressDialog.show();
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                //mWebViewFileDisplay.loadUrl(request.getUrl().getPath());
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {

                try {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();

                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }

            }
        });
        mWebViewFileDisplay.loadUrl(url);
    }


    @Override
    public void onClickEvent(View view) {
        switch (view.getId()) {
            case R.id.iv_imgMenu_Back:
                onBackPressed();
                break;

        }
    }

    @Override
    public void onBackPressed() {
        if (mWebViewFileDisplay.canGoBack()){
            mWebViewFileDisplay.goBack();
        }else {
            super.onBackPressed();
        }
    }
}
