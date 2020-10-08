package com.pinpoint.appointment.api;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.pinpoint.appointment.R;
import com.pinpoint.appointment.customviews.CustomDialog;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.utils.Debug;
import com.pinpoint.appointment.utils.Util;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class MultipartRequest extends AsyncTask<String, Integer, Long> {
    public static String REQUEST_GET = "GET";
    public static String REQUEST_POST = "POST";
    public static String REQUEST_POSTPAIR = "POSTPAIR";
    public static String REQUEST_POSTIMAGE = "POSTIMAGE";
    public static Gson gson;
    boolean isShowDialog = true;
    private boolean isDialogRequired = true;
    private String strResult = "";
    private List<NameValuePair> objValuePair = null;
    private int ApiId = 0;
    private RequestListener mListener;
    @SuppressLint("StaticFieldLeak")
    private Context mActivity;
    private RequestCode mRequestCode;
    private boolean isFinalRequest;

    public MultipartRequest(Context mContext, List<NameValuePair> objvaluepair, RequestCode requestCode, RequestListener listener, boolean isDialogRequired, boolean isFinalRequest) {
        this.mListener = listener;
        this.mActivity = mContext;
        this.mRequestCode = requestCode;
        this.objValuePair = objvaluepair;
        this.isDialogRequired = isDialogRequired;
        this.isFinalRequest = isFinalRequest;

        gson = new GsonBuilder().enableComplexMapKeySerialization().serializeNulls().setPrettyPrinting().create();
    }

    private static <T> void processSuccess(final String response, final ResponseStatus manage, final RequestCode requestCode, MultipartRequest.IListener listener) {
        if (listener != null) {
            Object object = null;
            if ((requestCode.getLocalClass() != null) && requestCode.getLocalClass().getSimpleName().equalsIgnoreCase("Manage")) {
                object = manage;
            } else {
                try {
                    object = ResponseManager.parse(requestCode, response, gson);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (object instanceof ResponseStatus) {
                listener.onOtherError(manage.getMessage(), manage.getStatus());
            } else {
                listener.onProcessCompleted(object);
            }
        }
    }

    private static void processError(final ResponseStatus manage, MultipartRequest.IListener listener) {
        if (listener != null) {
            listener.onOtherError(manage.getMessage(), manage.getStatus());
        }
    }

    int parseIntValue(String strValue) {
        int value = 0;
        if (strValue != null && strValue.length() > 0) {
            value = Integer.parseInt(strValue);
        }
        return value;
    }

    float parseFloatValue(String strValue) {
        float value = 0.0f;
        if (strValue != null && strValue.length() > 0) {
            value = Float.parseFloat(strValue);
        }
        return value;
    }

    @Override
    protected void onPreExecute() {
        CustomDialog.getInstance().showProgressBar(mActivity);
    }

    @Override
    protected Long doInBackground(String... values) {
        BufferedReader in = null;
        try {
            if (Util.checkInternetConnection()) {
                String URL_STR = getAbsoluteUrl(values[1]);
                if (values[0].equalsIgnoreCase(REQUEST_POSTPAIR)) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    Log.v("TAG", "URL: " + URL_STR);
                    Log.v("TAG", "Params: " + objValuePair.toString());

                    Log.v("TAG", "SERVER URL: " + URL_STR);
                    Log.v("TAG", "PARAMS: " + objValuePair.toString());

                    HttpClient client = new DefaultHttpClient();
                    client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "android");

                    HttpPost request = new HttpPost();

                    HttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 60000);
                    request.setParams(httpParams);
                    request.setEntity(new UrlEncodedFormEntity(objValuePair));
                    request.setURI(new URI(URL_STR));
                    HttpResponse response = client.execute(request);
                    in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    String NL = System.getProperty("line.separator");
                    while ((line = in.readLine()) != null) {
                        sb.append(line + NL);
                    }
                    in.close();
                    strResult = sb.toString();

                }
                else if (values[0].equalsIgnoreCase(REQUEST_POST)) {

                    JSONObject objParam = new JSONObject();

                    for (NameValuePair pair : objValuePair) {
                        objParam.put(pair.getName(), pair.getValue());
                    }

                    Log.v("param : ", objParam.toString());
                    Log.v("URL : ", URL_STR);
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    HttpClient client = new DefaultHttpClient();
                    client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "android");

                    HttpPost request = new HttpPost();

                    request.setHeader("Content-type", "application/json");

                    HttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 60000);
                    request.setParams(httpParams);

                    if (objParam.toString().length() > 0) {
                        StringEntity se = new StringEntity(objParam.toString());
                        se.setContentEncoding("UTF-8");
                        se.setContentType("application/json");
                        request.setEntity(se);
                    }
                    request.setURI(new URI(URL_STR));
                    HttpResponse response = client.execute(request);
                    in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    String NL = System.getProperty("line.separator");
                    while ((line = in.readLine()) != null) {
                        sb.append(line + NL);
                    }
                    in.close();
                    strResult = sb.toString();

                }
                else if (values[0].equalsIgnoreCase(REQUEST_GET)) {
                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);
                    Log.v("URL", URL_STR);
                    HttpClient client = new DefaultHttpClient();
                    client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "android");

                    HttpGet request = new HttpGet();
                    request.setHeader("Content-type", "application/json");

                    HttpParams httpParams = new BasicHttpParams();
                    HttpConnectionParams.setConnectionTimeout(httpParams, 60000); // in
                    // milliseconds
                    request.setParams(httpParams);
                    request.setURI(new URI(URL_STR));
                    HttpResponse response = client.execute(request);
                    in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                    StringBuffer sb = new StringBuffer("");
                    String line = "";

                    String NL = System.getProperty("line.separator");
                    while ((line = in.readLine()) != null) {
                        sb.append(line + NL);
                    }
                    in.close();
                    strResult = sb.toString();

                }
                else if (values[0].equalsIgnoreCase(REQUEST_POSTIMAGE)) {
                    /**
                     * Post Request with image. This type of request is used in
                     * Registration, EditProfile,Add Article...etc
                     **/

                    Log.i("TAG", "URL" + URL_STR);
                    Log.i("TAG", "param" + objValuePair.toString());

                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy);

                    HttpURLConnection conn = null;
                    DataOutputStream dos = null;
                    String lineEnd = "\r\n";
                    String twoHyphens = "--";
                    String boundary = "*****";
                    int bytesRead, bytesAvailable, bufferSize;
                    byte[] buffer;
                    int maxBufferSize = 1024 * 1024;

                    Log.i("TAG", "Value 2 : " + values[2]);
                    String filePath = values[2];
                    String fileName = "";

                    FileInputStream fileInputStream = null;
                    if (filePath.length() > 0) {
                        fileInputStream = new FileInputStream(filePath);
                        fileName = new File(filePath).getName();
                    }

                    URL url = new URL(URL_STR);
                    // Open a HTTP connection to the URL
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(60000);
                    conn.setDoInput(true); // Allow Inputs
                    conn.setDoOutput(true); // Allow Outputs
                    conn.setUseCaches(false); // Don't use a Cached Copy
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                    conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"image\";filename=\"" + fileName + "\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    if (fileInputStream != null) {
                        /** create a buffer of maximum size */
                        bytesAvailable = fileInputStream.available();

                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        buffer = new byte[bufferSize];

                        /** read file and write it into form... */
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                        while (bytesRead > 0) {
                            dos.write(buffer, 0, bufferSize);
                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                        }

                        /**
                         * send multipart form data necesssary after file
                         * data...
                         */
                        dos.writeBytes(lineEnd);
                    }

                    for (NameValuePair namepair : objValuePair) {
                        dos.writeBytes(twoHyphens + boundary + lineEnd);
                        dos.writeBytes("Content-Disposition: form-data; name=\"" + namepair.getName() + "\"" + lineEnd);
                        dos.writeBytes("Content-Type: text/plain" + lineEnd);
                        dos.writeBytes(lineEnd);
                        dos.writeBytes(namepair.getValue());
                        dos.writeBytes(lineEnd);
                    }

                    dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                    // Responses from the server (code and message)
//                    int serverResponseCode = conn.getResponseCode();
//                    String serverResponseMessage = conn.getResponseMessage();

                    InputStream is = new BufferedInputStream(conn.getInputStream());
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }

                    is.close();
                    strResult = sb.toString();
                }
            } else {
                strResult = MessageHelper.getInstance().getAppMessage(mActivity.getResources().getString(R.string.str_internet_availability));
            }
            return null;
        } catch (Exception e) {
            strResult = MessageHelper.getInstance().getAppMessage(mActivity.getResources().getString(R.string.str_internet_availability));
            e.printStackTrace();
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            } else {
                return null;
            }
        }
    }

    @Override
    protected void onPostExecute(Long result) {
        Debug.trace("Uploading Result" + strResult);

        verifyResponse(strResult, mRequestCode, new MultipartRequest.IListener() {
            @Override
            public void onOtherError(String errorMessage, String status) {
                if (mActivity != null) {

                    if (CustomDialog.getInstance().isDialogShowing()) {
                        CustomDialog.getInstance().hide();
                    }
                }
                if (mListener != null) {
                    mListener.onException(status, errorMessage, mRequestCode);
                }
            }

            @Override
            public void onHideProgressDialog() {
                if (isFinalRequest) {
                    CustomDialog.getInstance().hide();
                }
            }

            @Override
            public void onProcessCompleted(Object object) {
                Debug.trace("TAG", "onProcessCompleted" + mRequestCode.toString());
                CustomDialog.getInstance().hide();
//                if (isFinalRequest) {
//                    CustomDialog.getInstance().hide();
//                }
                if (mListener != null) {
                    mListener.onComplete(mRequestCode, object);
                }
            }
        });
    }

    private void verifyResponse(String response, RequestCode requestCode, IListener listener) {
        if (listener != null) {
            ResponseStatus responseStatus;
            try {
                JSONObject jResult = new JSONObject(response);
                if (jResult.has("response")) {
                    String strResult = jResult.getString("response");
                    jResult = new JSONObject(strResult);
                }
                responseStatus = gson.fromJson(new JSONObject(jResult.toString()).toString(), ResponseStatus.class);

                if (responseStatus.isFail()||responseStatus.isError()) {
                    processError(responseStatus, listener);
                } else {
                    if (responseStatus.isSuccess()) {
                        processSuccess(jResult.toString(), responseStatus, requestCode, listener);
                    }
                }
            } catch (JsonSyntaxException | JSONException e) {
                e.printStackTrace();
//                processError(responseStatus, listener);
            }
        }
    }

    private String getAbsoluteUrl(final String relativeUrl) {
        if (!relativeUrl.contains("maps.googleapis.com")) {
            return ServerConfig.SERVER_URL + relativeUrl;
        } else {
            return relativeUrl;
        }
    }

    private interface IListener {
        void onOtherError(String errorMessage, String status);

        void onHideProgressDialog();

        void onProcessCompleted(Object object);
    }
}