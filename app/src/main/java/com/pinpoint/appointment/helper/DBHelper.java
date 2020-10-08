package com.pinpoint.appointment.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "logs_db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(LogItem.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + LogItem.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }


    public long insertLogItem(String log) {

        String time_stamp = getTimeStampInFormat(new Date());

        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(LogItem.COLUMN_LOG, log);
        values.put(LogItem.COLUMN_TIMESTAMP, time_stamp);

        // insert row
        long id = db.insert(LogItem.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public LogItem getLogItem(long id)
    {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(LogItem.TABLE_NAME,
                new String[]{LogItem.COLUMN_ID, LogItem.COLUMN_LOG, LogItem.COLUMN_TIMESTAMP},
                LogItem.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        // prepare log object
        LogItem logItem = new LogItem(
                cursor.getInt(cursor.getColumnIndex(LogItem.COLUMN_ID)),
                cursor.getString(cursor.getColumnIndex(LogItem.COLUMN_LOG)),
                cursor.getString(cursor.getColumnIndex(LogItem.COLUMN_TIMESTAMP)));

        // close the db connection
        cursor.close();

        return logItem;
    }

    public List<LogItem> getAllLogs() {
        List<LogItem> arrTmp = new ArrayList<>();

        // Select All Query

        try {
            String selectQuery = "SELECT  * FROM " + LogItem.TABLE_NAME + " ORDER BY " +
                    LogItem.COLUMN_TIMESTAMP + " DESC";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    LogItem logItem = new LogItem();
                    logItem.setId(cursor.getInt(cursor.getColumnIndex(LogItem.COLUMN_ID)));
                    logItem.setLog(cursor.getString(cursor.getColumnIndex(LogItem.COLUMN_LOG)));
                    logItem.setTimestamp(cursor.getString(cursor.getColumnIndex(LogItem.COLUMN_TIMESTAMP)));

                    arrTmp.add(logItem);
                } while (cursor.moveToNext());
            }

            // close db connection
            db.close();

        }catch (Exception ex){ex.printStackTrace();}

        // return Logs list
        return arrTmp;
    }

    public int getLogsCount() {
        String countQuery = "SELECT  * FROM " + LogItem.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public int updateLog(LogItem logItem) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LogItem.COLUMN_LOG, logItem.getLog());

        // updating row
        return db.update(LogItem.TABLE_NAME, values, LogItem.COLUMN_ID + " = ?",
                new String[]{String.valueOf(logItem.getId())});
    }

    public void deleteLog(LogItem logItem) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(LogItem.TABLE_NAME, LogItem.COLUMN_ID + " = ?",
                new String[]{String.valueOf(logItem.getId())});
        db.close();
    }




    //getTimeStampInFormat
    public static String getTimeStampInFormat(Date date) {
        String formattedDate = "";
        try {
            DateFormat originalFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
            formattedDate = originalFormat.format(date);
        } catch (Exception ex) {
            ex.printStackTrace();
            return formattedDate;
        }

        return formattedDate;
    }


    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(LogItem.TABLE_NAME, null, null);
//        db.delete(LogItem.TABLE_NAME, Note.COLUMN_ID + " = ?",
//                new String[]{String.valueOf(note.getId())});
        db.close();
    }
}