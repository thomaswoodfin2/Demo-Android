package com.pinpoint.appointment.api;

/**
 * This interface catch the response
 * of API Call
 */
public interface RequestListener {

//    /**
//     * This method called when a request completes with the given response. Executed by a
//     * background thread.
//     *
//     * @param requestCode (RequestCode) : To identify the response
//     * @param object      (Object)           : API response object
//     */
//    void onRequestComplete(RequestCode requestCode, Object object);
//
//    /**
//     * This method called when a request has a network or request error. Executed by a
//     * background thread.
//     * <pre>{@code
//     * status = 1  // response status success
//     * status = 2  // response status fail
//     * status = 3  // response status error
//     * status = 4  // response status invalid
//     * }
//     * </pre>
//     *
//     * @param error       (String)            : To pass the required error e.g. network error, request error
//     * @param status      (int)           : To get the response status
//     * @param requestCode (RequestCode) : To identify the response
//     */
//    void onRequestError(String error, int status, RequestCode requestCode);

    void onComplete(RequestCode requestCode, Object object);

    /**
     * Called when a request has a network or request error. Executed by a
     * background thread: do not update the UI in this method.
     */
    void onException(String statusCode, String error, RequestCode requestCode);

    void onOtherStatus(RequestCode requestCode, Object object);
}
