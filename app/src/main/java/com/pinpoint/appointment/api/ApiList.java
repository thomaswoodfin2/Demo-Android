package com.pinpoint.appointment.api;


/**
 * This class contain the API
 * Keys,values and API list
 */
public class ApiList {

    public static final String KEY_MESSAGE = "message";
    public static final String KEY_RESPONSE = "response";
    public static final String KEY_CONTENT = "Content-Type";
    public static final String KEY_CONTENT_TYPE = "application/json; charset=utf-8";

    public static final String KEY_AUTHENTICATE = "Authenticate";

    //CheckVersion Api
    public static final String KEY_VERSIONCODE = "versionCode";
    public static final String KEY_APP_VERSION = "appVersion";
    public static final String KEY_USERID = "userId";
    public static final String KEY_STOP = "stop";
    public static final String KEY_MESSAGEUPDATE = "messageUpdateDate";
    public static final String KEY_DEVICETYPE = "deviceType";
    public static final String KEY_LASTLOGINTIME = "lastLoginTime";
    public static final String KEY_DEVICE_TOKEN = "deviceToken";
    public static final String KEY_DEVICE_DETAILS = "deviceDetails";
    public static final String KEY_APPLICATION_TYPE = "applicationType";
    public static final String KEY_AGAIN_LOGIN = "againLogin";


    //Signup aPIS
    public static final String KEY_EMAIL = "email";
    public static final String KEY_TYPE = "type";
    public static final String KEY_NAME = "name";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_CONTACT = "contact";
    public static final String KEY_COMPANY = "company";
    public static final String KEY_DEVICE_TYPE = "deviceType";
    public static final String KEY_HWID = "hwid";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_DEVICE_TOKEN_CLIENT = "deviceToken";
    public static final String KEY_DEVICE_HWID = "deviceHWId";

    public static final String KEY_START = "pageNo";
    public static final String KEY_LIMIT = "limit";


//    public static final String KEY_ISSENT = "issent";

    public static final String KEY_ISSENT = "isSent";

    public static final String KEY_SENDERID = "senderId";
    public static final String KEY_MODE = "mode";
    public static final String KEY_LOGINID = "loginId";
    public static final String KEY_FRIENDID = "friendId";
    public static final String KEY_AGENTID = "agentId";
    public static final String KEY_CLIENTID = "clientId";
    public static final String KEY_ID = "id";

    public static final String KEY_APP_DATE = "appDate";

    public static final String KEY_DEVICE_DATE = "deviceDate";
    public static final String KEY_DATE = "date";
    public static final String KEY_TIME = "time";
    public static final String KEY_INFORMATION = "information";
    public static final String KEY_ADDRESS = "address";

    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

    public static final String KEY_APPOINTMENTID = "appId";
    public static final String KEY_APPOINTMENT = "appointmentId";
    public static String KEY_OLD_PASSWORD = "oldPassword";
    public static String KEY_NEW_PASSWORD = "newPassword";
    public static String KEY_PAGE = "page";
    public static String KEY_PAGENO = "pageNo";
    public static String KEY_NOTIFICATIONID = "notiId";
    public static String KEY_STATUS = "status";
    public static String KEY_VERIFICATION_CODE = "verificationCode";
    public static String KEY_BADGCOUNT = "badgcount";
    public static String KEY_DATA = "data";
    public static String KEY_TIMEFRAME = "timeFrame";
    public static String KEY_PID = "pId";
    public static String KEY_DATETIME = "dateTime";
    public static String KEY_AMOUNT = "amount";
    public static String KEY_DEVICEINFO = "deviceInfo";
    public static String KEY_JSON_TERMS_URL = "terms";
    public static String KEY_PRIVACY_POLICY = "policy";
    public static String KEY_RESULT = "result";
    public static String KEY_TITLE = "title";
    public static String KEY_TIMEZONE = "timeZone";
    public static String KEY_FULLADDRESS = "fullAddress";


    public enum APIs {
        checkAppVersion("checkappversion"),
        checkemailexist("signup/checkemailexist"),
        loginCustomer("login"),
        logoutCustomer("login/logout"),
        forgotpassword("forgotpassword"),
        verifypassword("forgotpassword/change_password"),

        signup("signup/register"),

        updatephone("profile/updatecontact"),
        getuserinfo("profile/getinfo"),
        updateuserinfo("profile/updateinfo"),
        updatepassword("profile/verifypassword"),


        friendslist("friend/show_list"),
        deletefriend("friend/delete_friend"),
        resendfriend("friend/resend_request"),
        acceptfriendrequest("friend/accept_request"),
        denyfriendrequest("friend/request_deny"),
        addfriend("friend/friend_request"),
        addfriendlocation("friend/setlocation"),
        setuserstatus("login/setuserstatus"),

        getfriendlocation("friend/getlocation"),
        setofflinereason("login/offlinereason"),
        crashreport("crashreport"),
        crashreportlog("crashreportlog"),

        appointmentlist("appointment/appoint_list"),
        addappointment("appointment/add_appointment"),
        deleteappointment("appointment/delete_appointment"),
        acceptappointment("appointment/accept_req"),
        resendappointment("appointment/resend_appointment"),

        denyappointment("appointment/deny_req"),

        panic("panic"),
        checkpaymentstatus("panic/payment_status"),

        updatelocation("appointment/update_location"),
        getlocation("appointment/get_location"),

        getnotification("notification/notification_list"),
        deletenotification("notification/delete_notification"),
        readnotification("notification/read_notification"),


        propertylisting("property/property_list"),
        addproperty("property/create_prop"),
        editproperty("property/editproperty"),
        deleteproperty("property/delete_property"),
        deletepropertyimage("property/delete_property_image"),
        deleteprofileimage("profile/delete_profile_image"),
        deletelead("lead/delete_lead"),

        addlead("lead/add_lead"),
        getleads("lead/lead_list"),
        getSubscriptionPlans("getSubscriptionPlans"),
        updatePaymentStatus("profile/payment_success"),
        cancelsubscription("login/cancelsubscribe"),

        common_login("common_login"),
        getMessages("message");


        private final String URL;

        APIs(final String URL) {
            this.URL = URL;
        }

        public String getUrl() {
            return URL;
        }


    }


}