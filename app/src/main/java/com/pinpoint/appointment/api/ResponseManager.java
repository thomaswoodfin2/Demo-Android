package com.pinpoint.appointment.api;

import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.utils.Debug;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

class ResponseManager {

    static <T> Object parse(RequestCode requestCode, String response, Gson gson) {
        Debug.trace("Response: " + response);
        Object object = null;
        try {
            final JSONObject jsonObject;
            jsonObject = new JSONObject(response).getJSONObject("result");

            switch (requestCode) {
                case checkVersion:

                    object = gson.fromJson(jsonObject.toString(), requestCode.getLocalClass());


                    PrefHelper.setString(PrefHelper.KEY_TERMS_URL, jsonObject.getJSONObject("cmsContentURL").optString
                            (ApiList.KEY_JSON_TERMS_URL));

                    PrefHelper.setString(PrefHelper.KEY_PRIVACY_POLICY, jsonObject.getJSONObject("cmsContentURL").optString
                            (ApiList.KEY_PRIVACY_POLICY));
               break;
                case messages:
                    PrefHelper.setString(PrefHelper.KEY_MESSAGE_UPDATE_DATE, new JSONObject(response).getString("messageUpdateDate"));
                    object = gson.fromJson(jsonObject.getJSONArray(ApiList.KEY_DATA).toString(), requestCode.getLocalClass());
                    object = Arrays.asList((T[]) object);
                    break;
                case loginCustomer:
                    BaseConstants.NOTIFICATION_COUNT= jsonObject.getInt(ApiList.KEY_BADGCOUNT);
                    PrefHelper.setInt(BaseConstants.COUNT,BaseConstants.NOTIFICATION_COUNT);
                    PrefHelper.setInt(BaseConstants.PAYMENT_STATUS, jsonObject.getInt(BaseConstants.PAYMENT_STATUS));
                        object= gson.fromJson(jsonObject.getJSONObject("userdata").toString(), requestCode.getLocalClass());
                        break;
                case signup:
                    object= gson.fromJson(jsonObject.getJSONObject(ApiList.KEY_DATA).toString(), requestCode.getLocalClass());
                    break;
                case CUSTOMER_REGISTRATION:
                    object = gson.fromJson(jsonObject.toString(), requestCode.getLocalClass());
                    break;
                case friendslist:
                    PrefHelper.setInt(PrefHelper.KEY_TOTAL_RECORDS, jsonObject.getInt(PrefHelper.KEY_TOTAL_FRIENDS));
                    object = gson.fromJson(jsonObject.getJSONArray(ApiList.KEY_DATA).toString(), requestCode.getLocalClass());
                    object = Arrays.asList((T[]) object);
                    break;
                case getSubscriptionPlans:
//                    PrefHelper.setInt(PrefHelper.KEY_TOTAL_RECORDS, jsonObject.getInt(PrefHelper.KEY_TOTAL_RECORDS));
                    object = gson.fromJson(jsonObject.getJSONArray(ApiList.KEY_DATA).toString(), requestCode.getLocalClass());
                    object = Arrays.asList((T[]) object);
                    break;
                case appointmentlist:
                    PrefHelper.setInt(PrefHelper.KEY_TOTAL_RECORDS, jsonObject.getInt(PrefHelper.KEY_TOTAL_APPOINTMENTS));
                    object = gson.fromJson(jsonObject.getJSONArray(ApiList.KEY_DATA).toString(), requestCode.getLocalClass());
                    object = Arrays.asList((T[]) object);
                    break;
                case getnotification:
//                PrefHelper.setInt(PrefHelper.KEY_TOTAL_RECORDS, jsonObject.getInt(PrefHelper.KEY_TOTAL_APPOINTMENTS));
                    BaseConstants.NOTIFICATION_COUNT=jsonObject.getInt(ApiList.KEY_BADGCOUNT);
                    PrefHelper.setInt(BaseConstants.COUNT,BaseConstants.NOTIFICATION_COUNT);
                object = gson.fromJson(jsonObject.getJSONArray(ApiList.KEY_DATA).toString(), requestCode.getLocalClass());
                object = Arrays.asList((T[]) object);
                break;
                case getleads:
//                PrefHelper.setInt(PrefHelper.KEY_TOTAL_RECORDS, jsonObject.getInt(PrefHelper.KEY_TOTAL_APPOINTMENTS));
                    object = gson.fromJson(jsonObject.getJSONArray(ApiList.KEY_DATA).toString(), requestCode.getLocalClass());
                    object = Arrays.asList((T[]) object);
                    break;
                case getuserinfo:
                    object = gson.fromJson(jsonObject.getJSONArray(ApiList.KEY_DATA).toString(), requestCode.getLocalClass());
                    object = Arrays.asList((T[]) object);
                    break;
                case panic:
//                PrefHelper.setInt(PrefHelper.KEY_TOTAL_RECORDS, jsonObject.getInt(PrefHelper.KEY_TOTAL_APPOINTMENTS));
                    object = gson.fromJson(jsonObject.getJSONArray(ApiList.KEY_DATA).toString(), requestCode.getLocalClass());
                    object = Arrays.asList((T[]) object);
                    break;
                case getfriendlocation:
                    object = gson.fromJson(jsonObject.getJSONArray(ApiList.KEY_DATA).toString(), requestCode.getLocalClass());
                    object = Arrays.asList((T[]) object);
                    break;

                case propertylisting:
                    object = gson.fromJson(jsonObject.getJSONArray(ApiList.KEY_DATA).toString(), requestCode.getLocalClass());
                    object = Arrays.asList((T[]) object);
                    break;

                default:
                    object = response;
                    break;
            }
        } catch (JSONException e) {
//            e.printStackTrace();
            object=response;
        }

        return object;
    }
}