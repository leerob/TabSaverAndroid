package com.tabsaver._Screens.Active;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.tabsaver.Helpers.JSONFunctions;
import com.tabsaver.Helpers.ParseAnalyticsFunctions;
import com.tabsaver.Helpers.ParseDownloadManager;
import com.tabsaver.Helpers.SessionStorage;
import com.tabsaver.R;
import com.tabsaver._Screens.Inactive.AdminActivity;
import com.tabsaver._Screens.Inactive.LoginActivity;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SettingsActivity extends ActionBarActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    //List of cities
    private JSONArray fullCities;

    private HashMap<String, String> previousCity;

    //Session information
    private SessionStorage session;

    private boolean downloading;

    private boolean cityReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_settings);

        //Setup the session
        session = new SessionStorage(this);

        //Grab the previous city for hard resets
        previousCity = session.getCityObject();

        downloading = cityReset = false;

        setupCitiesData();

        //Setup listener for city change
        SharedPreferences sp = getSharedPreferences(SessionStorage.prefName, MODE_PRIVATE);
        sp.registerOnSharedPreferenceChangeListener(this);

        ParseAnalyticsFunctions.verboseLog("Selected", "Settings Menu");
    }

    /**
     * Setting up our settings menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Handle settings menu interactions
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.cancelSettings:
                if ( !downloading ) {
                    finish();
                } else {
                    Toast.makeText(this, "Please wait for downloading to complete", Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setupCitiesData(){
        //Set the city options
        try {
            fullCities = new JSONArray(session.getCities());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Grab the string values for our settings suggestion
        final String[] cities = new String[fullCities.length()];

        for(int i = 0; i < fullCities.length(); i++ ) {
            try {
                JSONObject temp = fullCities.getJSONObject(i);
                cities[i] = temp.getString("name");
            } catch (JSONException e) {
                Toast.makeText(this, "Failed to read cities list.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ( key.equals("city") ) {
            int misCount = 0;

            if ( !cityReset ) {
                //Save the city
                for (int i = 0; i < fullCities.length(); i++) {
                    try {
                        JSONObject curCity = fullCities.getJSONObject(i);

                        if (curCity.getString("name").toLowerCase().equals(sharedPreferences.getString("city", "none").toLowerCase())) {
                            //Update analytics
                            ParseAnalyticsFunctions.incrementAndroidAnalyticsValue("SettingsBasedCityChange", curCity.getString("name"));
                            ParseAnalyticsFunctions.verboseLog("Selected", curCity.getString("name"));

                            //Setup a hashmap because that's what is expected.. There's probably a better way
                            HashMap<String, String> city = new HashMap<>();
                            city.put("name", curCity.getString("name"));
                            city.put("lat", String.valueOf(curCity.getDouble("lat")));
                            city.put("long", String.valueOf(curCity.getDouble("long")));
                            city.put("state", curCity.getString("state"));
                            city.put("taxiService", curCity.getString("taxiService"));
                            city.put("taxiNumber", curCity.getString("taxiNumber"));

                            session.setCity(city);
                            previousCity = city;

                            downloading = true;

                            Toast.makeText(this, "Downloading bars for " + city.get("name"), Toast.LENGTH_SHORT).show();

                            //Async load of city info
                            new DownloadCityTask(this, city.get("name")).execute((Void) null);
                        } else {
                            misCount++;
                        }

                    } catch (JSONException e) {
                        Toast.makeText(this, "Failed to download cities.", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                cityReset = false;
            }

            if ( misCount == fullCities.length() ) {
                Toast.makeText(this, "Not a valid city", Toast.LENGTH_SHORT).show();
                cityReset = true;
                session.setCity(previousCity);
            }

        }

        //Show Closed Bars
        if ( key.equals("showClosedBars") ) {
            ParseAnalyticsFunctions.verboseLog(ParseAnalyticsFunctions.SHOWCLOSEDBARS, String.valueOf(sharedPreferences.getBoolean("showClosedBars", true)));
        }

        //Show bars with no deals
        if ( key.equals("showBarsWithNoDeals") ) {
            ParseAnalyticsFunctions.verboseLog(ParseAnalyticsFunctions.SHOWBARSNODEALS, String.valueOf(sharedPreferences.getBoolean("showBarsWithNoDeals", true)));
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class DownloadCityTask extends AsyncTask<Void, Void, Boolean> {

        private final String mCity;
        private Context mContext;

        DownloadCityTask(Context context, String city) {
            mCity = city;
            mContext = context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //Download the bars for this city
            ParseDownloadManager pdm = new ParseDownloadManager(mContext, null, null);
            pdm.getBarsInCity(mCity);

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            //Alert of success
            Toast.makeText(mContext, "Downloaded city successfully!", Toast.LENGTH_SHORT).show();

            downloading = false;
        }


    }


}
