package com.pinpoint.appointment.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pinpoint.appointment.BaseApplication;
import com.pinpoint.appointment.BaseConstants;
import com.pinpoint.appointment.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.pinpoint.appointment.activities.SplashActivity;
import com.pinpoint.appointment.helper.MessageHelper;
import com.pinpoint.appointment.receivers.PinPointAlarmReceiver;
import com.pinpoint.appointment.service.LocationTracker;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class Util {

    public static int NOTIFICATION_ID = 101;

    private static Dialog alertDialog;

    public static boolean checkGooglePlayServiceAvailable(Context context) {
        int REQUEST_CODE_RECOVER_PLAY_SERVICES = 101;

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(context);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog((Activity) context, result,
                        REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
            }

            return false;
        }

        return true;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    ;

    public static boolean checkInternetConnection() {

        ConnectivityManager connectivity = (ConnectivityManager) BaseApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivity != null) {
            activeNetworkInfo = connectivity.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public static boolean checkInternetConnectionConnected() {

        ConnectivityManager connectivity = (ConnectivityManager) BaseApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = null;
        if (connectivity != null) {
            activeNetworkInfo = connectivity.getActiveNetworkInfo();
        }
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public String generateHashKey() {
        String hashKey = "";
        /** This method is generate Hash key for Facebook */
        try {
            PackageInfo info = BaseApplication.getInstance().getPackageManager().getPackageInfo(BaseApplication.getInstance().getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                hashKey = Base64.encodeToString(md.digest(), Base64.DEFAULT);
                Log.i("TAG", "Hash Key:" + hashKey);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashKey;
    }
    //  Facebook
//Hash key:SU1btzx29LywbadafssQ9TY0kqY=


    @SuppressLint("ClickableViewAccessibility")
    public static void setupOutSideTouchHideKeyboard(final View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboard(view);
                    return false;
                }

            });
        }

        // If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {

                View innerView = ((ViewGroup) view).getChildAt(i);
                setupOutSideTouchHideKeyboard(innerView);
            }
        }
    }

    public static boolean isTabletDevice(Context activityContext) {

        return (activityContext.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static void hideKeyboard(View view) {
        InputMethodManager mgr = (InputMethodManager) BaseApplication.getInstance().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * This method give the App version code
     *
     * @return (String) version : it return app version name
     * return version - e.g. 1, 2, 3
     */
    public static int getAppVersionCode() {
        int version = 0;
        try {
            PackageInfo pInfo = BaseApplication.getInstance().getPackageManager().getPackageInfo(BaseApplication.getInstance().getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return version;
    }

    /**
     * This method give the App versionName
     *
     * @return (String) version :  it return app version code e.g. 1.0, 2.0
     * return version - e.g. 1.0, 2.0
     */
    public static String getAppVersionName() {
        String version = "";
        try {
            PackageInfo pInfo = BaseApplication.getInstance().getPackageManager().getPackageInfo(BaseApplication.getInstance().getPackageName(), 0);
            version = pInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    public static String changeDateFormat(String selectedDate) {
        String formattedDate = "";
        try {
            DateFormat originalFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
            DateFormat targetFormat = new SimpleDateFormat("dd MMMM yyyy");
            Date date = originalFormat.parse(selectedDate);
            formattedDate = targetFormat.format(date);
        } catch (Exception ex) {
            ex.printStackTrace();
            return formattedDate;
        }

        return formattedDate;
    }

    public static String getAppKeyValue(Context context, int resId) {
//        String strKey = context.getResources().getResourceEntryName(resId);
        return MessageHelper.getInstance().getAppMessage(context.getString(resId));

        /*If key is null then return blank*/
//        if (strKey.length() == Constants.ZERO)
//            return "";
//
//        String strValue = "";
//        try {
//
//            if (BaseApplication.getInstance() != null && BaseApplication.msgHashMap != null) {
//                strValue = BaseApplication.msgHashMap.get(strKey);
//            }
//            /*Note: If any valMessageId = 152ue is not found then get from local string file*/
//            if (strValue != null && strValue.length() > Constants.ZERO) {
//                /*Note: In API \n is converted to \\n so replace \\n with \n*/
//                strValue = strValue.replace("\\n", "\n");
//            } else {
//                //Note: Get string resource value by name
//                strValue = context.getResources().getString(resId);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return strValue;
    }

    public static String getDeviceDetails() {

        StringBuilder deviceDetails = new StringBuilder();

        deviceDetails.append("\n************ VERSION INFORMATION ***********\n");
        deviceDetails.append(Util.getAppVersionName());

        deviceDetails.append("\n************ DEVICE INFORMATION ***********\n");
        deviceDetails.append("Brand: ");
        deviceDetails.append(Build.BRAND);
        deviceDetails.append(Constants.LINE_SEPARATOR);
        deviceDetails.append("Device: ");
        deviceDetails.append(Build.DEVICE);
        deviceDetails.append(Constants.LINE_SEPARATOR);
        deviceDetails.append("Model: ");
        deviceDetails.append(Build.MODEL);
        deviceDetails.append(Constants.LINE_SEPARATOR);
        deviceDetails.append("Id: ");
        deviceDetails.append(Build.ID);
        deviceDetails.append(Constants.LINE_SEPARATOR);
        deviceDetails.append("Product: ");
        deviceDetails.append(Build.PRODUCT);
        deviceDetails.append(Constants.LINE_SEPARATOR);
        deviceDetails.append("\n************ FIRMWARE ************\n");
        deviceDetails.append("SDK: ");
        deviceDetails.append(Build.VERSION.SDK_INT);
        deviceDetails.append(Constants.LINE_SEPARATOR);
        deviceDetails.append("Release: ");
        deviceDetails.append(Build.VERSION.RELEASE);
        deviceDetails.append(Constants.LINE_SEPARATOR);
        deviceDetails.append("Incremental: ");
        deviceDetails.append(Build.VERSION.INCREMENTAL);
        deviceDetails.append(Constants.LINE_SEPARATOR);
        return deviceDetails.toString();
    }

    public static boolean isAppForground(Context context) {
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            //The first in the list of RunningTasks is always the foreground task.
            ActivityManager.RunningTaskInfo foregroundTaskInfo = am.getRunningTasks(1).get(0);

            String foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
            PackageManager pm = context.getPackageManager();
            PackageInfo foregroundAppPackageInfo;

            foregroundAppPackageInfo = pm.getPackageInfo(foregroundTaskPackageName, 0);
            String foregroundTaskAppName = foregroundAppPackageInfo.applicationInfo.loadLabel(pm).toString();

            return foregroundTaskAppName.equalsIgnoreCase(context.getResources().getString(R.string.app_name));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void freeMemory() {
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    public static String generateHashKey(Context context) {
        String hashKey = "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                hashKey = Base64.encodeToString(md.digest(), Base64.DEFAULT);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashKey;
    }

    public static String stringToMD5(String strAuth) {

        MessageDigest mdEnc = null;
        try {
            mdEnc = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Exception while encrypting to md5");
            e.printStackTrace();
        } // Encryption algorithm
        mdEnc.update(strAuth.getBytes(), 0, strAuth.length());
        String md5 = new BigInteger(1, mdEnc.digest()).toString(16);
        while (md5.length() < 32) {
            md5 = "0" + md5;
        }
        return md5;
    }

    public static long getNextdate(int nextDaysdate) {
        Calendar currentCal = Calendar.getInstance();
        currentCal.add(Calendar.DATE, nextDaysdate);
        long date = currentCal.getTimeInMillis();
        return date;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

//                if ("primary".equalsIgnoreCase(type)) {
                return Environment.getExternalStorageDirectory() + "/" + split[1];
//                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return "";
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        if (procInfos != null) {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void showAlertWithButtonClick(final Context context, String msg, String header, String positiveButton, View.OnClickListener onClickListener1, String negativeButton, View.OnClickListener onClickListener2, boolean isShowNegative) {

//            if(alertDialog==null) {
        alertDialog = new Dialog(context, R.style.DialogTheme);
//            }

        @SuppressLint("InflateParams") final View view = LayoutInflater.from(context).inflate(R.layout.dialog_alert_new, null);
        TextView txtHeader = view.findViewById(R.id.txtHeader);
        TextView txtMessage = view.findViewById(R.id.tv_txtMessage);
        TextView txtNegative = view.findViewById(R.id.tv_txtNegative);
        ImageView iv_header = (ImageView) view.findViewById(R.id.iv_header);

        txtHeader.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        txtMessage.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));
        txtNegative.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_REGULAR));

        if (header.equalsIgnoreCase("Alert")) {
            iv_header.setImageResource(R.drawable.ic_call_black_24dp);
        } else if (header.equalsIgnoreCase("Logout")) {
            iv_header.setImageResource(R.drawable.ic_info_outline_black_24dp);
        }
        Button btn_positive = view.findViewById(R.id.btn_positive);
        btn_positive.setTypeface(BaseApplication.mTypefaceMap.get(BaseConstants.OPENSANS_SEMIBOLD));
        txtHeader.setText(header);
        txtMessage.setText(Html.fromHtml(msg));
        btn_positive.setText(positiveButton);
        txtNegative.setText(negativeButton);
        if (isShowNegative) {
            txtNegative.setVisibility(View.VISIBLE);
        }
//          mTxtYes.setTag(buttonText);*/
        btn_positive.setOnClickListener(onClickListener1);
        txtNegative.setOnClickListener(onClickListener2);
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                hide();
            }
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setContentView(view);
        try {
            if (alertDialog != null) {
                if (!alertDialog.isShowing()) {
                    alertDialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean isDialogShowing() {
        return alertDialog != null && alertDialog.isShowing();
    }

    public static void hide() {
        if (alertDialog != null) {
            if (alertDialog.isShowing())
                alertDialog.dismiss();
        }
    }


    //showToast
    public static void showToast(final Context mContext, final String msg) {
        try {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //sarfaraj
    public static boolean checkGPS(Context context) {
        // TODO Auto-generated method stub

        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }

    public static void displayGPSNotification(Context context) {

        String msg = "PinPoint can only report your GPS location when the GPS is turned on. You must turn it on.";

        Intent notifyIntent = new Intent(context, SplashActivity.class);
        // Sets the Activity to start in a new, empty task
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Creates the PendingIntent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(context,
                0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                context).setSmallIcon(R.drawable.ic_my_location)
                .setContentTitle("Turn GPS On")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg));
        mBuilder.setContentText(msg);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setAutoCancel(true);
        mBuilder.setSound(
                RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(notifyPendingIntent);

        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        nm.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public static boolean isNotificationVisible(Context ctx) {

        Intent notificationIntent = new Intent(ctx, SplashActivity.class);
        PendingIntent pi = PendingIntent.getActivity(ctx, NOTIFICATION_ID,
                notificationIntent, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public static void cancelNotification(Context ctx) {

        NotificationManager nm = (NotificationManager) ctx
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }


    public static boolean checkConnectivity(Context ctx) {

        if (ctx != null) {
            ConnectivityManager cm = (ConnectivityManager) ctx
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo ni = cm.getActiveNetworkInfo();

            return (ni != null && ni.isConnected());
//            return ni != null && ni.isConnected() && ni.isAvailable();
        } else
            return false;
    }


    //checkSensorAlaram
    public static boolean checkSensorAlaram(Context ctx) {
        boolean alarmUp = (PendingIntent.getBroadcast(ctx, 1, new Intent(ctx,
                        PinPointAlarmReceiver.class).setAction(Constants.SENSOR_ACTION),
                PendingIntent.FLAG_NO_CREATE) != null);

        return alarmUp;
    }

    //startSensorAlaram
    public static void startSensorAlaram(Context ctx, long minutes) {

        showLog("Sensor Alarm Time", minutes + "");
        AlarmManager alarmManager = (AlarmManager) ctx
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ctx, PinPointAlarmReceiver.class);
        intent.setAction(Constants.SENSOR_ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(ctx, 1, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(), minutes, pi);

        showLog("Sensor Alarm", "Alarm Started");
    }

    public static void stopAlaramSensor(Context ctx) {

        Intent intent = new Intent(ctx, PinPointAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 1,
                intent, 0);
        AlarmManager alarmManager = (AlarmManager) ctx
                .getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        if(isServiceRunning(ctx, LocationTracker.class.getName()))
            ctx.stopService(new Intent(ctx, LocationTracker.class));

        showLog("Sensor Alarm", "Alarm Stopped");
    }


    public static void showLog(String tag, String msg) {

        if (Constants.DEBUG)
            Log.d(tag, msg);
    }


    //requestBatterySavingIgnore
    public static void requestBatterySavingIgnore(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = context.getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                context.startActivity(intent);
            }
        }
    }


    public static boolean isBatterySavingIgnored(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = context.getPackageName();
            PowerManager pm = (PowerManager) context.getSystemService(context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                return false;
            }
        }

        return true;
    }

    public static long getToday() {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTimeInMillis();
    }

    public static void saveCurrTimeLogDB(Context context, long val) {
        new AppSharePreference(context).setValue_long("CURRENT_TIME", val);
    }

    public static long getCurrTimeLogDB(Context context) {
        return new AppSharePreference(context).getValue_long("CURRENT_TIME", 0);
    }

    public static boolean isServiceRunning(Context context, String name) {
        ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (name.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}