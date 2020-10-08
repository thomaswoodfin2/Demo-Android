package com.pinpoint.appointment.models;



import com.pinpoint.appointment.api.RestClient;
import com.pinpoint.appointment.helper.PrefHelper;

import java.util.List;

/**
 * Created by comp252 on 27-05-2017.
 */

public class LoginHelper {
    private static LoginHelper instance;

    private CustomerDetails user = null;

    private LoginHelper() {
//        if(!PrefHelper.getString(PrefHelper.KEY_CURRENT_LOGGED_IN_USER, "").equalsIgnoreCase(""))
        user = RestClient.gson.fromJson(PrefHelper.getInstance().getString(PrefHelper.KEY_CURRENT_LOGGED_IN_USER, ""), CustomerDetails.class);
    }

    public static LoginHelper getInstance() {
        if (null == instance) {
            instance = new LoginHelper();
        }
        return instance;
    }

    // USER METHODS
    public void doLogin(CustomerDetails user) {
        this.user = user;
        PrefHelper.setString(PrefHelper.KEY_CURRENT_LOGGED_IN_USER, RestClient.gson.toJson(user));
    }

    public Boolean isLoggedIn() {
        return null != user;
    }

    public void logoutUser() {
        user = null;

        PrefHelper.setString(PrefHelper.KEY_CURRENT_LOGGED_IN_USER, "");
    }


    public String getUserID() {
        return null == user ? "0" : user.getUserid();
    }

    public String getName() {
        return null == user ? "" : user.getName();
    }

    public String getEmail() {
        return null == user ? "" : user.getEmail();
    }


}