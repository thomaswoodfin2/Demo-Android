package com.pinpoint.appointment.api;


import com.pinpoint.appointment.models.AppMessages;
import com.pinpoint.appointment.models.AppointmentData;
import com.pinpoint.appointment.models.CheckVersion;
import com.pinpoint.appointment.models.CustomerDetails;
import com.pinpoint.appointment.models.FriendDetails;
import com.pinpoint.appointment.models.LeadDetails;
import com.pinpoint.appointment.models.NotificationDetails;
import com.pinpoint.appointment.models.PanicData;
import com.pinpoint.appointment.models.PlanData;
import com.pinpoint.appointment.models.PropertyData;
import com.pinpoint.appointment.models.TrackingFriend;

import java.lang.reflect.Type;

/**
 * This enum store all API Request Codes
 */
public enum RequestCode {

    checkVersion(CheckVersion.class),
    messages(AppMessages[].class),
    loginCustomer(CustomerDetails.class),
    forgotpassword(String.class),
    verifypassword(String.class),
    signup(CustomerDetails.class),
    addfriend(String.class),
    CUSTOMER_LOGIN(String.class),
    deletefriend(String.class),
    acceptfriendrequest(String.class),
    denyfriendrequest(String.class),
    resendfriend(String.class),
    friendslist(FriendDetails[].class),
    addappointment(String.class),
    deleteappointment(String.class),
    acceptappointment(String.class),
    denyappointment(String.class),
    setuserstatus(String.class),
    addfriendlocation(String.class),
    updateuserinfo(String.class),
    getfriendlocation(TrackingFriend[].class),
    getSubscriptionPlans(PlanData[].class),
    logoutCustomer(String.class),
    setofflinereason(String.class),
    cancelsubscription(String.class),
    appointmentlist(AppointmentData[].class),
    panic(PanicData[].class),
    checkpaymentstatus(String.class),
    getnotification(NotificationDetails[].class),
    deletenotification(String.class),
    readnotification(String.class),
    resendappointment(String.class),
    checkemailexist(CheckVersion.class),//to check for if email already registered
    updatephone(String.class),
    updatepassword(String.class),
    getuserinfo(CustomerDetails[].class),
    getleads(LeadDetails[].class),
    propertylisting(PropertyData[].class),
    addproperty(String.class),
    addlead(String.class),
    deletelead(String.class),
    crashreport(String.class),
    crashreportlog(String.class),

    updatePaymentStatus(String.class),

    editproperty(String.class),
    deleteproperty(String.class),
    deletepropertyimage(String.class),
    deleteprofileimage(String.class),
    CUSTOMER_REGISTRATION(String.class);


    private Class<?> localClass = null;
    private Type localType = null;

    RequestCode(Class<?> localClass) {
        this.localClass = localClass;
    }

    /**
     * @return localClass(Class) : to get class
     */
    public Class<?> getLocalClass() {
        return localClass;
    }

    /**
     * @return localType(Type) : to get localType
     */
    public Type getLocalType() {
        return localType;
    }

}
