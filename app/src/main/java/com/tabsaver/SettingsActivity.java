package com.tabsaver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SettingsActivity extends ActionBarActivity {

    //Textview for the cities setting option
    AutoCompleteTextView location;

    //List of cities
    JSONArray fullCities;
    int distancePreference;

    //Session information
    ClientSessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Setup the session
        session = new ClientSessionManager(getApplicationContext());

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
                Toast.makeText(getApplicationContext(), "Failed to read cities list.", Toast.LENGTH_SHORT).show();
            }

        }

        location = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView1);


        //Set our city if it's saved
        if ( !session.getCityName().equals("none") ) {
            location.setText(session.getCityName());
        }

        //Set our autocomplete textview adapter up
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, cities);
        location.setAdapter(adapter);
        location.setThreshold(1);

        //Client Login- On Click Listener
        ((TextView) findViewById(R.id.client_login)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Update analytics
                AnalyticsFunctions.incrementAndroidAnalyticsValue("ClientLogin", "Clicks");

                Intent clientLogin = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(clientLogin);
            }
        });

        //Contact Us - On Click Listener
        ((TextView) findViewById(R.id.contact_us)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Update analytics
                AnalyticsFunctions.incrementAndroidAnalyticsValue("ContactUs","Clicks");

                //Navigate to intent
                Intent contactUs = new Intent(getApplicationContext(), ContactActivity.class);
                startActivity(contactUs);
            }
        });

        //ToS - On Click Listener
        ((TextView) findViewById(R.id.tos)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Navigate to intent
                Intent termsOfService = new Intent(getApplicationContext(), TermsOfService.class);
                startActivity(termsOfService);
            }
        });

        //PP - On Click Listener
        ((TextView) findViewById(R.id.pp)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent privacyPolicy = new Intent(getApplicationContext(), PrivacyPolicy.class);
                startActivity(privacyPolicy);
            }
        });

        //Setup the distance seekBar listener
        ((SeekBar)findViewById(R.id.locationDistanceBar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //Update value and view
                ((TextView) findViewById(R.id.currentDistanceDisplay)).setText(progress + " mi");
                distancePreference = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //Set the distance seekbar default value
        ((SeekBar) findViewById(R.id.locationDistanceBar)).setProgress(session.getDistancePreference());
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
            case R.id.saveSettings:

                //Save the city
                for(int i = 0; i < fullCities.length(); i++ ){
                    try {
                        JSONObject temp  = fullCities.getJSONObject(i);

                        if ( temp.getString("name").toLowerCase().contains(location.getText().toString().toLowerCase()) ) {
                            //Update analytics
                            AnalyticsFunctions.incrementAndroidAnalyticsValue("SettingsBasedCityChange", temp.getString("name"));

                            //Set city location
                            session.setCity(temp.getString("name"), temp.getDouble("lat"), temp.getDouble("long"), temp.getString("taxiService"), temp.getString("taxiNumber"));
                        }

                    } catch (JSONException e) {
                        Toast.makeText(SettingsActivity.this, "Failed to save settings.. Is a city selected?", Toast.LENGTH_SHORT).show();
                    }
                }

                //Save the distance
                session.setDistancePreference(distancePreference);

                //Alert of success
                Toast.makeText(SettingsActivity.this, "Saved settings!", Toast.LENGTH_SHORT).show();

                finish();
                return true;
            case R.id.cancelSettings:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
