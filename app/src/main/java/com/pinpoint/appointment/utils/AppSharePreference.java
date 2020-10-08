package com.pinpoint.appointment.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AppSharePreference {
    private static final String USER_PREFS = "PIN_POINT_APPOINTMENT_APP_KEY";
    private SharedPreferences appSharedPrefs;
    private Editor prefsEditor;

    public AppSharePreference(Context context) {
        this.appSharedPrefs = context.getSharedPreferences(USER_PREFS, 0);
        prefsEditor = this.appSharedPrefs.edit();
    }

    public int getValue_int(String intKeyValue) {
        return this.appSharedPrefs.getInt(intKeyValue, 0);
    }

    public int getValue_int(String intKeyValue, int defVal) {
        return this.appSharedPrefs.getInt(intKeyValue, defVal);
    }

    public float getValue_float(String intKeyValue) {
        return this.appSharedPrefs.getFloat(intKeyValue, 0.0f);
    }

    public float getValue_float(String intKeyValue, float defVal) {
        return this.appSharedPrefs.getFloat(intKeyValue, defVal);
    }

    public String getValue_string(String stringKeyValue) {
        return this.appSharedPrefs.getString(stringKeyValue, "");
    }

    public String getValue_string(String stringKeyValue, String def) {
        return this.appSharedPrefs.getString(stringKeyValue, def);
    }

    public void setValue_int(String intKeyValue, int _intValue) {
        this.prefsEditor.putInt(intKeyValue, _intValue).commit();
    }

    public void setValue_float(String intKeyValue, float _intValue) {
        this.prefsEditor.putFloat(intKeyValue, _intValue).commit();
    }

    public boolean getValue_bool(String stringKeyValue) {
        return this.appSharedPrefs.getBoolean(stringKeyValue, false);
    }

    public boolean getValue_bool(String stringKeyValue, boolean defVal) {
        return this.appSharedPrefs.getBoolean(stringKeyValue, defVal);
    }

    public void setValue_string(String stringKeyValue, String _stringValue) {
        this.prefsEditor.putString(stringKeyValue, _stringValue).commit();
    }

    public void setValue_bool(String stringKeyValue, boolean _boolValue) {
        this.prefsEditor.putBoolean(stringKeyValue, _boolValue).commit();
    }

    public void setValue_int(String intKeyValue) {
        this.prefsEditor.putInt(intKeyValue, 0).commit();
    }

    public void setValue_long(String stringKeyValue, long val) {
        this.prefsEditor.putLong(stringKeyValue, val).commit();
    }

    public long getValue_long(String stringKeyValue, long defVal) {
        return this.appSharedPrefs.getLong(stringKeyValue, defVal);
    }

    public ArrayList<String> getValue_list(String stringKeyValue) {
        Set<String> set =  this.appSharedPrefs.getStringSet(stringKeyValue, null);

        if(set == null){
            return new ArrayList<String>();
        }
        ArrayList arrList = new ArrayList<String>();

        arrList.addAll(set);

        return arrList;
    }

    public void setValue_list(String stringKeyValue, ArrayList<String> val) {
        Set<String> set = new HashSet<>();
        set.addAll(val);

        this.prefsEditor.putStringSet(stringKeyValue, set).commit();
    }

    public void removeKey(String key) {
        this.prefsEditor.remove(key).commit();
    }

    public void clearData() {
        this.prefsEditor.clear().commit();
    }
}

/*

    //Set the values
    Set<String> set = new HashSet<String>();
set.addAll(mArrayList1);
        scoreEditor.putStringSet("key", set);
        scoreEditor.commit();

//Retrieve the values
        Set<String> set = new HashSet<String>();
        set = myScores.getStringSet("key", null);
        */