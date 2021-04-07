package org.precise.raproto;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONException;
import org.json.JSONObject;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Raproto";
    private static final String TABLE_NAME = "SensorData";
    private static final String KEY_ID = "id";
    private static final String KEY_ts= "ts";
    private static final String KEY_values= "valuess";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_ts + " TEXT, " +
                KEY_values +  " TEXT)";

        db.execSQL(CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME );
        onCreate(db);
    }

    void addJson(JSONObject json) throws JSONException {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues json_values = new ContentValues();
        json_values.put(KEY_ts, json.getString("ts"));
        json_values.put(KEY_values, json.getString("values"));
        db.insert(TABLE_NAME, null, json_values);
        //db.close();
    }
}
