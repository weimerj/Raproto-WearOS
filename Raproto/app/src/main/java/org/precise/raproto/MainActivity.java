package org.precise.raproto;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider {

    private List<ListsItem> mItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AmbientModeSupport.attach(this);

        // Create a list of items for adapter to display.
        mItems = new ArrayList<>();
        mItems.add(new ListsItem(R.string.app_name));
        mItems.add(new ListsItem(R.string.device_id));
        mItems.add(new ListsItem(R.string.on_off));
        mItems.add(new ListsItem(R.string.sync));
        mItems.add(new ListsItem(R.string.settings));
        mItems.add(new ListsItem(R.string.about));



        // Initialize an adapter and set it to ListView listView.
        ListViewAdapter adapter = new ListViewAdapter(this, mItems);
        final ListView listView = findViewById(R.id.list_view_lists);
        listView.setAdapter(adapter);

    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return null;
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}
}