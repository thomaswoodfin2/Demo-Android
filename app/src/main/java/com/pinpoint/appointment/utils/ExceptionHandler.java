package com.pinpoint.appointment.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;

import com.pinpoint.appointment.R;
import com.pinpoint.appointment.activities.AddLeadsActivity;
import com.pinpoint.appointment.activities.SplashActivity;
import com.pinpoint.appointment.api.ApiList;
import com.pinpoint.appointment.api.ServerConfig;
import com.pinpoint.appointment.models.LeadDetails;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private final Activity mContext;
    private String LINE_SEPARATOR = "\n";
    private String response;

    public ExceptionHandler(Activity context) {
        mContext = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception)
    {

//        StringWriter stackTrace = new StringWriter();
//        exception.printStackTrace(new PrintWriter(stackTrace));
//        Calendar cal = Calendar.getInstance();
//
//        StringBuilder errorReport = new StringBuilder();
//        errorReport.append("***** LOCAL CAUSE OF ERROR (" + mContext.getString(R.string.app_name) + ") Version: " + Util.getAppVersionCode() + " Date: " + cal.getTime() + " *****\n\n");
//        errorReport.append("Localized Error Message: ");
//        errorReport.append(exception.getLocalizedMessage());
//        errorReport.append("Error Message: ");
//        errorReport.append(exception.getMessage());
//        errorReport.append("StackTrace");
//        errorReport.append(stackTrace.toString());
//
//        errorReport.append("\n" + "*******************************\n");
//        errorReport.append(response);
//        errorReport.append("\n" + "*******************************\n");
//
//        errorReport.append("\n************ DEVICE INFORMATION ***********\n");
//        errorReport.append("Brand: ");
//        errorReport.append(Build.BRAND);
//        errorReport.append(LINE_SEPARATOR);
//        errorReport.append("Device: ");
//        errorReport.append(Build.DEVICE);
//        errorReport.append(LINE_SEPARATOR);
//        errorReport.append("Model: ");
//        errorReport.append(Build.MODEL);
//        errorReport.append(LINE_SEPARATOR);
//        errorReport.append("Id: ");
//        errorReport.append(Build.ID);
//        errorReport.append(LINE_SEPARATOR);
//        errorReport.append("Product: ");
//        errorReport.append(Build.PRODUCT);
//        errorReport.append(LINE_SEPARATOR);
//        errorReport.append("\n************ FIRMWARE ************\n");
//        errorReport.append("SDK: ");
//        errorReport.append(Build.VERSION.SDK);
//        errorReport.append(LINE_SEPARATOR);
//        errorReport.append("Release: ");
//        errorReport.append(Build.VERSION.RELEASE);
//        errorReport.append(LINE_SEPARATOR);
//        errorReport.append("Incremental: ");
//        errorReport.append(Build.VERSION.INCREMENTAL);
//        errorReport.append(LINE_SEPARATOR);

        Intent intent = new Intent(mContext, SplashActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                mContext, 0, intent, intent.getFlags());

        //Following code will restart your application after 2 seconds
        AlarmManager mgr = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                pendingIntent);

        //This will finish your activity manually
        mContext.finish();

        //This will stop your application and take out from it.
        System.exit(2);
//        LeadDetails.setMessage(mContext,errorReport.toString());
//
//        try {
//            URL url;
//            try {
//                url = new URL(ServerConfig.SERVER_URL + ApiList.KEY_FUNCTION_CRASH_REPORT);
//                Debug.trace("URL: " + url);
//
//                JSONObject objParam = new JSONObject();
//                objParam.put(ApiList.KEY_ERROR_TEXT, errorReport.toString());
//
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setReadTimeout(60000);
//                conn.setConnectTimeout(60000);
//                conn.setRequestMethod("POST");
//                conn.setDoInput(true);
//                conn.setDoOutput(true);
//                conn.setRequestProperty("User-Agent", "android");
//                conn.setRequestProperty("charset", "utf-8");
//                conn.setRequestProperty("Content-Type", "application/json");
//
//                if (Build.VERSION.SDK_INT > 9) {
//                    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//                    StrictMode.setThreadPolicy(policy);
//                }
//
//                OutputStream os = conn.getOutputStream();
//                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
//                writer.write(objParam.toString());
//
//                writer.flush();
//                writer.close();
//                os.close();
//                int responseCode = conn.getResponseCode();
//
//                if (responseCode == HttpsURLConnection.HTTP_OK) {
//                    String line;
//                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                    while ((line = br.readLine()) != null) {
//                        response += line;
//                    }
//                } else {
//                    response = "";
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            Debug.trace("Exception fire");
//            android.os.Process.killProcess(android.os.Process.myPid());
//            System.exit(10);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}