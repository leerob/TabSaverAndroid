package com.tabsaver._Screens.Active;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.tabsaver.Helpers.ParseAnalyticsFunctions;
import com.tabsaver.Helpers.ParseDownloadManager;
import com.tabsaver.Helpers.SessionStorage;
import com.tabsaver.R;
import com.tabsaver._Screens.Extensions.AutoCompleteEditTextPreference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * A placeholder fragment containing a simple view.
 */
public class SettingsActivityFragment extends PreferenceFragment {


    public SettingsActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(SessionStorage.prefName);
        prefMgr.setSharedPreferencesMode(Context.MODE_PRIVATE);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.settings_preferences);
    }

}
