package org.precise.raproto;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

import java.util.ArrayList;


public class MenuMain extends FragmentActivity implements AmbientModeSupport.AmbientCallbackProvider, DatabaseObserver {

    private ArrayList<ListsItem> mItems = new ArrayList<ListsItem>();

    private ListViewAdapterToggle mAdapter;
    private ListView listView;
    private DatabaseHandler db;
    private final String TAG = "MAIN";
    private int syncIndex = -1;
    private ListsItem syncItem;
    private Handler handler = new Handler();
    private PowerManager.WakeLock wakeLock = null;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Get BODY_SENSORS permission from user
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BODY_SENSORS}, 0);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_main);

        db = new DatabaseHandler(this);

        registerDatabaseObserver(this);

        String android_id = Settings.Secure.getString(MenuMain.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        SharedPreferences sharedPref = getSharedPreferences("Raproto", Context.MODE_PRIVATE);
        int colorValue = sharedPref.getInt("color", -1);
        if (colorValue == -1) {
            View view = this.getWindow().getDecorView();
            view.setBackgroundColor(Color.BLACK);
        }else {
            View view = this.getWindow().getDecorView();
            view.setBackgroundColor(colorValue);

        }

        //TODO: Fix the Toggle switch to be on if the app is on
        final Intent sensorIntent = new Intent(MenuMain.this,SensorService.class);
        AmbientModeSupport.attach(this);
        final Intent MQTTIntent = new Intent(MenuMain.this,MQTTService.class);
        AmbientModeSupport.attach(this);
        startService(MQTTIntent);

        // Create a list of items for adapter to display.

        mItems.add(new ListsItem(getString(R.string.raproto), sharedPref.getString("NAME",""), "2_rows_centered"));
        mItems.add(new ListsItem(getString(R.string.device_id), android_id, "2_rows"));
        mItems.add(new ListsItem(getString(R.string.on_off), "toggle"));
        mItems.add(syncItem = new ListsItem(getString(R.string.sync), String.valueOf(db.getNumRows(true)),"2_rows"));
        mItems.add(new ListsItem(getString(R.string.settings), MenuSettings.class,"arrow"));
        mItems.add(new ListsItem(getString(R.string.about), MenuAbout.class, "arrow"));
        mItems.add(new ListsItem(getString(R.string.exit),"arrow"));

        syncIndex = mItems.indexOf(syncItem);

        // Custom adapter used so we can use custom layout for the rows within the list.
        int finalColorValue = colorValue;
        mAdapter =
                new ListViewAdapterToggle(
                        this,
                        mItems,
                        new ListViewAdapterToggle.SwitchChangeListener() {
                            @SuppressLint("ResourceAsColor")
                            @Override
                            public void onChange(boolean switchOn) {
                                SharedPreferences sharedPref = MenuMain.this.getSharedPreferences("Raproto", Context.MODE_PRIVATE);
                                if (switchOn) {
                                    Log.d(TAG, "Switch On");
                                    PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                                    wakeLock = manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Wakelock :: TAG");
                                    if(!wakeLock.isHeld()) {
                                        Log.d(TAG, "Wakelock Acquired.");
                                        wakeLock.acquire();
                                    }
                                    View view = getWindow().getDecorView();
                                    int color = Color.parseColor("#37803a");
                                    view.setBackgroundColor(color);
                                    startForegroundService(sensorIntent);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putInt("color", color);
                                    editor.apply();

                                } else {
                                    Log.d(TAG, "Switch Off");

                                    View view = getWindow().getDecorView();
                                    view.setBackgroundColor(Color.BLACK);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putInt("color", Color.BLACK);
                                    editor.apply();
                                    stopService(sensorIntent);

                                    try{
                                        if (wakeLock.isHeld())
                                            wakeLock.release();//always release before acquiring for safety just in case
                                    }
                                    catch(Exception e){
                                        //probably already released
                                        Log.e(TAG, e.getMessage());
                                    }
                                }
                            }
                        });

        // Initialize an adapter and set it to ListView listView.
        //ListViewAdapter adapter = new ListViewAdapter(this, mItems);
        listView = findViewById(R.id.list_view_lists);
        listView.setAdapter(mAdapter);

        // Set header of listView to be the title from title_layout.
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View titleLayout = inflater.inflate(R.layout.title, null);
//        TextView titleView = titleLayout.findViewById(R.id.title_text);
//        titleView.setText(R.string.app_name);
//        titleView.setOnClickListener(null); // make title non-clickable.
//
//        listView.addHeaderView(titleView);


        // Goes to a new screen when you click on one of the list items.
        // Dependent upon position of click.
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        mItems.get(position - listView.getHeaderViewsCount())
                                .launchActivity(getApplicationContext());

                        // If sync now button is pressed
                        //Todo: change from hard-coded position
                        if(position == 3){
                            Log.d(TAG, "Syncing...");
                            stopService(MQTTIntent);
                            Intent intent = new Intent(MenuMain.this,MQTTService.class);
                            startService(intent);
                        }

                        if (position == 6){
                            Log.d(TAG, "Exiting app");
                            finish();
                            System.exit(0);
                        }
                    }
                });
    }


    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return null;
    }

    @Override
    public void registerDatabaseObserver(DatabaseObserver dbObserver) {
        //register this to DBHandler
        db.setCallBack(dbObserver);
    }

    @Override
    public void unregisterDatabaseObserver(DatabaseObserver dbObserver) {

    }

    @Override
    public void alertStatusChange() {
        if (syncIndex != -1) {
            handler.post(new Runnable () {
                @Override
                public void run() {
                    if (syncIndex != -1) {
                        mItems.get(syncIndex).updateItemName2(String.valueOf(db.getNumRows(false)));
                        String number = mItems.get(syncIndex).getItemName2();
                        //update item_text2 of "Sync Now" object
                        mAdapter.updateHolderTextView(syncIndex, 1);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}
}