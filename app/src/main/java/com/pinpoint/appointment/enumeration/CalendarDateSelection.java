package com.pinpoint.appointment.enumeration;

/**
 * This enum contains the calender Date
 * eg. CALENDAR_WITH_PAST_DATE
 * which is help to constraint calender Date selection
 */

public enum CalendarDateSelection {

    CALENDAR_WITH_ALL_DATE(), // No constraint in date selection
    CALENDAR_WITH_PAST_DATE(), // To set limit on past date selection
    CALENDAR_WITH_FUTURE_DATE(), // To set limit on future date selection
    CALENDAR_WITH_PAST_OR_FUTURE_DATE_INTERVAL(), // To set limit in past or future between years e.g. 1970-2000, 2020-2030
    CALENDAR_WITH_CURRENT_TO_FUTUREDATE()

}
