package com.pinpoint.appointment.enumeration;

/**
 * This enum contain register type and it's value
 * To identify how user registered in app like with facebook, google, twitter and app.
 */

public enum BottomTabs {
//    APP(1), FACEBOOK(2), GOOGLE(3), TWITTER(4);

    APPOINTMENTS(0), ALERTS(1),PANIC(2),FRIENDS(3),SETTINGS(4);
    private final int registerType;

    BottomTabs(final int mRegisterType) {
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
