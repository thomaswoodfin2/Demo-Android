package com.pinpoint.appointment.enumeration;

/**
 * This enum contain register type and it's value
 * To identify how user registered in app like with facebook, google, twitter and app.
 */

public enum Friendstatus {
//    APP(1), FACEBOOK(2), GOOGLE(3), TWITTER(4);
RECEIVED(0), SENT(1);
    private final int registerType;

    Friendstatus(final int mRegisterType) {
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
