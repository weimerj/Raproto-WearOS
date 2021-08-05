package org.precise.raproto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

import java.util.ArrayList;
import java.util.List;

public class MenuSettings extends FragmentActivity
        implements AmbientModeSupport.AmbientCallbackProvider {

    private List<ListsItem> mItems;

    protected void onCreate(Bundle savedInstanceState) {
        // Get the color preference
        SharedPreferences sharedPref = getSharedPreferences("Raproto", Context.MODE_PRIVATE);
        int colorValue = sharedPref.getInt("color", 0);
        String lastConfig = sharedPref.getString("configTime", "");

        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(colorValue);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        AmbientModeSupport.attach(this);

        // Create a list of items for adapter to display.
        mItems = new ArrayList<>();
        mItems.add(new ListsItem(getString(R.string.configuration), lastConfig, MenuConfiguration.class, "2_rows_arrow"));
        mItems.add(new ListsItem(getString(R.string.MQTT), getString(R.string.broker_address), MenuMQTT.class,"2_rows_arrow"));

        // Initialize an adapter and set it to ListView listView.
        ListViewAdapter adapter = new ListViewAdapter(this, mItems);
        final ListView listView = findViewById(R.id.about_lists);
        listView.setAdapter(adapter);

        // Set header of listView to be the title from title_layout.
        LayoutInflater inflater = LayoutInflater.from(this);
        View titleLayout = inflater.inflate(R.layout.title, null);
        TextView titleView = titleLayout.findViewById(R.id.title_text);
        titleView.setText(R.string.settings);
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
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}
}
