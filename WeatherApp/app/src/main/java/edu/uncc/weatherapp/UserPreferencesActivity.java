package edu.uncc.weatherapp;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class UserPreferencesActivity extends PreferenceActivity {
    SharedPreferences shareprefs;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        shareprefs = PreferenceManager.getDefaultSharedPreferences(this);
        Resources resources = getResources();
    }
}
