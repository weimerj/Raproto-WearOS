package org.precise.raproto;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.wear.ambient.AmbientModeSupport;

public class ScreenInfo extends FragmentActivity implements
        AmbientModeSupport.AmbientCallbackProvider {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);

        // Get the color preference
        SharedPreferences sharedPref = getSharedPreferences("RaprotoColorFile", Context.MODE_PRIVATE);
        int colorValue = sharedPref.getInt("color", 0);
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(colorValue);

        AmbientModeSupport.attach(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public AmbientModeSupport.AmbientCallback getAmbientCallback() {
        return new MyAmbientCallback();
    }

    private class MyAmbientCallback extends AmbientModeSupport.AmbientCallback {}
}