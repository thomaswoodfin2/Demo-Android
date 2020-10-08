package com.pinpoint.appointment.enumeration;

/**
 * This enum contain register type and it's value
 * To identify how user registered in app like with facebook, google, twitter and app.
 */

public enum SubscriptionStatus {

CANCELLED("cancelled"), UNSUBSCRIBED("unsubscribed"), SUBSCRIBED("subscribed");
    private final String registerType;

    SubscriptionStatus(final String mRegisterType) {
        this.registerType = mRegisterType;
    }

    /**
     * @return (int) registerType : it return enum value 1,2,3,4 e.g. APP.getType - 1
     * registerType - 1
     */
    public String getType() {
        return registerType;
    }
}
