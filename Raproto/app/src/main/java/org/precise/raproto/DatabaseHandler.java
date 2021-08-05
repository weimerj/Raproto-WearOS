package org.precise.raproto;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
//TODO:Close Database
//TODO: How much space do we use for our database? When do we stop collecting?
public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Raproto";
    private static final String TABLE_NAME = "SensorData";
    private static final String KEY_ID = "id";
    private static final String KEY_device_id= "device_id";
    private static final String KEY_values= "buffer";
    private final String TAG = "DATABASE HANDLER";


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_device_id + " TEXT, " +
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
        json_values.put(KEY_device_id, json.getString("device_id"));
        json_values.put(KEY_values, json.getString("buffer"));
        db.insert(TABLE_NAME, null, json_values);

        //db.close();
    }

    public long getNumRows(){
        SQLiteDatabase db = this.getWritableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, TABLE_NAME);
        //db.close();
        return count;
    }

    public JSONObject readFirstRow() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        JSONObject json = new JSONObject();
        if(cursor.moveToFirst()) {
            String device_id = cursor.getString(cursor.getColumnIndex(KEY_device_id));
            String buffer = cursor.getString(cursor.getColumnIndex(KEY_values));
            try {
                json.put("device_id", device_id);
                json.put("buffer", buffer);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    public void deleteFirstRow(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
        if(cursor.moveToFirst()) {
            String rowId = cursor.getString(cursor.getColumnIndex(KEY_ID));
            db.delete(TABLE_NAME, KEY_ID + "=?",  new String[]{rowId});
        }
    }

}
