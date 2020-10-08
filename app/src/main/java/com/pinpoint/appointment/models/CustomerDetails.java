package com.pinpoint.appointment.models;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import com.android.volley.Request;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.BuildConfig;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.MultipartRequest;
import com.pinpoint.appointment.api.RequestCode;
import com.pinpoint.appointment.api.RequestListener;
import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.api.ServerConfig;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.interfaces.DataObserver;
import com.pinpoint.appointment.utils.Constants;
import com.pinpoint.appointment.utils.Debug;
import com.google.gson.Gson;
import com.pinpoint.appointment.utils.Util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class CustomerDetails implements Serializable {

    private String userId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;
    private String name;
    private String email;
    private String image;
    private String profileImage;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getProfileimageurl() {
        return profileImage;
    }

    public void setProfileimageurl(String profileimageurl) {
        this.profileImage = profileimageurl;
    }

    public int getTotalfriendcount() {
        return totalFriendCount;
    }

    public void setTotalfriendcount(int totalfriendcount) {
        this.totalFriendCount = totalfriendcount;
    }

    public int getTotalAppointmentscount() {
        return totalAppointmentsCount;
    }

    public void setTotalAppointmentscount(int totalAppointmentscount) {
        this.totalAppointmentsCount = totalAppointmentscount;
    }

    private int totalFriendCount;
    private int totalAppointmentsCount;

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    private String contact;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private String password;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String imagePath;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDevice_type() {
        return deviceType;
    }

    public void setDevice_type(String device_type) {
        this.deviceType = device_type;
    }

    public String getDevice_token_client() {
        return deviceToken;
    }

    public void setDevice_token_client(String device_token_client) {
        this.deviceToken = device_token_client;
    }

    public String getDevice_hwid() {
        return deviceHWId;
    }

    public void setDevice_hwid(String device_hwid) {
        this.deviceHWId = device_hwid;
    }

    private String company;
    private String deviceType;
    private String deviceToken;
    private String deviceHWId;

    public String getUserid() {
        return userId;
    }

    public void setUserid(String userid) {
        this.userId = userid;
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


    private static CustomerDetails customerDetails;

    public static CustomerDetails getCustomerDetails() {
        return customerDetails;
    }

    public static void setCustomerDetails(CustomerDetails customerDetails) {
        CustomerDetails.customerDetails = customerDetails;
    }

    public static void saveLoginUserCredentials(CustomerDetails user) {
        PrefHelper.getInstance().setString(PrefHelper.KEY_CURRENT_LOGGED_IN_USER, new Gson().toJson(user));
    }

    public static void saveLoginUserCredentialsNew(String user) {
        PrefHelper.getInstance().setString(PrefHelper.KEY_CURRENT_LOGGED_IN_USER, user);
    }

    public static CustomerDetails getCurrentLoginUser() {
        return new Gson().fromJson(PrefHelper.getInstance().getString(PrefHelper.KEY_CURRENT_LOGGED_IN_USER, ""), CustomerDetails.class);
    }

    public static boolean isLoggedIn() {
        return !PrefHelper.getInstance().getString(PrefHelper.KEY_CURRENT_LOGGED_IN_USER, Constants.DEFAULT_BLANK_STRING).isEmpty();
    }

    public static void logoutUser() {
        PrefHelper.getInstance().setString(PrefHelper.KEY_CURRENT_LOGGED_IN_USER, Constants.DEFAULT_BLANK_STRING);
    }

    public static void chekEmailExist(final Activity activity, final DataObserver dataObserver, String emailId) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_EMAIL, emailId);
            param.put(ApiList.KEY_TYPE, 1);
            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.checkemailexist.getUrl(), param,
                    new RequestListener() {
                        @Override
                        public void onComplete(RequestCode requestCode, Object object) {
//                            setCheckVersionModel((CheckVersion) object);
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
                    }, RequestCode.checkemailexist, true);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void updatePhone(final Activity activity, final DataObserver dataObserver, String contact) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
            param.put(ApiList.KEY_CONTACT, contact);
            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.updatephone.getUrl(), param,
                    new RequestListener() {
                        @Override
                        public void onComplete(RequestCode requestCode, Object object) {
//                            setCheckVersionModel((CheckVersion) object);
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
                    }, RequestCode.updatephone, true);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void userLogin(final Activity activity, String emailID, String password, int againLogin, final DataObserver dataObserver) {
        try {
            TimeZone timezone = TimeZone.getDefault();
            String timezoneName = timezone.getID();
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_EMAIL, emailID);
            param.put(ApiList.KEY_PASSWORD, password);
            param.put(ApiList.KEY_DEVICETYPE, BaseConstants.DEVICE_TYPE);
            param.put(ApiList.KEY_DEVICE_TOKEN, PrefHelper.getString(PrefHelper.KEY_DEVICE_TOKEN, ""));
            param.put(ApiList.KEY_APP_VERSION, BuildConfig.VERSION_NAME);
            param.put(ApiList.KEY_APPLICATION_TYPE, BaseConstants.APP_TYPE);
            param.put(ApiList.KEY_AGAIN_LOGIN, againLogin);
            param.put(ApiList.KEY_DEVICE_DETAILS,
                    "Android V:" + Util.getAppVersionName()
                            + ", OS :" + Build.VERSION.RELEASE
                            + ", Model:" + Build.MODEL
                            + ", Brand:" + Build.BRAND
                            + ", Device:" + Build.DEVICE
                            + ", Id:" + Build.ID
                            + ", Product:" + Build.PRODUCT
                            + ", SDK:" + Build.VERSION.SDK_INT
                            + ", Release:" + Build.VERSION.RELEASE);
            param.put(ApiList.KEY_TIMEZONE, timezoneName);


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.loginCustomer.getUrl(), param, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {
                    LoginHelper.getInstance().doLogin((CustomerDetails) object);
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
            }, RequestCode.loginCustomer, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void addUser(final Activity activity, final CustomerDetails customerDetails, String filepath, final boolean isAdd, final DataObserver dataObserver) {
        try {
//            JSONObject param = new JSONObject();
//            param.put(ApiList.KEY_USERID, LoginHelper.getInstance().getUserId());
//            param.put(ApiList.KEY_TRUCK_ID, truckid);
            List<NameValuePair> nameValuePairs = new ArrayList<>();

///storage/emulated/0/Codebase/Images/IMG_1526656238891.jpg
            TimeZone timezone = TimeZone.getDefault();
            String timezoneName = timezone.getID();
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_NAME, customerDetails.getName()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_USERID, "" + customerDetails.getUserid()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_EMAIL, customerDetails.getEmail()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_PASSWORD, customerDetails.getPassword()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_CONTACT, customerDetails.getContact()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_COMPANY, customerDetails.getCompany()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_DEVICE_TYPE, String.valueOf("2")));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_DEVICE_TOKEN_CLIENT, PrefHelper.getString(PrefHelper.KEY_DEVICE_TOKEN, "")));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_DEVICE_HWID, String.valueOf("")));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_TIMEZONE, timezoneName));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_DEVICE_DETAILS, "Android V:" + Util.getAppVersionName()
                    + ", OS :" + Build.VERSION.RELEASE
                    + ", Model:" + Build.MODEL
                    + ", Brand:" + Build.BRAND
                    + ", Device:" + Build.DEVICE
                    + ", Product:" + Build.PRODUCT
                    + ", SDK:" + Build.VERSION.SDK_INT
                    + ", Release:" + Build.VERSION.RELEASE));
//            if (isAdd) {
//                nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_DEVICE_TOKEN, String.valueOf(PrefHelper.getString(PrefHelper.KEY_DEVICE_TOKEN, ""))));
//                nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_DEVICETYPE, String.valueOf(BaseConstant.DEVICE_TYPE)));
//                nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_APP_VERSION, String.valueOf(BaseConstant.DEVICE_TYPE)));
//                nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_DEVICE_DETAILS,
//                        "Android V:" + Utils.getInstance().getAppVersionName()
//                                + ", OS :" + Build.VERSION.RELEASE
//                                + ", Model:" + Build.MODEL
//                                + ", Brand:" + Build.BRAND
//                                + ", Device:" + Build.DEVICE
//                                + ", Id:" + Build.ID
//                                + ", Product:" + Build.PRODUCT
//                                + ", SDK:" + Build.VERSION.SDK_INT
//                                + ", Release:" + Build.VERSION.RELEASE));
//            }
            Debug.trace(nameValuePairs.toString());
            MultipartRequest restClient = new MultipartRequest(activity, nameValuePairs, RequestCode.signup, new RequestListener() {
                @Override
                public void onComplete(RequestCode requestCode, Object object) {
                    String response = String.valueOf(object);
                    JSONObject mJobjResponse = null;
                    try {
                        LoginHelper.getInstance().doLogin((CustomerDetails) object);

                        mJobjResponse = new JSONObject(response);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

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
            }, false, true);
            if (customerDetails.getImagePath() != null && !customerDetails.getImagePath().equalsIgnoreCase("")) {
                restClient.execute(MultipartRequest.REQUEST_POSTIMAGE, ApiList.APIs.signup.getUrl(), customerDetails.getImagePath());
            } else {
                restClient.execute(MultipartRequest.REQUEST_POSTPAIR, ApiList.APIs.signup.getUrl(), customerDetails.getImagePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateUser(final Activity activity, final CustomerDetails customerDetails, final DataObserver dataObserver) {
        try {

            List<NameValuePair> nameValuePairs = new ArrayList<>();

            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_NAME, customerDetails.getName()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_USERID, "" + LoginHelper.getInstance().getUserID()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_EMAIL, customerDetails.getEmail()));
            nameValuePairs.add(new BasicNameValuePair(ApiList.KEY_COMPANY, customerDetails.getCompany()));

            MultipartRequest restClient = new MultipartRequest(activity, nameValuePairs, RequestCode.updateuserinfo, new RequestListener() {
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
            if (customerDetails.getImagePath() != null && !customerDetails.getImagePath().equalsIgnoreCase("")) {
                restClient.execute(MultipartRequest.REQUEST_POSTIMAGE, ApiList.APIs.updateuserinfo.getUrl(), customerDetails.getImagePath());
            } else {
                restClient.execute(MultipartRequest.REQUEST_POSTPAIR, ApiList.APIs.updateuserinfo.getUrl(), customerDetails.getImagePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getUserInfo(final Activity activity, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.getuserinfo.getUrl(), param,
                    new RequestListener() {
                        @Override
                        public void onComplete(RequestCode requestCode, Object object) {
//                            setCheckVersionModel((CheckVersion) object);
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
                    }, RequestCode.getuserinfo, true);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void updatePassword(final Activity activity, final DataObserver dataObserver, String oldPassword, String newPassword) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
            param.put(ApiList.KEY_OLD_PASSWORD, oldPassword);
            param.put(ApiList.KEY_NEW_PASSWORD, newPassword);


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.updatepassword.getUrl(), param,
                    new RequestListener() {
                        @Override
                        public void onComplete(RequestCode requestCode, Object object) {
//                            setCheckVersionModel((CheckVersion) object);
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
                    }, RequestCode.updatepassword, true);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void forgottPassword(final Activity activity, final DataObserver dataObserver, String email) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_EMAIL, email);


            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.forgotpassword.getUrl(), param,
                    new RequestListener() {
                        @Override
                        public void onComplete(RequestCode requestCode, Object object) {
//                            setCheckVersionModel((CheckVersion) object);
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
                    }, RequestCode.forgotpassword, true);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void verifyPassword(final Activity activity, final DataObserver dataObserver, String OTP, String password) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_VERIFICATION_CODE, OTP);
            param.put(ApiList.KEY_NEW_PASSWORD, password);

            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.verifypassword.getUrl(), param,
                    new RequestListener() {
                        @Override
                        public void onComplete(RequestCode requestCode, Object object) {
//                            setCheckVersionModel((CheckVersion) object);
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
                    }, RequestCode.verifypassword, true);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void callLogoutUser(final Activity activity, final DataObserver dataObserver) {
        try {
            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, Integer.parseInt(LoginHelper.getInstance().getUserID()));
            param.put(ApiList.KEY_DEVICE_TOKEN, PrefHelper.getString(PrefHelper.KEY_DEVICE_TOKEN, ""));
            param.put(ApiList.KEY_DEVICETYPE, BaseConstants.DEVICE_TYPE);
            param.put(ApiList.KEY_DEVICE_DETAILS, Util.getDeviceDetails());

            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.logoutCustomer.getUrl(), param,
                    new RequestListener() {
                        @Override
                        public void onComplete(RequestCode requestCode, Object object) {
//                            setCheckVersionModel((CheckVersion) object);
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
                    }, RequestCode.logoutCustomer, true);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void deleteProfileImage(final Activity activity, final CustomerDetails propertyData, final DataObserver dataObserver) {
        try {

            JSONObject param = new JSONObject();
            param.put(ApiList.KEY_USERID, propertyData.getId());
            RestClient.getInstance().post(activity, Request.Method.POST, ApiList.APIs.deleteprofileimage.getUrl(), param, new RequestListener() {
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
            }, RequestCode.deleteprofileimage, true);
        } catch (Exception ec) {
        }
    }

//    public void callLoginAPI(Context context, final DataObserver dataObserver, String strEmail, String strPassword) {
//
//        JSONObject param = new JSONObject();
//
//        try {
//            /*param.put(ApiList.KEY_EMAIL_ID, strEmail);
//            param.put(ApiList.KEY_PASSWORD, strPassword);
//            param.put(ApiList.KEY_DEVICE_TOKEN, PrefHelper.getInstance().getString(PrefHelper.KEY_DEVICE_TOKEN, Constants.DEFAULT_BLANK_STRING));
//            param.put(ApiList.KEY_APP_TYPE, Constants.DEFAULT_VALUE_DEVICE_TYPE_ANDROID);*/
//            String UNIQUE_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
//            String LoginURL = ServerConfig.SERVER_BASE_URL + "index.php/common_login?email=" + strEmail
//                    + "&password=" + strPassword + "&device_token=" + PrefHelper.getInstance().getString("deviceToken","fPk1yWMw_mg:APA91bGMymq14NeFASLW9TxhUWH_BKa1JPTEtFmogqrqAQLRPn_Bx-PPtKisID7aTy8lssINGyEUuRjLWzQwi9851Vu9oLeRHjZDWF_i6qC1E34jaCqcWHKJ24nfGZ3ZkyljrK6n7V5l") + "&device_type=2" + "&hwid=" + UNIQUE_id;//
//            Debug.trace( param.toString());
//
//            RestClient.getInstance().get(context, Request.Method.POST, LoginURL, param,
//                    new RequestListener() {
//                        @Override
//                        public void onRequestComplete(RequestCode requestCode, Object object) {
//                            saveLoginUserCredentialsNew( object.toString());
//                            dataObserver.OnSuccess(requestCode,object);
//                        }
//
//                        @Override
//                        public void onRequestError(String error, int status, RequestCode requestCode) {
//                            dataObserver.OnFailure(requestCode, error);
//                        }
//                    }, RequestCode.CUSTOMER_LOGIN, true);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

//    public void callRegisterAPI(Context context, final DataObserver dataObserver, String name, String strEmail,
//                                String strPassword, String socialMediaID, int loginType) {
//
//        JSONObject param = new JSONObject();
//        try {
//            /*param.put(ApiList.KEY_FIRST_NAME, firstName);
//            param.put(ApiList.KEY_LAST_NAME, "");
//            param.put(ApiList.KEY_BIRTHDATE, birthDate);
//            param.put(ApiList.KEY_EMAIL_ID, strEmail);
//            param.put(ApiList.KEY_PASSWORD, strPassword);
//            param.put(ApiList.KEY_HEAR_ABOUT_US, strHearAboutUs);
//            param.put(ApiList.KEY_PHONE_NO, strMobileNo);
//            param.put(ApiList.KEY_ADDRESS, strAddress);
//            param.put(ApiList.KEY_LOGIN_TYPE, loginType);
//            param.put(ApiList.KEY_DEVICE_TOKEN, PrefHelper.getInstance().getString(PrefHelper.KEY_DEVICE_TOKEN, Constants.DEFAULT_BLANK_STRING));
//            param.put(ApiList.KEY_APP_TYPE, Constants.DEFAULT_VALUE_DEVICE_TYPE_ANDROID);
//            param.put(ApiList.KEY_SOCIAL_MEDIA_ID, socialMediaID);*/
//
//            Debug.trace(Constants.DEBUG_KEY_POST_PARAM, param.toString());
//
//            RestClient.getInstance().post(context, Request.Method.POST, ApiList.KEY_FUNCTION_REGISTER, param,
//                    new RequestListener() {
//                        @Override
//                        public void onRequestComplete(RequestCode requestCode, Object object) {
//                            saveLoginUserCredentials((CustomerDetails) object);
//                            dataObserver.OnSuccess(requestCode,  object);
//                        }
//
//                        @Override
//                        public void onRequestError(String error, int status, RequestCode requestCode) {
//                            dataObserver.OnFailure(requestCode, error);
//                        }
//                    }, RequestCode.CUSTOMER_REGISTRATION, true);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

}
