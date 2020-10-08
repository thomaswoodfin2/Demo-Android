package com.pinpoint.appointment.enumeration;

/**
 * This enum contain register type and it's value
 * To identify how user registered in app like with facebook, google, twitter and app.
 */

public enum NotificationType {
//    APP(1), FACEBOOK(2), GOOGLE(3), TWITTER(4);
APPOINTMENT(1), FRIEND(2),PANIC(3);
    private final int registerType;

    NotificationType(final int mRegisterType) {
        this.registerType = mRegisterType;
    }

    /**
     * @return (int) registerType : it return enum value 1,2,3,4 e.g. APP.getType - 1
     * registerType - 1
     */
    public int getType() {
        return registerType;
    }
}
