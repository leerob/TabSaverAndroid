package com.tabsaver;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class SettingsActivity extends ActionBarActivity {

    //Textview for the cities setting option
    AutoCompleteTextView location;

    //List of cities
    JSONArray fullCities;

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
                Intent clientLogin = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(clientLogin);
            }
        });

        //Contact Us - On Click Listener
        ((TextView) findViewById(R.id.contact_us)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent contactUs = new Intent(getApplicationContext(), ContactActivity.class);
                startActivity(contactUs);
            }
        });

        //Saving our settings
        ((Button) findViewById(R.id.save_settings)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                for(int i = 0; i < fullCities.length(); i++ ){
                    try {
                        JSONObject temp  = fullCities.getJSONObject(i);

                        if ( temp.getString("name").toLowerCase().contains(location.getText().toString().toLowerCase()) ) {
                              session.setCity(temp.getString("name"), temp.getDouble("lat"), temp.getDouble("long"));
                        }

                    } catch (JSONException e) {
                        Toast.makeText(SettingsActivity.this, "Failed to save settings.. Is a city selected?", Toast.LENGTH_SHORT).show();
                    }
                }

                Toast.makeText(SettingsActivity.this, "Saved settings!", Toast.LENGTH_SHORT).show();

                Intent map = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(map);
            }
        });
    }

}
