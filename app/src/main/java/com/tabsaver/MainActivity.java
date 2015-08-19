package com.tabsaver;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    //All bars information
    private ArrayList<HashMap<String, String>> bars;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup the session
        session = new ClientSessionManager(getApplicationContext());

        //Show loader until location is determined
        loader = findViewById(R.id.barLoading);
        loader.setVisibility(View.VISIBLE);

        //Once location is determined, the view will be loaded
        setupLocationTracking();
    }

    @Override
    public void onResume(){
        super.onResume();

        if ( adapter != null ) {
            adapter.filterByDistancePreference();
        }
    }

    private void setupLocationTracking(){
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(getApplicationContext().LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                myLocation = location;
                myLocation.setLongitude(myLocation.getLongitude());

                if ( !myLocationDetermined ) {

                    //Grab bar information from online if we have to. TODO: Add a once daily sync.
                    try {
                        //Setup hash maps for efficient data access
                        setupBarsHashmap();
                        sortBarsByDistance();
                    } catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    displayListView();

                    loader.setVisibility(View.INVISIBLE);
                }

                myLocationDetermined = true;
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
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


                //Setup the distance
                Location barLocation = new Location("");
                barLocation.setLatitude(barJSON.getDouble("lat"));
                barLocation.setLongitude(barJSON.getDouble("long"));
                bar.put("distance", (myLocation.distanceTo(barLocation) / 1609.34) + "");


                // Set the JSON Objects into the array
                bars.add(bar);
            }

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
        searchView.setQueryHint("Type a bar or drink type");
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
}
