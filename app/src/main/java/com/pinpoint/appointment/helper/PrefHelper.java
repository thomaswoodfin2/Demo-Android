package com.pinpoint.appointment.helper;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.reflect.TypeToken;
import com.pinpoint.appointment.BaseApplication;
import com.google.gson.Gson;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrefHelper {

    //shared pref file name
    private static final String APP_PREF = "appPreference";
    // Faster pref saving for high performance
    private static final Method sApplyMethod = findApplyMethod();
    public static final String KEY_DEVICE_TOKEN = "deviceToken";
    public static String KEY_CURRENT_LOGGED_IN_USER = "currentLogInUserDetails";
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;
    private static PrefHelper instance;
//    public static String KEY_USER = "user";

    public static String KEY_USER_CART_PRODUCT_DETAIL = "userCartProductDetails";
    public static String KEY_USER_TOTAL_BASKET = "userTotalBasket";
    public static String KEY_USER_CART_SELECTED_ADDRESS = "userCartSelectedAddress";

    public static final String KEY_TOTAL_RECORDS = "TotalRecord";
    public static final String KEY_TOTAL_FRIENDS = "total_friend";
    public static final String KEY_TOTAL_APPOINTMENTS = "totalAppointment";
    public static final String KEY_ZOOM_INST = "zoomInstFirstTime";
    public static final String KEY_TOTAL_RECORDS_TRANSACTION = "TotalRecordTransaction";
    public static final String KEY_TRANSACTION_TYPE_LIST = "transactionTypeList";
    public static final String KEY_NOTIFICATION_COUNT = "NotificationCount";

    public static final String KEY_UNREAD_NOTIFICATION = "UnreadNotification";
    public static final String KEY_UNREAD_DISCUSSION = "UnreadDiscssu" +
            "" +
            "" +
            "" +
            "ion";


    public static final String KEY_TOTAL_MSGS_COUNT = "TotalMsgsCount";

    // Faster pref saving for high performance

    public static String KEY_Primary_TRIP = "primaryTrips";


    public static final String KEY_TOTAL_ENQUIRY_RECORD = "totalEnquiryRecord";
    public static final String KEY_TOTAL = "total";
    public static String KEY_USER = "user";
    public static String KEY_MESSAGE = "message";

    public static String KEY_AUTHENTICATIONKEY = "authBase64Key";
    public static String KEY_MESSAGE_UPDATE_DATE = "messageUpdateDate";
    public static String KEY_NEW_ENQUIRY = "newEnquiry";
    public static String KEY_BOOKMARK_ENQUIRY = "bookmarkEnquiry";
    public static String KEY_MYCURRENT_ENQUIRY = "myCurrentBid";
    public static String KEY_CONFIRM_BID = "confirmBid";
    public static String KEY_COMPLETED_BID = "completedBid";
    public static String KEY_TOTAL_ENQUIRY = "totalEnquiry";
    public static final String KEY_ABOUT_URL = "cmsContentURL";
    public static final String KEY_TERMS_URL = "cmsAboutUrl";
    public static final String KEY_VEHICLE_URL = "cmsVehicle";
    public static final String KEY_LOAN_URL = "cmsLoanUrl";
    public static final String KEY_TNC_LITE = "tncLite";
    public static final String KEY_PRIVACY_POLICY = "privacyPolicy";
    public static final String KEY_TRACKING_CODE = "generatedTrackingCode";
    public static final String KEY_UPLOADED = "uploaded";
    public static String KEY_LAST_LAT="lastlat";
    public static String KEY_LAST_LONG="lastlong";
    public static String KEY_SWITCHOFFTIME="switchoffTime";

    //constructor
    private PrefHelper() {
    }

    @SuppressLint("CommitPrefEdits")
    public static PrefHelper getInstance() {
        int PRIVATE_MODE = 0;
        preferences = BaseApplication.getInstance().getSharedPreferences(APP_PREF, PRIVATE_MODE);
        editor = preferences.edit();
        if (instance == null) {
            instance = new PrefHelper();
        }
        return instance;
    }

//
//    private static Method findApplyMethod() {
//        try {
//            final Class<SharedPreferences.Editor> cls = SharedPreferences.Editor.class;
//            return cls.getMethod("apply");
//        } catch (final NoSuchMethodException unused) {
//            // fall through
//        }
//        return null;
//    }
//
//    private static void apply(final SharedPreferences.Editor editor) {
//        if (sApplyMethod != null) {
//            try {
//                sApplyMethod.invoke(editor);
//                return;
//            } catch (final InvocationTargetException unused) {
//                // fall through
//            } catch (final IllegalAccessException unused) {
//                // fall through
//            }
//        }
//        editor.commit();
//    }
//
//    public int getInt(final String key, final int defaultValue) {
//        return preferences.getInt(key, defaultValue);
//    }
//
//    public void setInt(final String key, final int value) {
//        editor.putInt(key, value);
//        apply(editor);
//    }
//
//    public static String getString(final String key, final String defaultValue) {
//        return preferences.getString(key, defaultValue);
//    }

//    public static void setString(final String key, final String value) {
//        editor.putString(key, value);
//        apply(editor);
//    }
//    public static void deletePreference(final String key) {
//        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance()).edit();
//        editor.remove(key);
//        apply(editor);
//    }
    public void setString(final String key, final Map list) throws IOException {
        editor.putString(key, new Gson().toJson(list));
        apply(editor);
    }

    public static <T> void setList(String key, List<T> list) {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance()).edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);

        editor.putString(key, json);
        editor.commit();
    }


    public static void setString(final String key, final String value) {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance()).edit();
        editor.putString(key, value);
        apply(editor);
    }

    public static String getString(final String key, final String defaultValue) {
        final SharedPreferences _preference = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
        return _preference.getString(key, defaultValue);
    }

    public static void setInt(final String key, final int value) {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance()).edit();
        editor.putInt(key, value);
        apply(editor);
    }

    public static int getInt(final String key, final int defaultValue) {
        final SharedPreferences _preference = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
        return _preference.getInt(key, defaultValue);
    }

    public static boolean getBoolean(final String key, final boolean defaultValue) {
        final SharedPreferences _preference = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
        return _preference.getBoolean(key, defaultValue);
    }

    public static void setBoolean(final String key, final boolean value) {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance()).edit();
        editor.putBoolean(key, value);
        apply(editor);
    }

    public static void setLong(final String key, final long value) {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance()).edit();
        editor.putLong(key, value);
        apply(editor);
    }

    public static long getLong(final String key, final long value) {
        final SharedPreferences _preference = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
        return _preference.getLong(key, Double.doubleToRawLongBits(value));
    }

    public static void setDouble(final String key, final double value) {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance()).edit();
        editor.putLong(key, Double.doubleToRawLongBits(value));
        apply(editor);
    }

//    public static void setCountryList(String key, List<Country> mCountry) {
//        final Editor editor = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance()).edit();
//        Gson gson = new Gson();
//        String json = gson.toJson(mCountry);
//        editor.putString(key, json);
//        editor.apply();
//    }
//
//    public static List<Country> getCountryList(final String key) {
//        List<Country> mMessageMap = new ArrayList<>();
//        final SharedPreferences _preference = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
//        Gson gson = new Gson();
//
//        String json = _preference.getString(key, "");
//
//        if (!json.equalsIgnoreCase("")) {
//            mMessageMap = gson.fromJson(json, new TypeToken<List<Country>>() {
//            }.getType());
//        }
//        return mMessageMap;
//    }

    public static long getDouble(final String key, final double value) {
        final SharedPreferences _preference = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
        return _preference.getLong(key, Double.doubleToRawLongBits(value));
    }

    public static void setHashMap(String key, HashMap<String, String> mMessageMap) {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance()).edit();
        Gson gson = new Gson();
        String json = gson.toJson(mMessageMap);
        editor.putString(key, json);
        editor.apply();
    }

    public static HashMap<String, String> getHashMap(final String key) {
        final SharedPreferences _preference = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance());
        Gson gson = new Gson();

        String json = _preference.getString(key, "");
        HashMap<String, String> mMessageMap = gson.fromJson(json, new TypeToken<HashMap<String, String>>() {
        }.getType());
        if (mMessageMap == null) {
            return new HashMap<String, String>();
        }
        return mMessageMap;
    }

    public static void deletePreference(final String key) {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance()).edit();
        editor.remove(key);
        apply(editor);
    }

    public static void deleteAllPreferences() {
        final SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(BaseApplication.getInstance()).edit();
        editor.clear();
        apply(editor);
    }

    private static Method findApplyMethod() {
        try {
            final Class<SharedPreferences.Editor> cls = SharedPreferences.Editor.class;
            return cls.getMethod("apply");
        } catch (final NoSuchMethodException unused) {
            // fall through
        }
        return null;
    }

    public static void apply(final SharedPreferences.Editor editor) {
        if (sApplyMethod != null) {
            try {
                sApplyMethod.invoke(editor);
                return;
            } catch (final InvocationTargetException unused) {
                // fall through
            } catch (final IllegalAccessException unused) {
                // fall through
            }
        }
        editor.commit();
    }

}