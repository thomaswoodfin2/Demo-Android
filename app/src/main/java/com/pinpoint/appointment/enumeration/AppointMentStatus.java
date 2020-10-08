package com.pinpoint.appointment.enumeration;

/**
 * This enum contain register type and it's value
 * To identify how user registered in app like with facebook, google, twitter and app.
 */

public enum AppointMentStatus {
//    APP(1), FACEBOOK(2), GOOGLE(3), TWITTER(4);
ACCEPTED("Accepted"), PENDING("Pending"),EXPIRED("Expired");
    private final String registerType;

    AppointMentStatus(final String mRegisterType) {
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
