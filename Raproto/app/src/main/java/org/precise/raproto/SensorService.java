package org.precise.raproto;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class SensorService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;

    private DatabaseHandler db;


    public SensorService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        String android_id = Settings.Secure.getString(SensorService.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        //Create Database handler
        db = new DatabaseHandler(this);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Get sensor manager on starting the service.
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Registering Sensors
        //TODO: Add Battery
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE), SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;
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
    public void onSensorChanged(SensorEvent sensorEvent  ) {

        //TODO: Battery Sensor

        switch(sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float accel_x = sensorEvent.values[0];
                float accel_y = sensorEvent.values[1];
                float accel_z = sensorEvent.values[2];
                long tsLong = System.currentTimeMillis()/1000;
                String accel_string = "ACC:{\"x\":" + accel_x + ",\"y\":"+ accel_y+",\"z\":" + accel_z +"}";


                JSONObject xyz = new JSONObject();
                /*try {
                    xyz.put("x", accel_x);
                    xyz.put("y", accel_y);
                    xyz.put("z", accel_z);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject accel = new JSONObject();
                try {
                    accel.put("ACC", xyz);
                } catch (JSONException e) {
                    e.printStackTrace();
                }*/
                JSONObject accel_json = new JSONObject();
                try {
                    accel_json.put("ts", tsLong);
                    accel_json.put("values", accel_string);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.w("myApp", String.valueOf(accel_json));
                try {
                    db.addJson(accel_json);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;

            case Sensor.TYPE_GYROSCOPE:
                float gyro_x = sensorEvent.values[0];
                float gyro_y = sensorEvent.values[1];
                float gyro_z = sensorEvent.values[2];
                tsLong = System.currentTimeMillis()/1000;

                JSONObject gyro_xyz = new JSONObject();
                try {
                    gyro_xyz.put("x", gyro_x);
                    gyro_xyz.put("y", gyro_y);
                    gyro_xyz.put("z", gyro_z);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject gyro = new JSONObject();
                try {
                    gyro.put("GYRO", gyro_xyz);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject gyro_json = new JSONObject();
                try {
                    gyro_json.put("ts", tsLong);
                    gyro_json.put("values", gyro);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.w("myApp", String.valueOf(gyro_json));
                break;

            case Sensor.TYPE_GRAVITY:
                float grav_x = sensorEvent.values[0];
                float grav_y = sensorEvent.values[1];
                float grav_z = sensorEvent.values[2];
                tsLong = System.currentTimeMillis()/1000;

                JSONObject grav_xyz = new JSONObject();
                try {
                    grav_xyz.put("x", grav_x);
                    grav_xyz.put("y", grav_y);
                    grav_xyz.put("z", grav_z);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject grav = new JSONObject();
                try {
                    grav.put("GRAVITY", grav_xyz);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject grav_json = new JSONObject();
                try {
                    grav_json.put("ts", tsLong);
                    grav_json.put("values", grav);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.w("myApp", String.valueOf(grav_json));

                break;

            case Sensor.TYPE_HEART_RATE:
                //TODO: Look into Green light vs red light
                float hrm = sensorEvent.values[0];
                tsLong = System.currentTimeMillis()/1000;

                JSONObject hrm_obj = new JSONObject();
                try {
                    hrm_obj.put("hrm", hrm);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject hrm_2 = new JSONObject();
                try {
                    hrm_2.put("GRAVITY", hrm_obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject hrm_json = new JSONObject();
                try {
                    hrm_json.put("ts", tsLong);
                    hrm_json.put("values", hrm_2);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.w("myApp", String.valueOf(hrm_json));

                break;
        }
    }


}