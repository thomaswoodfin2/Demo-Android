package com.pinpoint.appointment;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created on 19-06-2017.
 */

public class BaseConstants {
    /**
     * Font File Path...
     */
    public static final int IMAGEHW = 300;
    public static final int fSize=2;
    public static final int FileSize = 2 * 1024 * 1024;
    public static final String OPENSANS_LIGHT = "fonts/OpenSans-Light.ttf";
    public static final String OPENSANS_SEMIBOLD = "fonts/OpenSans-Semibold.ttf";
    public static final String OPENSANS_REGULAR = "fonts/OpenSans-Regular.ttf";
    public static final String OPENSANS_BOLD = "fonts/OpenSans-Bold.ttf";
    public static final String LINE_SEPARATOR = "\n";
    public static final int PAGE_SIZE = 10;
    public static final int PAGE_NO = 1;
    public static final int ZERO = 0;
    public static String KEY_FROM="from";
    public static String KEY_OBJECT="objectdata";
    public static String KEY_ADDRESS="address";
    public static int NOTIFICATION_COUNT=0;
    public static long ALARM_TIMER=1*60*1000;
    public static long ALARM_TIMER_Service=5*60*1000;
    public static String COUNT="notificationCount";
    public static boolean SELECT_SENT=true;
    //public static LatLng lastLatLOng=null;
    public static boolean SELECT_SENT_FRIEND=false;
    public static boolean FROM_NOTIFICATION=false;
    public static String KEY_PROPERTY_ID="propertyId";
    public static String PAYMENT_STATUS="paymentStatus";
    public static String NOTIFICATION_CLICK="notificationClick";
    public static String NOTIFICATION_RECEIVED="notificationReceived";
    public enum CURRENCY_TYPE {CODE, TYPE}
    public static String base64EncodedPublicKey ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA38gj8JZHOeWccbPuXY4dZ8JpFKTASkfg8ilVglcF2JggLsXn8HyVwqsvrm+L7oF9zht/hDh7+rC0AW0r5k0/1kI6tfrqldquDglOOS3c7wCuyEXBhkY1DCU9iWFg5t8oTB5cs5cXwAfu5ePL9gpzlFw/VWKW1AuvPCcEWFeNKdTD3yvuiQGAOMboAjHZbAQcrpe+Tw0ipFDTeMeHdTNpFg27RCedJq0jql7EYv51PHJnPy2WJHwDXzKS6fo2fLFxpwddEWI8Fnu7KB3nrPftKbi+GHT5DQruKc9u0gxybUjeVgGZGKgtVqp45rnbAkKtETnP0wH3nyXnhap86fZAtQIDAQAB";
    public static final String ITEM_SKU_PREMIUM = "pinpoint_premium";
    public enum SETTINGS_TYPE {NONE, EMAIL, NOTIFICATION}
    public static final String KEY_FACEBOOK_ID = "id";

    public static final String KEY_FACEBOOK_FIRST_NAME = "first_name";

    public static final String KEY_FACEBOOK_LAST_NAME = "last_name";

    public static final String KEY_FACEBOOK_GENDER = "gender";

    public static final String KEY_FACEBOOK_BIRTHDAY = "birthDate";

    public static final String KEY_FACEBOOK_LOCATION = "location";

    public static final String KEY_FACEBOOK_EMAIL = "email";

//    public static final String PICKDATEFORMAT = "dd/MM/yyyy";
public static final String PICKDATEFORMAT = "M/d/yyyy";
    public static final String PICKDATEFORMAT_API = "yyyy/MM/dd";
    public static final String PICKDATETIMEFORMAT = "yyyy/MM/dd HH:mm";
    public static final String APPOINTMENTDATETIME = "MM/dd/yyyy HH:mm a";

    public static final String PAYMENTTIME = "yyyy-MM-dd HH:mm:ss";
    public static final String PICKTIMEFORMAT = "HH:mm";
    public static final String DISPLAYPICKTIMEFORMAT = "hh:mm a";
    public static final String AUTH_STRING = "H3!loL0@d$_";
    public static int DEVICE_TYPE = 2;// Android temparary else 2
    public static int APP_TYPE = 1;
    public static boolean REFRESH_FRIENDS = false;
    public static boolean REFRESH_PROPERTIES = false;
    public static double LATITUDE = 0;
    public static double LONGITUDE = 0;
    public static int    RADIUS=50000;
    public static boolean REFRESH_APPOINTMENT = false;
    public static String CURRENTPHOTOPATH;
    public static final float VALUE_IMAGE_RATIO = 0.75f;
    public static final String GALLERY_FILE_TYPE = "image/*";
    public static Uri CAPTUREDFILEURI;
    public static final CharSequence EXTENSION_PNG = ".png";
    public static final CharSequence EXTENSION_JPG = ".jpg";
    public static final CharSequence EXTENSION_JPEG = ".jpeg";
    public static final CharSequence EXTENSION_DOC = ".doc";
    public static final CharSequence EXTENSION_TXT = ".txt";
    public static final CharSequence EXTENSION_PDF = ".pdf";
    public static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(
            new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));

    public static  String PACKAGE_NAME ="com.pinpoint.appointment";

    public static final String ACTION_BROADCAST = PACKAGE_NAME + ".broadcast";
    public static final String EXTRA_LOCATION = PACKAGE_NAME + ".location";

}