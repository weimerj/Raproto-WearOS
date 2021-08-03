package org.precise.raproto;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

    StringBuffer buffer = new StringBuffer(1024*10);

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

        String temp = "";
        JSONObject json = new JSONObject();

        if (buffer.length()< 1024*10) {

            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    float accel_x = sensorEvent.values[0];
                    float accel_y = sensorEvent.values[1];
                    float accel_z = sensorEvent.values[2];
                    long tsLong = System.currentTimeMillis();

                    temp = "{\"ts\":\"" + tsLong + "\",\"values\"={\"ACC\":{\"x\":" + accel_x + "\"y\":"
                            + accel_y + ",\"z\":" + accel_z + "}}}";

                    buffer.append(temp);
                    buffer.append(",");

                    break;

                case Sensor.TYPE_GYROSCOPE:
                    float gyro_x = sensorEvent.values[0];
                    float gyro_y = sensorEvent.values[1];
                    float gyro_z = sensorEvent.values[2];
                    tsLong = System.currentTimeMillis();

                    temp = "{\"ts\":\"" + tsLong + "\",\"values\"={\"GYRO\":{\"x\":" + gyro_x + "\"y\":"
                            + gyro_y + ",\"z\":" + gyro_z + "}}}";
                    buffer.append(temp);
                    buffer.append(",");

                    break;

                case Sensor.TYPE_GRAVITY:
                    float grav_x = sensorEvent.values[0];
                    float grav_y = sensorEvent.values[1];
                    float grav_z = sensorEvent.values[2];
                    tsLong = System.currentTimeMillis();

                    temp = "{\"ts\":\"" + tsLong + "\",\"values\"={\"GRAVITY\":{\"x\":" + grav_x + "\"y\":"
                            + grav_y + ",\"z\":" + grav_z + "}}}";
                    buffer.append(temp);
                    buffer.append(",");

                    break;

                case Sensor.TYPE_HEART_RATE:
                    //TODO: Look into Green light vs red light
                    float hrm = sensorEvent.values[0];
                    tsLong = System.currentTimeMillis();

                    temp = "{\"ts\":\"" + tsLong + "\",\"values\"={\"HRM\":{\"HRM\":" + hrm + "}}}";
                    buffer.append(temp);
                    buffer.append(",");

                    break;
            }
        }
        else{
            try {
                String android_id = Settings.Secure.getString(SensorService.this.getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                json.put("device_id", android_id);
                json.put("buffer", buffer);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                db.addJson(json);
                buffer.delete(0, buffer.length());

                //SharedPreferences sharedPref = getSharedPreferences("Raproto", Context.MODE_PRIVATE);
                //SharedPreferences.Editor editor = sharedPref.edit();
                //editor.putLong("numMessages", db.getNumRows());
                //editor.apply();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


}