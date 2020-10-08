package com.pinpoint.appointment.api;

/**
 * This class handle response status and message.
 * <pre>{@code
 * status = 1  // response status success
 * status = 2  // response status fail
 * status = 3  // response status error
 * status = 4  // response status invalid
 * }
 * </pre>
 */

public class ResponseStatus {

    // variable declaration
//    public static final int STATUS_FAIL = 0;   // response status fail
//    public static final int STATUS_SUCCESS = 1;   // response status success
//    public static final int STATUS_ERROR = 2;   // response status error
//    public static final int STATUS_INVALID = 3;   // response status invalid
    public static String STATUS_FAIL = "Fail";
    public static String STATUS_CRASH = "Failed";
    public static String STATUS_SUCCESS = "success";
    public static String STATUS_OK = "OK";
    public static String STATUS_ERROR = "Error";
    public static String STATUS_INVALID = "invalid";
    public static String STATUS_UNVERIFIED = "unverified";
    public static String STATUS_CHANGE = "change";
    public static String STATUS_ALREADY_LOGGED_IN = "alreadylogin";
    public static String STATUS_NOT_EXIST = "notexist";
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status;
    private String message;

    /**
     * @return (String) message : to get status message
     */
    String getMessage() {
        return message;
    }

    /**
     * @param message (String)  : to set status message
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * @return (String) status  : to get status success or fail
     * <pre>{@code
     * status = 1  // response status success
     * status = 2  // response status fail
     * status = 3  // response status error
     * status = 4  // response status invalid
     * }
     * </pre>
     */
//    int getStatus() {
//        return status;
//    }
//
//    /**
//     * @param status (String) : to set status success or fail
//     */
//    public void setStatus(final int status) {
//        this.status = status;
//    }

    /**
     * give status of API call
     *
     * @return (boolean) : return Either true or false on API response
     */
    boolean isSuccess() {
        return status.equalsIgnoreCase(STATUS_SUCCESS) || status.equalsIgnoreCase(STATUS_OK);
    }

    /**
     * give status of error API call
     *
     * @return (boolean) : return either true or false on API response
     */
    boolean isFail() {
        return status .equalsIgnoreCase (STATUS_FAIL);
    }

    /**
     * give status of error API call
     *
     * @return (boolean) : return either true or false on API response
     */
    boolean isError() {
        return status.equalsIgnoreCase(STATUS_ERROR);
    }

    /**
     * give status of error API call
     *
     * @return (boolean) : return either true or false on API response
     */
    boolean isInvalid() {
        return status .equalsIgnoreCase(STATUS_INVALID);
    }

    public boolean isOtherError() {
        return status.equalsIgnoreCase(STATUS_UNVERIFIED) || status.equalsIgnoreCase(STATUS_ALREADY_LOGGED_IN)|| status.equalsIgnoreCase(STATUS_NOT_EXIST);
    }
}
