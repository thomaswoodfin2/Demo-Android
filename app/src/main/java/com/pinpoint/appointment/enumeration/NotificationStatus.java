package com.pinpoint.appointment.enumeration;

/**
 * This enum contain register type and it's value
 * To identify how user registered in app like with facebook, google, twitter and app.
 */

public enum NotificationStatus {
//    APP(1), FACEBOOK(2), GOOGLE(3), TWITTER(4);
RECEIVED_APPOINTMENT("1"),ACCEPTED_APPOINTMENT("2"), RECEIVED_FRIEND("3"),ACCEPTED_FRIEND("4"),PANIC("5");
    private final String registerType;

    NotificationStatus(final String mRegisterType) {
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
