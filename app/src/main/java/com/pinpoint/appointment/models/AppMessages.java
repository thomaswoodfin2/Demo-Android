package com.pinpoint.appointment.models;

import android.app.Activity;

import com.android.volley.Request;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.interfaces.DataObserver;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by comp252 on 23-05-2017.
 */

public class AppMessages {
    public static HashMap<String, String> mMessageMap = new HashMap<>();

    private String messageKey;
    private String messageText;

    public String getKeyValue() {
        return messageKey;
    }

    public void setKeyValue(String keyValue) {
        this.messageKey = keyValue;
    }

    public String getMsgValue() {
        return messageText;
    }

    public void setMsgValue(String msgValue) {
        this.messageText = msgValue;
    }

    public static void getAppMessages(final Activity activity, final DataObserver dataObserver) {

        JSONObject param = new JSONObject();

        RestClient.getInstance().post(activity, Request.Method.GET, ApiList.APIs.getMessages.getUrl(), param,
                new RequestListener() {
                    @Override
                    public void onComplete(RequestCode requestCode, Object object) {
                        List<AppMessages> messageList = new ArrayList<>();
                        messageList.addAll((List<AppMessages>) object);

                        if (!messageList.isEmpty()) {
                            for (int i = 0; i < messageList.size(); i++) {
                                mMessageMap.put(messageList.get(i).getKeyValue(), messageList.get(i).getMsgValue());
                            }
                            MessageHelper.getInstance().saveAppMessages(mMessageMap);
                        }
                        dataObserver.OnSuccess(requestCode, object);
                    }

                    @Override
                    public void onException(String error, String status, RequestCode requestCode) {
                        dataObserver.OnFailure(requestCode, status, error);
                    }

                    @Override
                    public void onOtherStatus(RequestCode requestCode, Object object) {
                        dataObserver.onOtherStatus(requestCode, object);
                    }
                }, RequestCode.messages, false);
    }
}