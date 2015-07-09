package com.tabsaver;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends ActionBarActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    //Json representations of cities and bars
    ArrayList<HashMap<String, String>> bars = new ArrayList<>();
    ArrayList<HashMap<String, String>> cities = new ArrayList<>();

    //All of the bar markers on the map
    List<Marker> markers = new ArrayList<>();

    //Our current location
    Location myLocation;

    //Keeping track of our state
    boolean locationUndetermined = true;

    //Storing and retrieving session information
    ClientSessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        session = new ClientSessionManager(getApplicationContext());
        setUpMapIfNeeded();

        //Grab bar and city information from online if we have to. TODO: Add a once daily sync.
        if ( session.getBars().equals("none") ) {
            new DownloadJSON().execute();
        } else {
            try {
                //grab bar and cities json
                JSONArray citiesJSON = new JSONArray(session.getCities());
                JSONArray barsJSON = new JSONArray(session.getBars());

                //Setup hashmaps for efficient data access
                setupBarsHashmap(barsJSON);
                setupCitiesHashmap(citiesJSON);

                //Load our markers up
                setupBarMarkers();
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * Sync our bar and city data with the database
     */
    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            //Grab bar information and store it
            JSONArray barsJSON = JSONFunctions.getJSONfromURL("http://tabsaver.info/retrieveBars.php");
            session.setBars(barsJSON.toString());

            //Grab city information and store it
            JSONArray citiesJSON = JSONFunctions.getJSONfromURL("http://tabsaver.info/retrieveCities.php");
            session.setCities(citiesJSON.toString());

            //Now setup the hashmap for each bar from the JSON
            try {
                setupBarsHashmap(barsJSON);
                setupCitiesHashmap(citiesJSON);
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            setupBarMarkers();
        }
    }

    /**
     * Translate Bars JSON array into something useful
     * @throws JSONException
     */
    public void setupBarsHashmap(JSONArray barsJSON) throws JSONException {
        for (int i = 0; i < barsJSON.length(); i++) {
            //Grab the bar objects
            HashMap<String, String> bar = new HashMap<>();
            JSONObject barJSON = barsJSON.getJSONObject(i);

            //Set all the bar info
            bar.put("id",  barJSON.getString("BarId"));
            bar.put("name", barJSON.getString("name"));
            bar.put("lat", barJSON.getString("lat"));
            bar.put("long", barJSON.getString("long"));
            bar.put("Monday", barJSON.getString("Monday"));
            bar.put("Tuesday", barJSON.getString("Tuesday"));
            bar.put("Wednesday", barJSON.getString("Wednesday"));
            bar.put("Thursday", barJSON.getString("Thursday"));
            bar.put("Friday", barJSON.getString("Friday"));
            bar.put("Saturday", barJSON.getString("Saturday"));
            bar.put("Sunday", barJSON.getString("Sunday"));

            bars.add(bar);
        }

    }

    /**
     * Translate Cities JSON array into something useful
     * @throws JSONException
     */
    public void setupCitiesHashmap(JSONArray citiesJSON) throws JSONException {
        for (int i = 0; i < citiesJSON.length(); i++) {
            //Grab the bar objects
            HashMap<String, String> city = new HashMap<>();
            JSONObject cityJSON = citiesJSON.getJSONObject(i);

            city.put("name", cityJSON.getString("name"));
            city.put("lat", cityJSON.getString("lat"));
            city.put("long", cityJSON.getString("long"));

            cities.add(city);
        }

    }

    /**
     * Setup and place the markers
     */
    public void setupBarMarkers(){

        // Add markers to the map
        for(int i = 0; i < bars.size(); i++){
            // Get the current bar HashMap
            final HashMap<String, String> bar = bars.get(i);

            //Set name and location
            double latitude = Double.parseDouble(bar.get("lat"));
            double longitude = Double.parseDouble("-" + bar.get("long"));
            String name = bar.get("name");

            //Set day
            String day = getDayOfWeekStr();

            //Set deals TODO: Change this comma delimmited shit
            String[] dealArr = bar.get(day).split(",");


            //Set bar image
            BitmapFactory.Options o = new BitmapFactory.Options();
            Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mug, o);

            //Create the bar marker and place it on the map
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title(name)
                    .snippet(dealArr[0])
                    .icon(BitmapDescriptorFactory.fromBitmap(img)));
            markers.add((m));

            //Listener to load bar detail
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                @Override
                public void onInfoWindowClick(Marker marker) {
                    // Make new intent to detail view
                    Intent i = new Intent(getApplicationContext(), BarDetail.class);
                    i.putExtra("BarId", bar.get("id"));
                    startActivity(i);
                }
            });

        }

    }

    /**
     * Determines the day of the week
     * @return The string representation of the current day of the week
     */
    public String getDayOfWeekStr(){

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        switch (day) {
            case Calendar.SUNDAY:
                return "Sunday";
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
        }

        return "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Constantly update our location.
     */
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location  location) {
            myLocation = location;
            myLocation.setLongitude(myLocation.getLongitude() * -1);
        }
    };

    /**
     * Zoom the map to the current city
     */
    private void zoomToCurrentCity(){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(session.getLat(), session.getLong()), 12.0f));
        locationUndetermined = false;
    }

    /**
     * Google maps default config setup
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            //setup our listeners
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationChangeListener(myLocationChangeListener);

            //Zoom to our current location once.
            if ( locationUndetermined ) {
                if ( session.getCity().equals("none") ) {
                    determineClosestCity();
                }

                locationUndetermined = false;
                zoomToCurrentCity();
            }
        }
    }

    /**
     * Setup our menu items
     * @param menu Menu for this page
     * @return Not sure
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.searchList).getActionView();

        //Setup the search view
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search for a bar");
        searchView.setIconifiedByDefault(false);

        //Simple search mechanism.. TODO: Want to do autocomplete recommendations
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                boolean barFound = false;

                for (Marker marker : markers) {
                    String barName = marker.getTitle().toLowerCase().replace("'","");
                    if(barName.contains(s.toLowerCase())){
                        barFound = true;

                        // Zoom to bar
                        float zoom = 18;
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), zoom);
                        mMap.animateCamera(update);
                        marker.showInfoWindow();
                        break;
                    }
                }

                if(!barFound){
                    Toast.makeText(getApplicationContext(), "Bar not found!", Toast.LENGTH_SHORT).show();
                }

                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                return false;
            }

        });

        return true;
    }

    /**
     * Handling menu item selection
     * @param item Selected menu item
     * @return not sure
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            //Go to settings
            case R.id.action_settings:
                Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settings);
                return true;

            //Go to list view
            case R.id.action_list:
                Intent i = new Intent(getApplicationContext(), MainActivity.class);

                //Disabling animation
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(i, 0);
                overridePendingTransition(0, 0);

                //Tack on our distance and show the list
                String distances = calculateDistances();
                session.setBars(distances); //TODO: We are storing distances in sessions which will lead to the distances not updating because the session doesn't update??
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Determining the closest city to us
     */
    public void determineClosestCity()  {
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

        for(int i = 0; i < cities.size(); i++ ) {
            //Setting up our current city
            HashMap<String, String> thisCity = cities.get(i);
            cur.setLatitude(Double.valueOf(thisCity.get("lat")));
            cur.setLongitude(Double.valueOf(thisCity.get("long")));

            if (myLocation.distanceTo(cur) <= myLocation.distanceTo(min) ) {
                min.setLatitude(cur.getLatitude());
                min.setLongitude(cur.getLongitude());
                name = thisCity.get("name");
            }
        }

        //Set our minimum city in the session
        session.setCity(name, min.getLatitude(), min.getLongitude());
    }

    /**
     * Appending distances to our bars
     * @return a JSON String repesenting the bars
     */
    public String calculateDistances(){
        try {
            //Setup our arrays
            JSONArray barsWithoutDistances = new JSONArray(session.getBars());
            JSONArray barsWithDistances = new JSONArray();

            for( int i = 0; i < barsWithoutDistances.length(); i++ ) {

                //Grab bar and setup the barLocation
                JSONObject bar = barsWithoutDistances.getJSONObject(i);
                Location barLoc = new Location("Bar");
                barLoc.setLatitude(bar.getDouble("lat"));
                barLoc.setLongitude(bar.getDouble("long"));

                //Set location if valid
                if ( myLocation == null ) {
                    bar.put("distance", 0.000);
                } else {
                    double distance = myLocation.distanceTo(barLoc);
                    if ( distance == Double.NaN ) {
                        bar.put("distance", 0.000);
                    } else {
                        bar.put("distance", (double) Math.round((distance / 1609) * 1000) / 1000);
                    }
                }

                //Add this bar to the array
                barsWithDistances.put(bar);
            }

            return barsWithDistances.toString();

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }
}