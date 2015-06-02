package com.tabsaver;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
    }

//Removed settings bar in settings menu.

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_settings, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
