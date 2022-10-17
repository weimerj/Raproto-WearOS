package org.precise.raproto;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

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

    private Handler mHandler = new Handler();
    private Runnable mUpdateTask = new Runnable() {
        public void run() {
            Log.d(TAG, "Made it into Run");

            while(db.getNumRows(true) > 0) {
//                JSONObject json = db.readFirstRow();
                String row = db.readFirstRow();

                try {
//                    byte[] encodedPayload = json.toString().getBytes("UTF-8");
                    byte[] encodedPayload = row.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);

                    //Set Quality of Service to QOS shared preference
                    //If shared pref is set to -1, default to 1
                    if (sharedPref.getInt("QOS", 1) != -1) {
                        message.setQos(sharedPref.getInt("qos", 1));
                    } else {
                        message.setQos(1);
                    }

                    client.publish(topic, message);
                    db.deleteFirstRow();
                } catch (UnsupportedEncodingException | MqttException e) {
                    Log.d(TAG, "Exception Occurred" + e);
                    e.printStackTrace();
                }
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
    /**
     * Subscribes to attribute updates from Thingsboard and adds to shared preferences.
     */
    private void subscribeToAttributesTopic() {
        try {
            Log.d(TAG, "Subscribing..");
            client.subscribe("v1/devices/me/attributes/response/+", 1);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "Connection Lost.");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(TAG, "Message arrived.");
                    Log.d(TAG,"message>>" + new String(message.getPayload()));
                    Log.d(TAG,"topic>>" + topic);

                    // Add values to shared preferences obj
                    JSONObject response = new JSONObject(message.toString());
                    JSONObject sharedAttributes = response.getJSONObject("shared");
                    JSONArray sharedAttributesKeys = sharedAttributes.names();

                    for(int i = 0; i < sharedAttributes.length(); i++){
                        String key = sharedAttributesKeys.getString(i);
                        String value = sharedAttributes.getString(key);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(key, value);
                        editor.apply();
                    }

                    // Confirm values added to sharedPref
                    /*
                    Map<String, ?> allEntries = sharedPref.getAll();
                    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                        Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
                    }
                     */
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Subscription Successful.");
                }
            });
        } catch (MqttException e){
            Log.d(TAG, "Exception occurred " + e);
            e.printStackTrace();
        }
    }
    /**
     * Requests attribute values from Thingsboard using publish topic.
     */
    private void getSharedAttributes() {
        try {
            String attributesMessage = "{'GRAVITY': '-1'}";
            MqttMessage message = new MqttMessage(attributesMessage.getBytes("UTF-8"));
            client.publish("v1/devices/me/attributes/request/1", message);
            Log.d(TAG, "Requested shared attributes.");
        } catch (UnsupportedEncodingException | MqttException e) {
            Log.d(TAG, "Exception occurred " + e);
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}