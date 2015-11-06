package com.tabsaver._Screens.Active;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.tabsaver.Helpers.ParseAnalyticsFunctions;
import com.tabsaver.R;


public class PrivacyPolicy extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        ParseAnalyticsFunctions.verboseLog("Selected", "Privacy Policy");
    }

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
