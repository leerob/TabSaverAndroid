package com.tabsaver;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity {

    //All bars information
    private ArrayList<HashMap<String, String>> bars;

    //All cities
    private ArrayList<HashMap<String, String>> cities;

    //Listview
    private ListView listview;
    private ListArrayAdapter adapter;

    private SwipeRefreshLayout swipeContainer;

    //Progress view
    private ProgressBar loader;

    //Storing and retrieving session information
    private ClientSessionManager session;

    //Our current location
    private Location myLocation;
    private boolean myLocationDetermined = false;
    private long lastTimeLocationUpdated;

    //Frequency in which the app should force update = ms * sec * min * hours * day
    private static final int UPDATEFREQUENCY = 1000 * 60 * 60 * 24 * 1;

    //Gesture tracking variables
    float y1, y2 = 0;
    static final float SWIPETHRESHOLD = 35;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Grab our swipe refresh and such
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        loader = (ProgressBar) findViewById(R.id.view_loading);

        //Setup the session
        session = new ClientSessionManager(getApplicationContext());

        //First things first - validate that we're up to date!
        if (shouldUpdate()){
            Intent loader = new Intent(getApplicationContext(), LoadingActivity.class);
            startActivity(loader);
            finish();
        }

        //Once location is determined, the view will be loaded
        setupLocationTracking();

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

        //Refresh swipe container
        swipeContainer.setRefreshing(true);
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
            setupBarsHashmap();

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

        if ( adapter != null ) {
            adapter.filterByDistancePreference();
        }
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
                    lastTimeLocationUpdated = System.currentTimeMillis();

                    if ( session.getCity().equals("none") ) {
                        setupCitiesHashmap();
                        determineClosestCity();
                    }
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
     * Button to handle the tax service uses
     * @param view
     */
    public void hailCab(View view){
        //Update analytics
        AnalyticsFunctions.incrementAndroidAnalyticsValue("Taxi", "Clicks");

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

        //OnConfirm
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //If their taxi service was set, use it. Otherwise navigate to the settings menu
                if ( !session.getTaxiName().equals("none")) {
                    //Update analytics
                    AnalyticsFunctions.incrementAndroidAnalyticsValue("Taxi", "Calls");

                    //Parse phone number, send off the call to the taxi service
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + session.getTaxiNumber()));
                    startActivity(intent);
                } else {
                    //Navigate to settings
                    Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(settings);
                }

            }
        });

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
        adapter = new ListArrayAdapter(MainActivity.this, bars);
        listview.setAdapter(adapter);
        listview.setEmptyView((TextView) findViewById(R.id.emptyListViewText));
    }

    /**
     * Create a hashmap representation of all of our bars
     * @throws JSONException
     */
    public void setupBarsHashmap() throws JSONException {
        JSONArray barsJSON = new JSONArray(session.getBars());

       bars = new ArrayList<>();

        //Setup the bar info
        try {
            for (int i = 0; i < barsJSON.length(); i++) {
                HashMap<String, String> bar = new HashMap<>();
                JSONObject barJSON = barsJSON.getJSONObject(i);

                // Retrieve JSON Objects
                bar.put("id",  barJSON.getString("id"));
                bar.put("name", barJSON.getString("name"));

                //Put bars and hours
                bar.put("hours", barJSON.getString("hours"));
                bar.put("deals", barJSON.getString("deals"));


                if ( myLocationDetermined ) {
                    //Setup the distance
                    Location barLocation = new Location("");
                    barLocation.setLatitude(barJSON.getDouble("lat"));
                    barLocation.setLongitude(barJSON.getDouble("long"));
                    bar.put("distance", (myLocation.distanceTo(barLocation) / 1609.34) + "");
                } else {
                    bar.put("distance", "0.0");
                }

                // Set the JSON Objects into the array
                bars.add(bar);
            }

        } catch (JSONException e) {
            yell(e.getMessage());
        }
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
     * Translate Cities JSON array into something useful
     *
     * @throws JSONException
     */
    public void setupCitiesHashmap() {

        try {
            JSONArray citiesJSON = new JSONArray(session.getCities());

            for (int i = 0; i < citiesJSON.length(); i++) {
                //Grab the bar objects
                HashMap<String, String> city = new HashMap<>();
                JSONObject cityJSON = citiesJSON.getJSONObject(i);

                city.put("name", cityJSON.getString("name"));
                city.put("lat", cityJSON.getString("lat"));
                city.put("long", cityJSON.getString("long"));
                city.put("taxiService", cityJSON.getString("taxiService"));
                city.put("taxiNumber", cityJSON.getString("taxiNumber"));

                cities.add(city);
            }
        } catch (JSONException e ) {
            yell(e.getMessage());
        }

    }

    /**
     * Determining the closest city to us
     */
    public void determineClosestCity() {
        //Current location
        Location cur = new Location("BS");

        //Minimum location
        Location min = new Location("BS");

        //City name
        String name = "None";

        //Set our minimum city to the first
        HashMap<String, String> city = cities.get(0);
        min.setLatitude(Double.valueOf(city.get("lat")));
        min.setLongitude(Double.valueOf(city.get("long")));

        HashMap<String, String> minCity = new HashMap<>();
        for (int i = 0; i < cities.size(); i++) {
            //Setting up our current city
            HashMap<String, String> thisCity = cities.get(i);
            cur.setLatitude(Double.valueOf(thisCity.get("lat")));
            cur.setLongitude(Double.valueOf(thisCity.get("long")));

            if (myLocation.distanceTo(cur) <= myLocation.distanceTo(min)) {
                min.setLatitude(cur.getLatitude());
                min.setLongitude(cur.getLongitude());

                minCity = thisCity;
            }
        }

        AnalyticsFunctions.incrementAndroidAnalyticsValue("LocationBasedCityChange", name);

        //Set our minimum city in the session
        session.setCity(minCity.get("name"), min.getLatitude(), min.getLongitude(), minCity.get("taxiService"), minCity.get("taxiNumber"));
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
