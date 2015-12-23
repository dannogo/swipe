package com.sqisland.swipe;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

/**
 * Created by oleh on 12/23/15.
 */
public class App extends Application{

    protected static SharedPreferences sharedPreferences;
    protected static SharedPreferences.Editor editor;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        editor = sharedPreferences.edit();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

}