package org.precise.raproto;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
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
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), brokerAddress, clientId);

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


            sharedPref = getSharedPreferences("Raproto", Context.MODE_PRIVATE);
            String username = Settings.Secure.getString(MQTTService.this.getContentResolver(),
                    Settings.Secure.ANDROID_ID);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(username);
            options.setPassword(password.toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);

            try {
                Log.d(TAG, "Starting MQTT Service");
                Log.d(TAG, "Trying to Connect");

                IMqttToken token = client.connect(options);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // We are connected
                        Log.d(TAG, "Connected Successfully.");
                        Log.d(TAG, String.valueOf(db.getNumRows(true)));

                        while(db.getNumRows(true) > 0) {
                            Log.d(TAG, "Reading Database.");
                            String row = db.readFirstRow();
                            try {
                                byte[] encodedPayload = row.getBytes("UTF-8");
                                MqttMessage message = new MqttMessage(encodedPayload);

                                //Set Quality of Service to QOS shared preference
                                //If shared pref is set to -1, default to 1
                                if (sharedPref.getInt("QOS", 1) != -1) {
                                    message.setQos(sharedPref.getInt("qos", 1));
                                } else {
                                    message.setQos(1);
                                }
                                Log.d(TAG, "Publishing Data.");
                                client.publish(topic, message);
                                db.deleteFirstRow();
                            } catch (UnsupportedEncodingException | MqttException e) {
                                Log.d(TAG, "Exception Occurred" + e);
                                e.printStackTrace();
                            }
                        }
                        Log.d(TAG, "Done Reading Database.");

                        //Disconnect MQTT
                        try {
                            Log.d(TAG, "Disconnecting MQTT.");
                            client.disconnect();
                        } catch (MqttException e) {
                            Log.d(TAG, "Exception Occurred" + e);
                        }

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



            //Delay data transmission by TX_RATE shared preference
            //If shared pref is set to -1, default to 60 seconds
            if (sharedPref.getInt("TX_RATE", -1) != -1) {
                mHandler.postDelayed(this, sharedPref.getInt("TX_RATE", 0)*1000);
            } else {
                mHandler.postDelayed(this, 60000);
            }

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