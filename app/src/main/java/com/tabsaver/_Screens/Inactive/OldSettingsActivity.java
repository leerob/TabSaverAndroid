package com.tabsaver._Screens.Inactive;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.tabsaver.Helpers.ParseAnalyticsFunctions;
import com.tabsaver.Helpers.ParseDownloadManager;
import com.tabsaver.Helpers.SessionStorage;
import com.tabsaver.R;
import com.tabsaver._Screens.Active.ContactActivity;
import com.tabsaver._Screens.Active.PrivacyPolicy;
import com.tabsaver._Screens.Active.TermsOfService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class OldSettingsActivity extends ActionBarActivity {

    //Textview for the cities setting option
    AutoCompleteTextView location;

    //List of cities
    JSONArray fullCities;

    //Session information
    SessionStorage session;

    CheckBox showClosedBars;
    CheckBox showBarsWithNoDeals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Setup the session
        session = new SessionStorage(getApplicationContext());

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

        location = (AutoCompleteTextView) findViewById(R.id.city_autocomplete);

        //Set our city if it's saved
        if ( !session.getCityName().equals("none") ) {
            location.setText(session.getCityName());
        }

        //Set our autocomplete textview adapter up
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, cities);
        location.setAdapter(adapter);
        location.setThreshold(1);

        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location.showDropDown();
            }
        });

        //Contact Us - On Click Listener
        ((TextView) findViewById(R.id.contact_us)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Update analytics
                ParseAnalyticsFunctions.incrementAndroidAnalyticsValue("ContactUs", "Clicks");

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

        showClosedBars = (CheckBox) findViewById(R.id.showClosedBars);
        showBarsWithNoDeals = (CheckBox) findViewById(R.id.showBarsNoDeals);
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
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
