package org.precise.raproto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MenuConfiguration extends FragmentActivity
        implements AmbientModeSupport.AmbientCallbackProvider {

    private final String TAG = "Menu Configuration";
    String brokerAddress = "ssl://tb.precise.seas.upenn.edu:8883";
    private String username = "fbdb89251fdc95aa"; // Access token for device
    private String password = ""; // leave empty
    int qos = 1;

    private List<ListsItem> mItems;
    MqttAndroidClient client;
    SharedPreferences sharedPref;
    ListView listView;
    ListViewAdapter adapter;

    String lastUpdated;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        // Get shared preferences
        sharedPref = getSharedPreferences("Raproto", Context.MODE_PRIVATE);
        int colorValue = sharedPref.getInt("color", 0);
        String lastConfig = sharedPref.getString("configTime", "");
        lastUpdated = sharedPref.getString("lastUpdated", "N/A");


        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(colorValue);

        AmbientModeSupport.attach(this);

        // Generate MQTT settings / options
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), brokerAddress, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(username);
        options.setPassword(password.toCharArray());
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        // Create a list of items for adapter to display.
        mItems = new ArrayList<>();
        mItems.add(new ListsItem(getString(R.string.update), getString(R.string.last_update) + lastUpdated, "2_rows_arrow"));
        //mItems.add(new ListsItem(getString(R.string.update), lastConfig, ScreenConfiguration.class,"2_rows_arrow"));
        //mItems.add(new ListsItem(getString(R.string.update), getString(R.string.update_status), "2_rows"));
        mItems.add(new ListsItem(getString(R.string.subscribe), getString(R.string.subscribe_topic), "2_rows"));
        mItems.add(new ListsItem(getString(R.string.publish), getString(R.string.publish_topic),"2_rows"));

        // Initialize an adapter and set it to ListView listView.
        adapter = new ListViewAdapter(this, mItems);
        listView = findViewById(R.id.about_lists);
        listView.setAdapter(adapter);

        // Set header of listView to be the title from title_layout.
        LayoutInflater inflater = LayoutInflater.from(this);
        View titleLayout = inflater.inflate(R.layout.title, null);
        TextView titleView = titleLayout.findViewById(R.id.title_text);
        titleView.setText(R.string.configuration);
        titleView.setOnClickListener(null); // make title non-clickable.

        listView.addHeaderView(titleView);

        // Goes to a new screen when you click on one of the list items.
        // Dependent upon position of click.
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {

                        // if first button is clicked, update last updated by field
                        if(position == 1){
                            mItems.set(0, new ListsItem("Update Attributes", "Last Update: In Progress", "2_rows_arrow_progress_bar"));
                            listView.setAdapter(adapter);
                            connectToMQTTService(options);
                        }

                        mItems.get(position - listView.getHeaderViewsCount())
                                .launchActivity(getApplicationContext());
                    }
                });
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}

    /**
     * Attempts connection to MQTT service
     */
    protected void connectToMQTTService(MqttConnectOptions options){
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
                    //Log.d(TAG,"message>>" + new String(message.getPayload()));

                    addToSharedPreferences(message);

                    // Confirm values added to sharedPref
                    /*
                    Map<String, ?> allEntries = sharedPref.getAll();
                    for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                        Log.d("map values", entry.getKey() + ": " + entry.getValue().toString() + " Class: " + entry.getValue().getClass().toString());
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
            SharedPreferences.Editor editor = sharedPref.edit();

            for(int i = 0; i < sharedAttributes.length(); i++) {
                String key = sharedAttributesKeys.getString(i);

                if(!key.toUpperCase().equals("NAME")){
                    int value = sharedAttributes.getInt(key);
                    editor.putInt(key, value);
                } else {
                    String value = sharedAttributes.getString(key);
                    editor.putString(key, value);
                }

                editor.apply();
            }

            lastUpdated = new SimpleDateFormat("MMM-dd-yy HH:mm").format(Calendar.getInstance().getTime());
            editor.putString("lastUpdated", lastUpdated);
            editor.apply();

            mItems.set(0, new ListsItem("Update Attributes", "Last Update: " + lastUpdated, "2_rows_arrow"));
            listView.setAdapter(adapter);

        } catch (Exception e){
            Log.d(TAG, "Error parsing message: " + e);
        }

    }
}
