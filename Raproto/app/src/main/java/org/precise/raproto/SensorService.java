package org.precise.raproto;

import static android.hardware.Sensor.TYPE_HEART_RATE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SensorService extends Service implements SensorEventListener {
    private final static int BUFFER_THRESHOLD = 1024 * 10;
    private SensorManager mSensorManager;
    private DatabaseHandler db;
    private String TAG = SENSOR_SERVICE;
    private Intent batteryStatus;
    private MenuMain mMain = new MenuMain();

    StringBuffer buffer = new StringBuffer(BUFFER_THRESHOLD);
    JSONArray jsonArray = new JSONArray();

    public SensorService() {
    }

    @Override
    public void onCreate() {

        PackageManager packman = getPackageManager();
        super.onCreate();

        //Create Database handler
        db = new DatabaseHandler(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start Foreground Service
        createNotificationChannel();

        Intent intent1 = new Intent(MenuMain.class.toString());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0);

        Notification notification = new NotificationCompat.Builder(
                this, "ChannelID")
                .setContentTitle("MQTT Foreground Service")
                .setContentText("Service Running")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent).build();

        startForeground(1, notification);

        // Get sensor manager on starting the service.
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Battery filter register
        final IntentFilter batterIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        // Registering Sensors

        //        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        //        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        //        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
        //        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE), SensorManager.SENSOR_DELAY_NORMAL);

        SharedPreferences sharedPref = getSharedPreferences("Raproto", Context.MODE_PRIVATE);

        //Register ACC sensor
        if (sharedPref.getInt("ACC", -1) != -1) {
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sharedPref.getInt("ACC", -1) * 1000, sharedPref.getInt("ACC", -1) * 1000);
        }

        //Register GYRO sensor
        if (sharedPref.getInt("GYRO", -1) != -1) {
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), sharedPref.getInt("GYRO", -1) * 1000, sharedPref.getInt("GYRO", -1) * 1000);
            }

        //Register GRAVITY sensor
        if (sharedPref.getInt("GRAVITY", -1) != -1) {
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), sharedPref.getInt("GRAVITY", -1) * 1000, sharedPref.getInt("GRAVITY", -1) * 1000);
        }

        //Register HRM sensor
        if (sharedPref.getInt("HRM", -1) != -1) {
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE), sharedPref.getInt("HRM", -1) * 1000, sharedPref.getInt("HRM", -1) * 1000);
        }

        //Register battery sensor
        batteryStatus = this.registerReceiver(null, batterIntentFilter);

        return START_STICKY;
    }

    private void createNotificationChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(
                "ChannelID",
                "NotificationChannel",
                NotificationManager.IMPORTANCE_HIGH
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(notificationChannel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        String android_id = Settings.Secure.getString(SensorService.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);


        if(jsonArray.toString().getBytes().length < BUFFER_THRESHOLD){
            //put sensor value and timestamp to JSON string
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    jsonArray.put(getAccJson(sensorEvent, android_id));
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    jsonArray.put(getGyroJson(sensorEvent, android_id));
                    break;

                case Sensor.TYPE_GRAVITY:
                    jsonArray.put(getGravityJson(sensorEvent, android_id));
                    break;

                case TYPE_HEART_RATE:
                    //TODO: Look into Green light vs red light
                    jsonArray.put(getHRMJson(sensorEvent, android_id));
                    break;
            }
        }
        else{
            //include battery data to every buffer
            jsonArray.put(getBatteryJson(android_id));

            Log.d(TAG, jsonArray.toString());
            try {
                JSONObject LevelOneJson = new JSONObject();
                LevelOneJson.put("buffer", jsonArray);
                db.addJson(LevelOneJson);
                jsonArray = new JSONArray();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public JSONObject getBatteryJson(String android_id) {
        JSONObject LevelThrJson = new JSONObject();
        JSONObject LevelTwoJson = new JSONObject();
        JSONObject LevelOneJson = new JSONObject();

        //Put timestamp, battery status and level to JSON string
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        float batteryPot = level * 100 / (float) scale;
        //boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;
        long tsLong = System.currentTimeMillis();


        try {
            LevelThrJson.put("BATTERY", batteryPot);
            //LevelThrJson.put("IsCharging", isCharging);
            LevelTwoJson.put(android_id + "_BAT", LevelThrJson);
            LevelOneJson.put("ts", tsLong);
            LevelOneJson.put("values", LevelTwoJson);
        }catch (JSONException e) {
            e.printStackTrace();
        }
        return LevelOneJson;
    }

    public JSONObject getAccJson(SensorEvent sensorEvent, String android_id) {
        JSONObject LevelThrJson = new JSONObject();
        JSONObject LevelTwoJson = new JSONObject();
        JSONObject LevelOneJson = new JSONObject();

        float accel_x = sensorEvent.values[0];
        float accel_y = sensorEvent.values[1];
        float accel_z = sensorEvent.values[2];
        long tsLong = System.currentTimeMillis();

        try {
            LevelThrJson.put("x", Math.round(accel_x * 1000.0) / 1000.0);
            LevelThrJson.put("y", Math.round(accel_y * 1000.0) / 1000.0);
            LevelThrJson.put("z", Math.round(accel_z * 1000.0) / 1000.0);
            LevelTwoJson.put(android_id + "_ACC", LevelThrJson);
            LevelOneJson.put("ts", tsLong);
            LevelOneJson.put("values", LevelTwoJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return LevelOneJson;
    }

    public JSONObject getGyroJson(SensorEvent sensorEvent, String android_id) {
        JSONObject LevelThrJson = new JSONObject();
        JSONObject LevelTwoJson = new JSONObject();
        JSONObject LevelOneJson = new JSONObject();

        float gyro_x = sensorEvent.values[0];
        float gyro_y = sensorEvent.values[1];
        float gyro_z = sensorEvent.values[2];
        long tsLong = System.currentTimeMillis();

        try {
            LevelThrJson.put("x", Math.round(gyro_x * 1000.0) / 1000.0);
            LevelThrJson.put("y", Math.round(gyro_y * 1000.0) / 1000.0);
            LevelThrJson.put("z", Math.round(gyro_z * 1000.0) / 1000.0);
            LevelTwoJson.put(android_id + "_GYRO", LevelThrJson);
            LevelOneJson.put("ts", tsLong);
            LevelOneJson.put("values", LevelTwoJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return LevelOneJson;
    }

    public JSONObject getGravityJson(SensorEvent sensorEvent, String android_id) {
        JSONObject LevelThrJson = new JSONObject();
        JSONObject LevelTwoJson = new JSONObject();
        JSONObject LevelOneJson = new JSONObject();

        float grav_x = sensorEvent.values[0];
        float grav_y = sensorEvent.values[1];
        float grav_z = sensorEvent.values[2];

        long tsLong = System.currentTimeMillis();

        try {
            LevelThrJson.put("x", Math.round(grav_x * 1000.0) / 1000.0);
            LevelThrJson.put("y", Math.round(grav_y * 1000.0) / 1000.0);
            LevelThrJson.put("z", Math.round(grav_z * 1000.0) / 1000.0);
            LevelTwoJson.put(android_id + "_GRAVITY", LevelThrJson);
            LevelOneJson.put("ts", tsLong);
            LevelOneJson.put("values", LevelTwoJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return LevelOneJson;
    }

    public JSONObject getHRMJson(SensorEvent sensorEvent, String android_id) {
        JSONObject LevelThrJson = new JSONObject();
        JSONObject LevelTwoJson = new JSONObject();
        JSONObject LevelOneJson = new JSONObject();

        float hrm = sensorEvent.values[0];
        long tsLong = System.currentTimeMillis();

        try {
            LevelThrJson.put("HRM", hrm);
            LevelTwoJson.put(android_id + "_HRM", LevelThrJson);
            LevelOneJson.put("ts", tsLong);
            LevelOneJson.put("values", LevelTwoJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return LevelOneJson;
    }

}