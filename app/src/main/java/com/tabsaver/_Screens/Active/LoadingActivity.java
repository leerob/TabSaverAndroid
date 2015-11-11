package com.tabsaver._Screens.Active;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.tabsaver.Helpers.BarObjectManager;
import com.tabsaver.Helpers.ParseAnalyticsFunctions;
import com.tabsaver.Helpers.ParseDownloadManager;
import com.tabsaver.Helpers.SessionStorage;
import com.tabsaver.R;

import java.util.ArrayList;
import java.util.HashMap;


public class LoadingActivity extends Activity {

    //Setup our parse download manager
    ParseDownloadManager pdm;

    //Storing and retrieving session information
    private SessionStorage session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        //Setup our parse download manager
        pdm = new ParseDownloadManager(this, (TextView) findViewById(R.id.loadingMessage), MainActivity.class);

        //Setup shared preference manager
        session = new SessionStorage(this);

        //Set the closest city and/or retrieve bar info
        if ( session.getCityName().equals("none") ) {
            pdm.getCities();
        } else {
            pdm.getBarsInCity(session.getCity());
            ParseAnalyticsFunctions.verboseLog("App Open", session.getCityName());
        }

    }


}
