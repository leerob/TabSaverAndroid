package com.tabsaver._Screens.Active;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tabsaver.Adapters.ArrayAdapterSearchView;
import com.tabsaver.Helpers.BarObjectManager;
import com.tabsaver.Helpers.ParseAnalyticsFunctions;
import com.tabsaver.Helpers.SessionStorage;
import com.tabsaver.Adapters.ListArrayAdapter;
import com.tabsaver.R;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity {

    //All bars information
    private ArrayList<HashMap<String, String>> bars;

    //Listview
    private ListView listview;
    private ListArrayAdapter adapter;

    private SwipeRefreshLayout swipeContainer;

    //Progress view
    private ProgressBar loader;

    //Storing and retrieving session information
    private SessionStorage session;

    //Our current location
    private Location myLocation = null;
    private boolean myLocationDetermined = false;

    //Frequency in which the app should force update = ms * sec * min * hours * day
    private static final int UPDATEFREQUENCY = 1000 * 60 * 60 * 24 * 1;

    private String dayOfWeek;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup the session
        session = new SessionStorage(getApplicationContext());

        //First things first - validate that we're up to date!
        if (shouldUpdate()){
            Intent loader = new Intent(getApplicationContext(), LoadingActivity.class);
            startActivity(loader);
            finish();
        }

        //Grab our swipe refresh and such
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        loader = (ProgressBar) findViewById(R.id.view_loading);

        //Once location is determined, the view will be loaded
        setupLocationTracking();

        //Setup our swipe refresh
        setupSwipeRefresh();

        //Drop in the tabsaver logo
        setIconAsLogo();

        this.dayOfWeek = "today";
    }

    /**
     * Add logo to action bar
     */
    public void setIconAsLogo(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_mug);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    }

    public void setupSwipeRefresh(){
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                refreshListView();
            }
        });

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    /**
     * Establishes location listener so that the location updates
     */
    private void setupLocationTracking(){

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(getApplicationContext().LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                myLocation = location;

                if ( !myLocationDetermined ) {
                    myLocationDetermined = true;
                    refreshListView();
                }

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        try {
            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        } catch (IllegalArgumentException e ) {
            yell("Unable to access GPS, location services will not work.");
        }
    }

    /**
     * Determines if the app should force a bar deal update
     * @return
     */
    public boolean shouldUpdate(){
        Long lastUpdateTime = session.getLastUpdateTime();
        return lastUpdateTime + UPDATEFREQUENCY <= System.currentTimeMillis();
    }

    /**
     * Refreshes the distance for each bar!
     */
    public void refreshListView(){

        //display everything
        try {
            //Setup the bars hashmap
            bars = BarObjectManager.setupBarsHashmap(getApplicationContext(), myLocation);

            if ( myLocationDetermined ) {
                sortBarsByDistance();
            }

            displayListView();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        swipeContainer.setRefreshing(false);
        loader.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onResume(){
        super.onResume();

        refreshListView();
    }

    public void showDayDialog(View view){
        AlertDialog.Builder b = new AlertDialog.Builder(this);

        b.setTitle("Choose A day");
        //b.setMessage("Want to see deals for a different day? Go ahead and choose below!");

        b.setIcon(R.drawable.ic_mug);

        final String[] types = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        b.setItems(types, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dayOfWeek = types[which];
                refreshListView();
                dialog.dismiss();
            }

        });

        b.show();
    }

    /**
     * Button to handle the tax service uses
     * @param view
     */
    public void hailCab(View view){
        //Update analytics
        ParseAnalyticsFunctions.incrementAndroidAnalyticsValue("Taxi", "Clicks");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        //Set our title
        builder.setTitle("Need A Cab?");

        //Set our taxi icon
        builder.setIcon(R.drawable.ic_taxi);

        // If they have a city and taxi, prompt for it. Else prompt them to set their location!
        if ( !session.getTaxiName().equals("none")) {
            //Set our message
            builder.setMessage("Do you want us to call you a taxi? We partner with " + session.getTaxiName() + " of " + session.getCityName() + " to get you home safe.");
        } else {
            //Set our message
            builder.setMessage("You haven't set a city! You'll need to set one in the settings menu");
        }

        //If their taxi service was set, use it. Otherwise navigate to the settings menu
        if ( !session.getTaxiName().equals("none")) {
            //OnConfirm
            builder.setPositiveButton(R.string.take_me_home, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Update analytics
                    ParseAnalyticsFunctions.incrementAndroidAnalyticsValue("Taxi", "Calls");

                    //Parse phone number, send off the call to the taxi service
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + session.getTaxiNumber()));
                    startActivity(intent);


                }
            });
        } else {
            //OnConfirm
            builder.setPositiveButton(R.string.go_to_settings, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    //Navigate to settings
                    Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(settings);
                }
            });

        }


        //OnCancel
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });

        // Create the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /**
     * Add each of our items to the view
     */
    public void displayListView() {
        listview = (ListView) findViewById(R.id.listview);
        adapter = new ListArrayAdapter(MainActivity.this, bars, dayOfWeek);
        listview.setAdapter(adapter);
        listview.setEmptyView(findViewById(R.id.emptyListViewText));
    }

    /**
     * Sorts the bars by distance
     * //TODO: Inplace sort?
     */
    public void sortBarsByDistance(){
        //Sort the list
        ArrayList<HashMap<String, String>> sortedListByDistance = new ArrayList<>();

        while (!bars.isEmpty()) {
            HashMap<String, String> closestBar = bars.get(0);

            for (int j = 0; j < bars.size(); j++) {
                HashMap<String, String> currentBar = bars.get(j);

                if (Double.valueOf(closestBar.get("distance")) >= Double.valueOf(currentBar.get("distance"))) {
                    closestBar = currentBar;
                }

            }

            bars.remove(closestBar);
            sortedListByDistance.add(closestBar);
        }

        bars.addAll(sortedListByDistance);
    }

    /**
     * Setting up our settings menu
     * @param menu Menu for this page
     * @return Some boolean (idk)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_view, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final ArrayAdapterSearchView searchView = (ArrayAdapterSearchView) menu.findItem(R.id.search).getActionView();

        //Setting up the search view
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search Drinks & Bars");
        searchView.setIconifiedByDefault(false);

        searchView.setOnQueryTextListener(new ArrayAdapterSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //Update view with filter
                adapter.filter(s);
                return false;
            }

        });

        return true;
    }

    /**
     * Handle settings menu interactions
     * @param item Selected menu item
     * @return some boolean?
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settings);
                return true;
            case R.id.showMapView:
                Intent map = new Intent(getApplicationContext(), MapActivity.class);

                //Disabling animation for the transition
                map.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(map, 0);
                overridePendingTransition(0, 0); //0 for no animation

                startActivity(map);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void yell(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
