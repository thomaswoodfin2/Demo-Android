package com.pinpoint.appointment.interfaces;

import com.pinpoint.appointment.api.RequestCode;

public interface DataObserver {

//    void OnSuccess(RequestCode requestCode);
//    void OnSuccess(RequestCode requestCode, Object object);
//    void OnFailure(RequestCode requestCode, String error);
//
//    void onRetryRequest(RequestCode requestCode);




    /**
     * This method called when a request completes with the response.
     * Executed by background thread.
     *
     * @param requestCode (RequestCode) : To identify the request type
     */
    void OnSuccess(RequestCode requestCode, Object object);

    /**
     * This method called when a request has a network or request error.
     * Executed by background thread.
     *
     * @param requestCode (RequestCode) : To identify the request type
     * @param error       (String) : required error message e.g. network error, request time out error
     */
    void OnFailure(RequestCode requestCode, String errorCode, String error);

    void onOtherStatus(RequestCode requestCode, Object object);

    /**
     * This method called when a request needs to recall
     *
     * @param requestCode (RequestCode) : To identify the request type
     */
    void onRetryRequest(RequestCode requestCode);

}
