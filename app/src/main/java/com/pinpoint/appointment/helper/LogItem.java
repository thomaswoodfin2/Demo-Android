package com.pinpoint.appointment.helper;

public class LogItem {
    public static final String TABLE_NAME = "logs";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_LOG = "msg";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private int id;
    private String msg;
    private String timestamp;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_LOG + " TEXT,"
                    + COLUMN_TIMESTAMP + " TEXT"
                    + ")";

    public LogItem() {
    }

    public LogItem(int id, String msg, String timestamp) {
        this.id = id;
        this.msg = msg;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getLog() {
        return msg;
    }

    public void setLog(String msg) {
        this.msg = msg;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
