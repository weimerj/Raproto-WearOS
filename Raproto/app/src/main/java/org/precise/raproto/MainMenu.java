package org.precise.raproto;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;
import androidx.wear.widget.drawer.WearableActionDrawerView;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainMenu extends FragmentActivity implements AmbientModeSupport.AmbientCallbackProvider {

    private List<ListsItem> mItems;
    private ListViewAdapterToggle mAdapter;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);
        String android_id = Settings.Secure.getString(MainMenu.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        AmbientModeSupport.attach(this);

        // Create a list of items for adapter to display.
        mItems = new ArrayList<>();
        mItems.add(new ListsItem(getString(R.string.device_id), android_id, "2_rows"));
        mItems.add(new ListsItem(getString(R.string.on_off), "toggle"));
        mItems.add(new ListsItem(getString(R.string.sync), getString(R.string.messages),"2_rows"));
        mItems.add(new ListsItem(getString(R.string.settings), SettingsMenu.class,"arrow"));
        mItems.add(new ListsItem(getString(R.string.about), AboutMenu.class, "arrow"));
        mItems.add(new ListsItem(getString(R.string.exit), getString(R.string.save),"2_rows"));

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
                                    Toast.makeText(getApplicationContext(),"Collecting Sensor Data",Toast.LENGTH_SHORT).show();
                                    View view = getWindow().getDecorView();
                                    view.setBackgroundColor(Color.parseColor("#37803a"));
                                } else {
                                    View view = getWindow().getDecorView();
                                    view.setBackgroundColor(Color.BLACK);
                                }
                            }
                        });

        // Initialize an adapter and set it to ListView listView.
        //ListViewAdapter adapter = new ListViewAdapter(this, mItems);
        final ListView listView = findViewById(R.id.list_view_lists);
        listView.setAdapter(mAdapter);

        // Set header of listView to be the title from title_layout.
        LayoutInflater inflater = LayoutInflater.from(this);
        View titleLayout = inflater.inflate(R.layout.title_layout, null);
        TextView titleView = titleLayout.findViewById(R.id.title_text);
        titleView.setText(R.string.app_name);
        titleView.setOnClickListener(null); // make title non-clickable.

        listView.addHeaderView(titleView);

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

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}
}