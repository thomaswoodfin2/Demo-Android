package com.pinpoint.appointment.models;


import android.app.Activity;

import com.android.volley.Request;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.MultipartRequest;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.utils.Debug;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PropertyData implements Serializable {

    private String id;
    private String name;
    private String email;
    private String address;
    private String date;
    private String propertyImage;

    public int getLeadCount() {
        return leadCount;
    }

    public void setLeadCount(int leadCount) {
        this.leadCount = leadCount;
    }

    private int leadCount;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPropertyImage() {
        return propertyImage;
    }

    public void setPropertyImage(String propertyImage) {
        this.propertyImage = propertyImage;
    }

    public static void getPropertyData(final Activity activity, int page, int limit, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
            param.put(ApiList.KEY_PAGENO, page);
            param.put(ApiList.KEY_LIMIT, limit);


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.propertylisting.getUrl(), param, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {

                    dataObserver.OnSuccess(requestCode, object);
                }

                @Override
                public void onException(String statusCode, String error, RequestCode requestCode) {
                    dataObserver.OnFailure(requestCode, statusCode, error);
                }

                @Override
                public void onOtherStatus(RequestCode requestCode, Object object) {
                    dataObserver.onOtherStatus(requestCode, object);
                }
            }, RequestCode.propertylisting, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addPropertty(final Activity activity, final PropertyData propertyData, final String fullAddress, final DataObserver dataObserver) {
        try {

            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            agentId,name,email,address,device_token,image

            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_NAME, LoginHelper.getInstance().getName()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_USERID, String.valueOf(LoginHelper.getInstance().getUserID())));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_EMAIL, LoginHelper.getInstance().getEmail()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_ADDRESS, propertyData.getAddress()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_FULLADDRESS, fullAddress));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_DEVICE_TOKEN, ""));
            Debug.trace(nameValuePairs.toString());
            MultipartRequest restClient = new MultipartRequest(activity, nameValuePairs, RequestCode.addproperty, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {
                    String response = String.valueOf(object);
                    dataObserver.OnSuccess(requestCode, object);
                }

                @Override
                public void onException(String statusCode, String error, RequestCode requestCode) {
                    dataObserver.OnFailure(requestCode, statusCode, error);
                }

                @Override
                public void onOtherStatus(RequestCode requestCode, Object object) {
                    dataObserver.onOtherStatus(requestCode, object);
                }
            }, true, true);
            if (propertyData.getPropertyImage() != null && !propertyData.getPropertyImage().equalsIgnoreCase("")) {
                restClient.execute(MultipartRequest.REQUEST_POSTIMAGE, ApiList.APIs.addproperty.getUrl(), propertyData.getPropertyImage());
            } else {
                restClient.execute(MultipartRequest.REQUEST_POSTPAIR, ApiList.APIs.addproperty.getUrl(), propertyData.getPropertyImage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void editProperty(final Activity activity, final PropertyData propertyData, final String fullAddress, final DataObserver dataObserver) {
        try {

            List<NameValuePair> nameValuePairs = new ArrayList<>();
//            agentId,name,email,address,device_token,image
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_ID, String.valueOf(propertyData.getId())));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_NAME, LoginHelper.getInstance().getName()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_EMAIL, LoginHelper.getInstance().getEmail()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_ADDRESS, propertyData.getAddress()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_FULLADDRESS, fullAddress));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_DEVICE_TOKEN, ""));
            Debug.trace(nameValuePairs.toString());
            MultipartRequest restClient = new MultipartRequest(activity, nameValuePairs, RequestCode.editproperty, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {
                    String response = String.valueOf(object);
                    dataObserver.OnSuccess(requestCode, object);
                }

                @Override
                public void onException(String statusCode, String error, RequestCode requestCode) {
                    dataObserver.OnFailure(requestCode, statusCode, error);
                }

                @Override
                public void onOtherStatus(RequestCode requestCode, Object object) {
                    dataObserver.onOtherStatus(requestCode, object);
                }
            }, true, true);
            if (propertyData.getPropertyImage() != null && !propertyData.getPropertyImage().equalsIgnoreCase("")) {
                restClient.execute(MultipartRequest.REQUEST_POSTIMAGE, ApiList.APIs.editproperty.getUrl(), propertyData.getPropertyImage());
            } else {
                restClient.execute(MultipartRequest.REQUEST_POSTPAIR, ApiList.APIs.editproperty.getUrl(), propertyData.getPropertyImage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteProperty(final Activity activity, int propertryID, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_ID, propertryID);


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.deleteproperty.getUrl(), param, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {

                    dataObserver.OnSuccess(requestCode, object);
                }

                @Override
                public void onException(String statusCode, String error, RequestCode requestCode) {
                    dataObserver.OnFailure(requestCode, statusCode, error);
                }

                @Override
                public void onOtherStatus(RequestCode requestCode, Object object) {
                    dataObserver.onOtherStatus(requestCode, object);
                }
            }, RequestCode.deleteproperty, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deletePropertyImage(final Activity activity, final PropertyData propertyData, final DataObserver dataObserver) {
        try {

            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_ID, propertyData.getId());
            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.deletepropertyimage.getUrl(), param, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {

                    dataObserver.OnSuccess(requestCode, object);
                }

                @Override
                public void onException(String statusCode, String error, RequestCode requestCode) {
                    dataObserver.OnFailure(requestCode, statusCode, error);
                }

                @Override
                public void onOtherStatus(RequestCode requestCode, Object object) {
                    dataObserver.onOtherStatus(requestCode, object);
                }
            }, RequestCode.deletepropertyimage, true);
        } catch (Exception ec) {
        }
    }

}
