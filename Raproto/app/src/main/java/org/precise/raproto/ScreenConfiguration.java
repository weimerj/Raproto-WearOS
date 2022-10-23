package org.precise.raproto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

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
import java.util.Map;

public class ScreenConfiguration extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider {

    private final String TAG = "Screen Configuration";
    String brokerAddress = "ssl://tb.precise.seas.upenn.edu:8883";
    //private String username = "fbdb89251fdc95aa"; // Access token for device
    private String password = ""; // leave empty
    int qos = 1;

    MqttAndroidClient client;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_config);

        String username = Settings.Secure.getString(ScreenConfiguration.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        // Get the color preference
        SharedPreferences sharedPref = getSharedPreferences("Raproto", Context.MODE_PRIVATE);
        int colorValue = sharedPref.getInt("color", 0);

        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(colorValue);

        AmbientModeSupport.attach(this);

        this.sharedPref = getSharedPreferences("Raproto", Context.MODE_PRIVATE);

        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), brokerAddress, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        //options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        Log.d(TAG, "Attempting to connect");

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Log.d(TAG, "Successfully connected.");
                    subscribeToAttributesTopic();
                    getSharedAttributes();
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Log.d(TAG, "Connection failed.");
                }
            });
        } catch (MqttException e) {
            Log.d(TAG, "Error occurred while connecting");
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new ScreenConfiguration.MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}

    /**
     * Subscribes to attribute updates from Thingsboard
     */
    protected void subscribeToAttributesTopic() {
        try {
            Log.d(TAG, "Subscribing..");
            client.subscribe("v1/devices/me/attributes/response/+", qos);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "Connection Lost.");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(TAG, "Message arrived.");
                    Log.d(TAG,"message>>" + new String(message.getPayload()));

                    addToSharedPreferences(message);

                    // Confirm values added to sharedPref

                    Map<String, ?> allEntries = sharedPref.getAll();
                    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                        Log.d("map values", entry.getKey() + ": " + entry.getValue().toString() + " Class: " + entry.getValue().getClass().toString());
                    }

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Subscription Successful.");
                }
            });
        } catch (MqttException e){
            Log.d(TAG, "Exception occurred " + e);
        }
    }

    /**
     * Requests attribute values from Thingsboard using publish topic.
     */
    protected void getSharedAttributes() {
        try {
            String attributesMessage = "{'GRAVITY': '-1'}";
            MqttMessage message = new MqttMessage(attributesMessage.getBytes("UTF-8"));
            client.publish("v1/devices/me/attributes/request/1", message);
            Log.d(TAG, "Requested shared attributes.");
        } catch (UnsupportedEncodingException | MqttException e) {
            Log.d(TAG, "Exception occurred " + e);
        }
    }


    /**
     * Adds attributes to shared preferences object.
     */
    protected void addToSharedPreferences(MqttMessage message) {
        try {
            // Add values to shared preferences obj
            JSONObject response = new JSONObject(message.toString());
            JSONObject sharedAttributes = response.getJSONObject("shared");
            JSONArray sharedAttributesKeys = sharedAttributes.names();

            for(int i = 0; i < sharedAttributes.length(); i++) {
                String key = sharedAttributesKeys.getString(i);
                SharedPreferences.Editor editor = sharedPref.edit();

                if(!key.toUpperCase().equals("NAME")){
                    int value = sharedAttributes.getInt(key);
                    editor.putInt(key, value);
                } else {
                    String value = sharedAttributes.getString(key);
                    editor.putString(key, value);
                }

                editor.apply();

            }
        } catch (Exception e){
            Log.d(TAG, "Error parsing message: " + e);
        }

    }


}

