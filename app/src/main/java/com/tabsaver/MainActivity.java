package com.tabsaver;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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

    //Progress view
    private View loader;

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


        loader = findViewById(R.id.barLoading);
        loader.setVisibility(View.VISIBLE);

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

        refreshListView();
    }

//    public void emulateProgressBar(){
//        ListView mainView = (ListView) findViewById(R.id.listview);
//        ProgressBar showProgress = new ProgressBar(this);
//        showProgress.setVisibility(View.VISIBLE);
//        mainView.addView(showProgress);
//
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

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
        loader.setVisibility(View.VISIBLE);

       // emulateProgressBar();

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
        cities = new ArrayList<>();

        try {
            JSONArray citiesJSON = new JSONArray(session.getCities());

            for (int i = 0; i < citiesJSON.length(); i++) {
                //Grab the bar objects
                HashMap<String, String> city = new HashMap<>();
                JSONObject cityJSON = citiesJSON.getJSONObject(i);

                city.put("name", cityJSON.getString("name"));
                city.put("lat", cityJSON.getString("lat"));
                city.put("long", cityJSON.getString("long"));

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

        for (int i = 0; i < cities.size(); i++) {
            //Setting up our current city
            HashMap<String, String> thisCity = cities.get(i);
            cur.setLatitude(Double.valueOf(thisCity.get("lat")));
            cur.setLongitude(Double.valueOf(thisCity.get("long")));

            if (myLocation.distanceTo(cur) <= myLocation.distanceTo(min)) {
                min.setLatitude(cur.getLatitude());
                min.setLongitude(cur.getLongitude());
                name = thisCity.get("name");
            }
        }

        AnalyticsFunctions.setInstallationCity(name);

        //Set our minimum city in the session
        session.setCity(name, min.getLatitude(), min.getLongitude());
    }


    /**
     * Gesture tracking to refresh the list view
     */
    public void checkGestureForSwipe(MotionEvent event){

        switch (event.getAction()){
            //Record x when we start a motion event
            case MotionEvent.ACTION_DOWN:
                y1 = event.getY();
                break;

            //Record x when we finish, and act if our criteria are met (horizontal change in 35 pixels)
            case MotionEvent.ACTION_UP:
                y2 = event.getY();

                //Check for horizontal movement of 35px (in either direction
                if ( Math.abs(y1 - y2) > SWIPETHRESHOLD ) {

                    //Determine if we went down --> up (y2 < y1) or vice versa
                    if ( y2 > y1 ) {
                        refreshListView();
                        yell("Refreshing Location!");
                    }
                }
        }

    }

    /**
     * Interrupt the normal touch actions so that our activity can observe touch events
     * @param event
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event){

        //If we can't scroll up and we scroll up, refresh the view!
        if ( !listview.canScrollVertically(-1) ) {
            checkGestureForSwipe(event);
        }

        return super.dispatchTouchEvent(event);
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
