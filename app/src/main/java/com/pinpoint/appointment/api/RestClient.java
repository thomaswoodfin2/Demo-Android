package com.pinpoint.appointment.api;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.customviews.CustomOverlay;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.helper.PrefHelper;
import com.pinpoint.appointment.helper.ToastHelper;
import com.pinpoint.appointment.models.LeadDetails;
import com.pinpoint.appointment.models.PropertyData;
import com.pinpoint.appointment.utils.Constants;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.MyLifecycleHandler;
import com.pinpoint.appointment.utils.Util;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

import static com.pinpoint.appointment.api.ResponseStatus.STATUS_CRASH;
import static com.pinpoint.appointment.api.ResponseStatus.STATUS_ERROR;
import static com.pinpoint.appointment.api.ResponseStatus.STATUS_FAIL;

/**
 * This class send API requests
 * using volley network library and verify response.
 * <p><strong>1.</strong> API calling </p>
 * post request using {@link RestClient#post(Context, int, String, JSONObject, RequestListener, RequestCode, Boolean)}.
 * <p><strong>2.</strong> Get Response</p>
 * Volley network call, response will get and handle in this two Listener.
 * <ul>
 * <li>{@link Response.Listener#onResponse(Object)}</li>
 * <li>{@link Response.ErrorListener#onErrorResponse(VolleyError)}</li>
 * </ul>
 * <p><strong>3.</strong> <code> onResponse </code> </p>
 * Verify response using {@link RestClient#verifyResponse(String, RequestCode, IListener)}
 * <br> In this method, it will parse the response and check the response status as per our
 * <strong>API Structure.</strong></br>
 * <p><strong>3.1</strong> <code> responseStatus.isSuccess() </code> </p>
 * Again response Verified using {@link RestClient#processSuccess(String, ResponseStatus, RequestCode, IListener)}
 * and go to the next step for parsing.
 * <p><strong>3.1.1</strong> parse Response </code> </p>
 * it parse the response using {@link ResponseManager#parse(RequestCode, String, Gson)}
 * <p><strong>3.2</strong> <code> responseStatus.isFail() </code> </p>
 * Handle error relevant action using {@link RestClient#processError(ResponseStatus, IListener)}
 * <p><strong>4.</strong><code>onErrorResponse</code></p>
 * It checks the error type and call it's relevant method.
 *
 * @see <a href="http://stackoverflow.com/questions/24700582/handle-volley-error">Handle Volley error</a>
 */

public class RestClient {
    // variable declaration
    private static final String TAG = RestClient.class.getSimpleName();
    String urltoprint = "";
    public static final Gson gson;
    public static RestClient instance;

    /*
      get the instance of the Gson
      */
    static {
        gson = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls().setPrettyPrinting().create();
    }

    // constructor
    private RestClient() {
    }

    /**
     * This method return the instance of RestClient Class
     *
     * @return (RestClient) instance : it return RestClient instance
     */
    public static RestClient getInstance() {
        if (null == instance) {
            instance = new RestClient();
        }
        return instance;
    }


    public void postForService(final Context mContext, int requestType, String url, JSONObject params, final RequestListener responseHandler,
                               final RequestCode requestCode, final boolean isDialogRequired) {
        if (Util.checkInternetConnection()) {
            if (params == null) {
                Debug.trace("TAG: " + getAbsoluteUrl(url) + " No requestParams");
            } else {
                Debug.trace("TAG: " + getAbsoluteUrl(url) + " " + params.toString());
            }

            if (isDialogRequired) {
                if (!CustomDialog.getInstance().isDialogShowing()) {
                    CustomDialog.getInstance().showProgressBar(mContext);
                }
            }

            if (!url.contains("maps.googleapis.com")) {
                url = getAbsoluteUrl(url);
            }

            JsonObjectRequest request = new JsonObjectRequest(requestType, url, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Debug.trace("Response: " + requestCode + ": " + response.toString());

                    verifyResponse(response.toString(), requestCode, new IListener() {
                        @Override
                        public void onError(String errorMessage, String status) {
//                            if(errorMessage!=null) {
////                                ToastHelper.displayCustomToast(errorMessage);
//                            }
//                              ToastHelper.displayCustomToast(errorMessage);

                            if (errorMessage != null) {
                                if (errorMessage.contains("nknown") || errorMessage.contains("exception")) {
                                    responseHandler.onException(status, Util.getAppKeyValue(mContext, R.string.str_internet_availability), requestCode);
                                } else {
                                    responseHandler.onException(status, errorMessage, requestCode);
                                }
                            }
                            if (isDialogRequired) {
                                if (CustomDialog.getInstance().isDialogShowing()) {
                                    CustomDialog.getInstance().hide();
                                }
                            }
                        }

                        @Override
                        public void onProcessCompleted(Object object) throws IOException, ClassNotFoundException {
                            if (responseHandler != null) {
                                responseHandler.onComplete(requestCode, object);
                            }
                            if (CustomDialog.getInstance().isDialogShowing()) {
                                CustomDialog.getInstance().hide();
                            }
                        }

                        @Override
                        public void onOtherStatus(Object object) {
                            responseHandler.onOtherStatus(requestCode, object);
                            if (isDialogRequired) {
                                if (CustomDialog.getInstance().isDialogShowing()) {
                                    CustomDialog.getInstance().hide();
                                }
                            }
                        }
                    });
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    System.out.println("onErrorResponse " + ": " + requestCode + ", " + error);

                    error.printStackTrace();
                    if (isDialogRequired) {
                        if (CustomDialog.getInstance().isDialogShowing()) {
                            CustomDialog.getInstance().hide();
                        }
                    }
                    responseHandler.onException(STATUS_CRASH, error.getMessage(), requestCode);
                }
            }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put(ApiList.KEY_CONTENT, ApiList.KEY_CONTENT_TYPE);
//                    headers.put(ApiList.KEY_AUTHENTICATE, ServerConfig.AUTHENTICATE_VALUE);
                    String authKey = PrefHelper.getString(PrefHelper.KEY_AUTHENTICATIONKEY, "");
                    headers.put(ApiList.KEY_AUTHENTICATE, authKey.trim());
                    return headers;
                }
            };

            BaseApplication.getInstance().setRequestTimeout(request);
            BaseApplication.getInstance().addToRequestQueue(requestCode.name(), request);
        } else {
//            ToastHelper.displayCustomDialog(mContext, mContext.getResources().getString(R.string.str_internet_availability));
        }
    }

    public void post(final Activity mContext, int requestType, String url, JSONObject params, final RequestListener responseHandler,
                     final RequestCode requestCode, final boolean isDialogRequired) {
        if (Util.checkInternetConnection()) {
            urltoprint = getAbsoluteUrl(url);
            if (params == null) {
                Debug.trace("TAG: " + getAbsoluteUrl(url) + " No requestParams");
                urltoprint = getAbsoluteUrl(url);
            } else {
                Debug.trace("TAG: " + getAbsoluteUrl(url) + " " + params.toString());
                urltoprint = getAbsoluteUrl(url) + " " + params.toString();
            }

            if (isDialogRequired) {
                if (!CustomDialog.getInstance().isDialogShowing()) {
                    CustomDialog.getInstance().showProgressBar(mContext);
                }
            }

            if (!url.contains("maps.googleapis.com")) {
                url = getAbsoluteUrl(url);
            }

            JsonObjectRequest request = new JsonObjectRequest(requestType, url, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Debug.trace("Response:" + response.toString());

                    verifyResponse(response.toString(), requestCode, new IListener() {
                        @Override
                        public void onError(String errorMessage, String status) {
                            if (errorMessage != null) {
//                                ToastHelper.displayCustomToast(errorMessage);
                            }
                            if (CustomDialog.getInstance().isDialogShowing()) {
                                CustomDialog.getInstance().hide();
                            }

                            if (errorMessage != null) {
                                if (errorMessage.contains("nknown") || errorMessage.contains("exception")) {
                                    responseHandler.onException(status, Util.getAppKeyValue(mContext, R.string.str_internet_availability), requestCode);
                                } else {
                                    responseHandler.onException(status, errorMessage, requestCode);
                                }
                            } else {
                                responseHandler.onException(status, errorMessage, requestCode);
                            }
//                            if (isDialogRequired) {


//                            }
//
                        }

                        @Override
                        public void onProcessCompleted(Object object) throws IOException, ClassNotFoundException {
                            if (CustomDialog.getInstance().isDialogShowing()) {
                                CustomDialog.getInstance().hide();
                            }
                            if (responseHandler != null) {
                                responseHandler.onComplete(requestCode, object);
                            }
                        }

                        @Override
                        public void onOtherStatus(Object object) {

                            if (isDialogRequired) {
                                if (CustomDialog.getInstance().isDialogShowing()) {
                                    CustomDialog.getInstance().hide();
                                }
                            }
                            responseHandler.onOtherStatus(requestCode, object);
                        }
                    });
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    Debug.trace("onErrorResponse:" + error.getLocalizedMessage());

                    error.printStackTrace();
                    if (isDialogRequired) {
                        if (CustomDialog.getInstance().isDialogShowing()) {
                            if (requestCode != RequestCode.propertylisting)
                                CustomDialog.getInstance().hide();
                        }
                    }
                    try {
                        LeadDetails.setMessage(mContext, error.toString() + " API Crashed : URL : " + urltoprint + new Date());
                    } catch (Exception ex) {
                    }
                    checkFailureResponse(error, requestCode, mContext, isDialogRequired, responseHandler);
                }
            }
            ) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put(ApiList.KEY_CONTENT, ApiList.KEY_CONTENT_TYPE);

                    String authKey = "MF83OTBhYjM5YTVkZTYyZTNjZDBjMThiNzZjY2QwOWY3MQ==";
                    //headers.put(ApiList.KEY_AUTHENTICATE, authKey);
                    return headers;
                }
            };
            BaseApplication.getInstance().setRequestTimeout(request);
            BaseApplication.getInstance().addToRequestQueue(requestCode.name(), request);

        } else {
//
            CustomDialog.getInstance().hide();
            ToastHelper.displayCustomToast(MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.str_internet_availability)));
        }
    }

    private void checkFailureResponse(VolleyError volleyError, RequestCode requestCode, Context mContext, Boolean isDialogRequired, RequestListener responseHandler) {
        if (mContext != null) {
            if (isDialogRequired) {
                if (CustomDialog.getInstance().isDialogShowing()) {
                    if (requestCode != RequestCode.propertylisting)
                        CustomDialog.getInstance().hide();
                }
            }
        }

        String errorMessage = null;

        if (mContext != null) {

            if (volleyError instanceof TimeoutError) {

                errorMessage = MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.slow_connection));
                LeadDetails.setMessage(mContext, "TimeoutError  API Crashed : URL : " + urltoprint + new Date());
            } else if (volleyError instanceof NoConnectionError) {
//                errorMessage=MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.short_maintenance));
                errorMessage = "Internet connection is not available.";

                LeadDetails.setMessage(mContext, "NoConnectionError  API Crashed : URL : " + urltoprint + new Date());

                Log.d("TAG", "errorMessage:" + requestCode + errorMessage);

            } else if (volleyError instanceof NetworkError) {
//                errorMessage=MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.short_maintenance));
                errorMessage = "Internet connection is not available.";
                LeadDetails.setMessage(mContext, "NetworkError  API Crashed : URL : " + urltoprint + new Date());

                Log.d("TAG", "errorMessage:" + requestCode + errorMessage);

            } else if (volleyError instanceof ParseError) {
                errorMessage = MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.short_maintenance));
                LeadDetails.setMessage(mContext, "ParseError  API Crashed : URL : " + urltoprint + new Date());

                Log.d("TAG", "errorMessage:" + requestCode + errorMessage);

            } else if (volleyError instanceof ServerError) {
                errorMessage = MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.short_maintenance));
                LeadDetails.setMessage(mContext, "ServerError  API Crashed : URL : " + urltoprint + new Date());

                Log.d("TAG", "errorMessage:" + requestCode + errorMessage);

            } else {

                errorMessage = MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.request_problem));
                Log.d("TAG", "errorMessage:" + errorMessage);
            }
            try {
                if (!urltoprint.contains("getlocation")) {
                    if (!(requestCode == RequestCode.friendslist || requestCode == RequestCode.appointmentlist || requestCode == RequestCode.getnotification || requestCode == RequestCode.propertylisting)) {
                        CustomDialog.getInstance().hide();
                        CustomDialog.getInstance().showAlertWithButtonClick(mContext, errorMessage, MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.str_server_error)), MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.str_ok)), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomDialog.getInstance().hide();

                            }
                        }, MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.str_ok)), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomDialog.getInstance().hide();
                            }
                        }, false);
                    }
                }
            } catch (Exception ex) {

            }
        }
        responseHandler.onException(STATUS_CRASH, errorMessage, requestCode);

    }

    public void postString(final Activity mContext, int requestType, String url, JSONObject params, final RequestListener responseHandler,
                           final RequestCode requestCode, final boolean isDialogRequired) {
        if (Util.checkInternetConnection()) {
//            if (params == null) {
////                Debug.trace("TAG: " + getAbsoluteUrl(url) + " No requestParams");
//            } else {
////                Debug.trace("TAG: " + getAbsoluteUrl(url) + " " + params.toString());
//            }

            if (isDialogRequired) {
                if (!CustomDialog.getInstance().isDialogShowing()) {
                    CustomDialog.getInstance().showProgressBar(mContext);
                }
            }

            if (!url.contains("maps.googleapis.com")) {
                url = getAbsoluteUrl(url);
            }

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Toast.makeText(mContext, response, Toast.LENGTH_LONG).show();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();

                    params.put("versionCode", "1");
                    params.put("appVersion", "1.0");
                    params.put("deviceType", "2");
                    params.put("applicationType", "1");
                    params.put("userId", "0");
                    params.put("messageUpdateDate", "2018-05-15 14:12:04.283");
                    params.put("lastLoginTime", "");
                    Debug.trace("TAG: " + params.toString());
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put(ApiList.KEY_CONTENT, ApiList.KEY_CONTENT_TYPE);
//                    headers.put(ApiList.KEY_AUTHENTICATE, ServerConfig.AUTHENTICATE_VALUE);
                    String authKey = PrefHelper.getString(PrefHelper.KEY_AUTHENTICATIONKEY, "");
//                    headers.put(ApiList.KEY_AUTHENTICATE, authKey);
                    return headers;
                }
            };


            BaseApplication.getInstance().setRequestTimeout(stringRequest);
            BaseApplication.getInstance().addToRequestQueue(requestCode.name(), stringRequest);
        } else {
            try {
//                CustomDialog.getInstance().hide();
                CustomDialog.getInstance().showAlertWithButtonClick(mContext, MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.str_internet_availability)), MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.app_name)), MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.str_ok)), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomDialog.getInstance().hide();

                    }
                }, MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.str_ok)), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomDialog.getInstance().hide();
                    }
                }, false);
            } catch (Exception ex) {

            }
//                ToastHelper.displayCustomToast(MessageHelper.getInstance().getAppMessage(mContext.getResources().getString(R.string.str_internet_availability)));

        }
    }

    /**
     * This method gives the absolute url path
     *
     * @param relativeUrl (String) : API url from API list e.g. CommonWebService.asmx/registerCustomer
     * @return URL (String): complete url with server live url
     * e.g. http://192.168.192.90:5555/CommonWebService.asmx/registerCustomer
     */
    private String getAbsoluteUrl(String relativeUrl) {
        return ServerConfig.SERVER_URL + relativeUrl;
    }


    private void verifyResponse(String response, RequestCode requestCode, IListener listener) {
        if (listener != null) {
            System.out.println("verifyResponse " + requestCode + ", " + response);

            ResponseStatus responseStatus;
//            {"response":{"result":{"cmsContentURL":[],"badgcount":0,"isUpdateType":"2","logoutCustomer":0,"isMessageUpdate":0,"updateMessage":"Optional Update","url":"http:\/\/abc.com\/asb","currency":0},"status":"Success","message":""}}
            try {
                JSONObject jResult = new JSONObject(response);
                if (jResult.has(ApiList.KEY_RESPONSE)) {
                    String strResult = jResult.getString(ApiList.KEY_RESPONSE);
                    jResult = new JSONObject(strResult);
                }
                responseStatus = gson.fromJson(new JSONObject(jResult.toString()).toString(), ResponseStatus.class);

                if (responseStatus.isFail() || responseStatus.isError()) {
                    processError(responseStatus, listener);
                } else if (responseStatus.isOtherError()) {
                    processOtherError(jResult.toString(), listener);
                } else {
                    if (responseStatus.isSuccess()) {
                        processSuccess(jResult.toString(), responseStatus, requestCode, listener);
                    }
                }
            } catch (JsonSyntaxException | JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private static void processOtherError(final String response, final IListener listener) {
        if (listener != null) {
            listener.onOtherStatus(response);
        }
    }

    private static void processError(final ResponseStatus responseStatus, final IListener listener) {
        if (listener != null) {
            listener.onError(responseStatus.getMessage(), responseStatus.getStatus());
        }
    }

    private static <T> void processSuccess(final String response, final ResponseStatus responseStatus, final RequestCode requestCode, final IListener listener) throws IOException, ClassNotFoundException {

        if (listener != null) {
            Object object = null;
            if ((requestCode.getLocalClass() != null) && requestCode.getLocalClass().getSimpleName().equalsIgnoreCase("ResponseStatus")) {
                object = responseStatus;
            } else {
                try {
                    object = ResponseManager.parse(requestCode, response, gson);
                } catch (Exception ex) {
                }
            }
            if (object instanceof ResponseStatus) {
                listener.onError(responseStatus.getMessage(), responseStatus.getStatus());
            } else {
                listener.onProcessCompleted(object);
            }
        }
    }

    /**
     * interface for handling the
     * other errors, hiding process dialog
     * and process further the response
     **/
//    private interface IListener {
//        /**
//         * <pre>{@code
//         * status = 1  // response status success
//         * status = 2  // response status fail
//         * status = 3  // response status error
//         * status = 4  // response status invalid
//         * }
//         * </pre>
//         *
//         * @param errorMessage (String) : appropriate message relevant to errors
//         * @param status       (String)       : status of response
//         */
//        void onOtherError(String errorMessage, int status);
//
//        /**
//         * This method hide progress dialog
//         */
//        void onHideProgressDialog();
//
//        /**
//         * @param object (Object) : succeed response in object
//         */
//        void onProcessCompleted(Object object);
//    }

    /**
     * interface for handling the
     * other errors, hiding process dialog
     * and process further the response
     **/
    public interface IListener {
        /**
         * <pre>{@code
         * status = 1  // response status success
         * status = 0  // response status fail
         * }
         * </pre>
         *
         * @param errorMessage (String) : appropriate message relevant to errors
         * @param status       (String) : status of response
         */
        void onError(String errorMessage, String status);

        /**
         * @param object (Object) : succeed response in object
         */
        void onProcessCompleted(Object object) throws IOException, ClassNotFoundException;

        void onOtherStatus(Object object);
    }


    public Single<Object> post(String endpoint, JSONObject params, RequestCode requestCode, @Nullable RequestQueue queue) {
        return Single.create(emitter -> {
                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, endpoint, params, response -> verifyResponse(response.toString(), requestCode, new IListener() {
                        @Override
                        public void onError(String errorMessage, String status) {
                            emitter.onError(new Throwable(new Exception(errorMessage)));
                        }

                        @Override
                        public void onProcessCompleted(Object object) throws IOException, ClassNotFoundException {
                            emitter.onSuccess((object));
                        }

                        @Override
                        public void onOtherStatus(Object object) {

                        }
                    }), emitter::onError

                    );

                    if(queue != null){

                        int requestTimeout = Constants.REQUEST_TIMEOUT;
                        request.setRetryPolicy(new DefaultRetryPolicy(requestTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                        queue.add(request);

                    }else {
                        BaseApplication.getInstance().setRequestTimeout(request);
                        BaseApplication.getInstance().addToRequestQueue(requestCode.name(), request);
                    }
                });


    }

    /*
    public Single<List<Post>> getPosts() {
        return Single.create(new SingleOnSubscribe<List<Post>>() {
            @Override
            public void subscribe(@NonNull final SingleEmitter<List<Post>> e) throws Exception {
                JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, ENDPOINT_GET_POSTS, null,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                if (response != null) {
                                    ArrayList<Post> result = new ArrayList<>();

                                    try {
                                        for (int i = 0; i < response.length(); i++) {
                                            result.add(getPost(response.getJSONObject(i)));
                                        }
                                    } catch (JSONException ex) {
                                        e.onError(ex);
                                    }

                                    e.onSuccess(result);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                e.onError(error);
                            }
                        }
                );

                VolleyDispatcher.getInstance().addToQueue(jsonObjectRequest);
            }
        });
    }
    */
}