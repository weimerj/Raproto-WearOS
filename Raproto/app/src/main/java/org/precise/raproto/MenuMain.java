package org.precise.raproto;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MenuMain extends FragmentActivity implements AmbientModeSupport.AmbientCallbackProvider, DatabaseObserver {

    private ArrayList<ListsItem> mItems = new ArrayList<ListsItem>();

    private ListViewAdapterToggle mAdapter;
    private ListView listView;
    private DatabaseHandler db;
    private final String TAG = "MAIN";
    private int syncIndex = -1;
    private ListsItem syncItem;
    private Handler handler = new Handler();

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_main);

        db = new DatabaseHandler(this);

        registerDatabaseObserver(this);

        String android_id = Settings.Secure.getString(MenuMain.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        SharedPreferences sharedPref = getSharedPreferences("Raproto", Context.MODE_PRIVATE);
        int colorValue = sharedPref.getInt("color", -1);
        if (colorValue == -1) {
            colorValue = Color.parseColor("#0060ff");
            View view = this.getWindow().getDecorView();
            view.setBackgroundColor(colorValue);
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
        mItems.add(new ListsItem(getString(R.string.exit), getString(R.string.save),"2_rows"));

        syncIndex = mItems.indexOf(syncItem);

        // Custom adapter used so we can use custom layout for the rows within the list.
        mAdapter =
                new ListViewAdapterToggle(
                        this,
                        mItems,
                        new ListViewAdapterToggle.SwitchChangeListener() {
                            @SuppressLint("ResourceAsColor")
                            @Override
                            public void onChange(boolean switchOn) {
                                if (switchOn) {
                                    //Todo: Make the toggle switch work not just pressing the list item
                                    View view = getWindow().getDecorView();
                                    //Todo: Change this color of green to match the Tizen App
                                    int color = Color.parseColor("#37803a");
                                    view.setBackgroundColor(color);
                                    startService(sensorIntent);
                                    SharedPreferences sharedPref = MenuMain.this.getSharedPreferences("Raproto",Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putInt("color", color);
                                    editor.apply();

                                } else {
                                    View view = getWindow().getDecorView();
                                    view.setBackgroundColor(Color.BLACK);
                                    SharedPreferences sharedPref = MenuMain.this.getSharedPreferences("Raproto",Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putInt("color", Color.BLACK);
                                    editor.apply();
                                    stopService(sensorIntent);
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