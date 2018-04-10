package com.pybeta.daymatter.sportslife.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by luogj on 2018/4/10.
 */

public class DBHelper extends SQLiteOpenHelper {

    public static final String USERTABLE = "user_table";

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + USERTABLE
                + "(_id INTEGER PRIMARY KEY AUTOINCREMENT,address TEXT,district TEXT,lattitude TEXT,longitude TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
