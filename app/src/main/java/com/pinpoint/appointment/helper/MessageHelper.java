package com.pinpoint.appointment.helper;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created on 18-07-2017.
 */

public class MessageHelper {
    private static MessageHelper instance;

    private MessageHelper() {
    }

    public static MessageHelper getInstance() {
        if (instance == null) {
            instance = new MessageHelper();
        }
        return instance;
    }

    public String loadJSONFromAsset(Context mContext, String fileName) {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void saveAppMessages(HashMap<String, String> mMessageMap) {
        PrefHelper.setHashMap(PrefHelper.KEY_MESSAGE, mMessageMap);
    }

    private HashMap<String, String> getAppMessages() {
        return PrefHelper.getHashMap(PrefHelper.KEY_MESSAGE);
    }

    public String getAppMessage(String messageKey) {
        Map<String, String> appMessages = getAppMessages();
        return appMessages.get(messageKey);
    }
}