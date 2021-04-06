package org.precise.raproto;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

public class SensorService extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;

    public SensorService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String android_id = Settings.Secure.getString(SensorService.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // Get sensor manager on starting the service.
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Registering Sensors
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
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

        //Toast.makeText(getApplicationContext(),"Collecting Accel Data",Toast.LENGTH_SHORT).show();

        //"{\"ts\": \"%llu\",\"values\"={\"%s_SYS\":{\"Bat\":%d}}}", tsLong, android_id, battery_percent);
        switch(sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float accel_x = sensorEvent.values[0];
                float accel_y = sensorEvent.values[1];
                float accel_z = sensorEvent.values[2];
                long tsLong = System.currentTimeMillis()/1000;
                //TODO: JSON Formatting
                break;

            case Sensor.TYPE_GYROSCOPE:
                float gyro_x = sensorEvent.values[0];
                float gyro_y = sensorEvent.values[1];
                float gyro_z = sensorEvent.values[2];
                tsLong = System.currentTimeMillis()/1000;
                //TODO: JSON Formatting
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                float mag_x = sensorEvent.values[0];
                float mag_y = sensorEvent.values[1];
                float mag_z = sensorEvent.values[2];
                tsLong = System.currentTimeMillis()/1000;
                //TODO: JSON Formatting
                break;

            case Sensor.TYPE_GRAVITY:
                float grav_x = sensorEvent.values[0];
                float grav_y = sensorEvent.values[1];
                float grav_z = sensorEvent.values[2];
                tsLong = System.currentTimeMillis()/1000;
                //TODO: JSON Formatting
                break;

            case Sensor.TYPE_HEART_RATE:
                float hrm = sensorEvent.values[0];
                tsLong = System.currentTimeMillis()/1000;
                //TODO: JSON Formatting
                break;
        }
    }


}