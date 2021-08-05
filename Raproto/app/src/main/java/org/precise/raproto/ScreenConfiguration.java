package org.precise.raproto;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;

public class ScreenConfiguration extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider {
    private final String TAG = "MQTT CONFIG";
    private final String MQTTURL = "tcp://broker.hivemq.com:1883";
    private final String topic = "configuring/#";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.screen_disclamer);

        // Get the color preference
        SharedPreferences sharedPref = getSharedPreferences("Raproto", Context.MODE_PRIVATE);
        int colorValue = sharedPref.getInt("color", 0);

        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(colorValue);

        AmbientModeSupport.attach(this);

        String clientId = MqttClient.generateClientId();
        MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext(), MQTTURL, clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    int qos = 0;
                    try {
                        IMqttToken subToken = client.subscribe(topic, qos);
                        subToken.setActionCallback(new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                // The message was published
                                Log.d(TAG, "Successfully Subscribed");
                                client.setCallback(new MqttCallback() {
                                    @Override
                                    public void connectionLost(Throwable cause) {
                                    }

                                    @Override
                                    public void messageArrived(String topic, MqttMessage message) throws Exception {
                                        Log.d("tag","message>>" + new String(message.getPayload()));
                                        Log.d("tag","topic>>" + topic);
                                        String payload = new String(message.getPayload());

                                        SharedPreferences.Editor editor = sharedPref.edit();
                                        editor.putString("configTime", payload);
                                        editor.apply();

                                    }

                                    @Override
                                    public void deliveryComplete(IMqttDeliveryToken token) {

                                    }

                                });
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken,
                                                  Throwable exception) {
                                // The subscription could not be performed, maybe the user was not
                                // authorized to subscribe on the specified topic e.g. using wildcards
                            }
                        });

                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
        Intent myIntent = new Intent(ScreenConfiguration.this, MenuConfiguration.class);
        ScreenConfiguration.this.startActivity(myIntent);
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
}

