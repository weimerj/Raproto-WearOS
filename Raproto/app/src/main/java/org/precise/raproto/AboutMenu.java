package org.precise.raproto;

import android.content.res.Resources;
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
import org.precise.raproto.BuildConfig;


public class AboutMenu extends FragmentActivity
        implements AmbientModeSupport.AmbientCallbackProvider {

    private List<ListsItem> mItems;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_menu);

        AmbientModeSupport.attach(this);

        String versionName = BuildConfig.VERSION_NAME;

        Resources res = getResources();
        // Create a list of items for adapter to display.
        mItems = new ArrayList<>();
        mItems.add(new ListsItem(getString(R.string.version, versionName), "text_only"));
        mItems.add(new ListsItem(getString(R.string.info), Info.class,"arrow"));
        mItems.add(new ListsItem(getString(R.string.disclamer), Disclamer.class, "arrow"));
        mItems.add(new ListsItem(getString(R.string.help), Help.class, "arrow"));

        // Initialize an adapter and set it to ListView listView.
        ListViewAdapter adapter = new ListViewAdapter(this, mItems);
        final ListView listView = findViewById(R.id.list_view_lists);
        listView.setAdapter(adapter);

        // Set header of listView to be the title from title_layout.
        LayoutInflater inflater = LayoutInflater.from(this);
        View titleLayout = inflater.inflate(R.layout.title_layout, null);
        TextView titleView = titleLayout.findViewById(R.id.title_text);
        titleView.setText(R.string.about);
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