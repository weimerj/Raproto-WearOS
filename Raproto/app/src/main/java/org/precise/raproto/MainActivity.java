package org.precise.raproto;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import android.provider.Settings.Secure;



public class MainActivity extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider {

    private List<ListsItem> mItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String android_id = Settings.Secure.getString(MainActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        AmbientModeSupport.attach(this);

        // Create a list of items for adapter to display.
        mItems = new ArrayList<>();
        mItems.add(new ListsItem(getString(R.string.device_id, android_id), 0));
        mItems.add(new ListsItem(getString(R.string.on_off), 2));
        mItems.add(new ListsItem(getString(R.string.sync), 1));
        mItems.add(new ListsItem(getString(R.string.settings), SettingsActivity.class,1));
        mItems.add(new ListsItem(getString(R.string.about), AboutActivity.class, 1));

        // Initialize an adapter and set it to ListView listView.
        ListViewAdapter adapter = new ListViewAdapter(this, mItems);
        final ListView listView = findViewById(R.id.list_view_lists);
        listView.setAdapter(adapter);

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