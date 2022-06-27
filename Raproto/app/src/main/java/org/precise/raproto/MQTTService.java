package org.precise.raproto;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class MQTTService extends Service {

    private final String TAG = "MQTT Service";
    String brokerAddress = "ssl://tb.precise.seas.upenn.edu:8883";
    String topic = "v1/devices/me/telemetry";
    private String username = "fbdb89251fdc95aa"; //Access token for device
    private String password = ""; //leave empty

    private DatabaseHandler db;
    MqttAndroidClient client;
    SharedPreferences sharedPref;

    public MQTTService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        //Create Database handler
        db = new DatabaseHandler(this);

        Log.d(TAG, "Starting MQTT Service");

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), brokerAddress, clientId);
        sharedPref = getSharedPreferences("Raproto", Context.MODE_PRIVATE);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        try {
            Log.d(TAG, "Trying to Connect");

            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "Connected Successfully.");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    Log.d(TAG, "Exception Occurred" + exception);
                }
            });
        } catch (MqttException e) {
            Log.d(TAG, "Exception Occurred" + e);
            e.printStackTrace();

        }
        mHandler.postDelayed(mUpdateTask, 10000);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_STICKY;
    }

    private Handler mHandler = new Handler();
    private Runnable mUpdateTask = new Runnable() {
        public void run() {
            Log.d(TAG, "Made it into Run");

            while(db.getNumRows(true) > 0) {
                String row = db.readFirstRow();

                try {
                    byte[] encodedPayload = row.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    client.publish(topic, message);
                    db.deleteFirstRow();
                } catch (UnsupportedEncodingException | MqttException e) {
                    Log.d(TAG, "Exception Occurred" + e);

                    e.printStackTrace();
                }
            }
            mHandler.postDelayed(this, 20000);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}