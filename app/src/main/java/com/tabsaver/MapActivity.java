package com.tabsaver;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
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
    private ArrayList<HashMap<String, String>> bars = new ArrayList<>();
    private ArrayList<HashMap<String, String>> cities = new ArrayList<>();

    //All of the bar markers on the map
    private List<Marker> markers = new ArrayList<>();

    //Our current location
    private Location myLocation;

    //Keeping track of our state
    private boolean cityLocationUndetermined = true;
    private boolean myLocationUndetermined = true;

    //Storing and retrieving session information
    private ClientSessionManager session;


    private String[] barsListForSearchview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        session = new ClientSessionManager(getApplicationContext());

        //Grab bar and city information
        try {
            //grab bar and cities json
            JSONArray citiesJSON = new JSONArray(session.getCities());
            JSONArray barsJSON = new JSONArray(session.getBars());

            //Setup hashmaps for efficient data access
            setupBarsHashmap(barsJSON);
            setupCitiesHashmap(citiesJSON);

            setUpMapIfNeeded();

            //Load our markers up
            setupBarMarkers();
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
            bar.put("id",  barJSON.getString("id"));
            bar.put("name", barJSON.getString("name"));
            bar.put("lat", barJSON.getString("lat"));
            bar.put("long", barJSON.getString("long"));
            bar.put("deals", barJSON.getString("deals"));

            bars.add(bar);
        }

        setupSearchStringArray();
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
            double longitude = Double.parseDouble(bar.get("long"));
            String name = bar.get("name");

            //Set day
            String day = getDayOfWeekStr();

            //Set deals TODO: Change this comma delimmited shit
            String firstDeal = getFirstDeal(day, bar);

            //Set bar image
            BitmapFactory.Options o = new BitmapFactory.Options();
            Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mug, o);

            //Create the bar marker and place it on the map
            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title(name)
                    .snippet(firstDeal + "...")
                    .icon(BitmapDescriptorFactory.fromBitmap(img)));
            markers.add((m));

            //Listener to zoom to a marker on click
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    float zoom = 18;
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), zoom);
                    mMap.animateCamera(update);
                    marker.showInfoWindow();
                    return true;
                }
            });

            //Listener to load bar detail
            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                @Override
                public void onInfoWindowClick(Marker marker) {
                    // Make new intent to detail view
                    Intent i = new Intent(getApplicationContext(), BarDetail.class);
                    i.putExtra("BarId", getBarIdFromName(marker.getTitle().toString()));
                    startActivity(i);
                }
            });

        }
    }

    public String getBarIdFromName(String barName){
        for(int i = 0; i < bars.size(); i++) {
            String curBarName = bars.get(i).get("name");

            if ( curBarName.equals(barName) ) {
                return bars.get(i).get("id");
            }
        }

        return "";
    }

    public String getFirstDeal(String dayOfWeek, HashMap<String, String> bar){
        JSONObject dealsArray;
        JSONArray todaysDeals;
        try {
            dealsArray = new JSONObject(bar.get("deals"));
            todaysDeals = dealsArray.getJSONArray(dayOfWeek);
            return todaysDeals.getString(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
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

            //Zoom to our current location once.
            if ( cityLocationUndetermined && myLocationUndetermined ) {
                if ( session.getCity().equals("none") ) {
                    determineClosestCity();
                }

                cityLocationUndetermined = false;
                myLocationUndetermined = false;
                zoomToCurrentCity();
            }


        }
    };

    /**
     * Zoom the map to the current city
     */
    private void zoomToCurrentCity(){
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(session.getLat(), session.getLong()), 12.0f));
        cityLocationUndetermined = false;
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
        }
    }

    /**
     * Setup the string for our listview
     */
    private void setupSearchStringArray(){
        barsListForSearchview = new String[bars.size()];

        for(int i = 0; i < bars.size(); i++ ){
            barsListForSearchview[i] = bars.get(i).get("name");
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
        final ArrayAdapterSearchView searchView = (ArrayAdapterSearchView) menu.findItem(R.id.searchList).getActionView();

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.searchview_listitem, barsListForSearchview);
        searchView.setAdapter(adapter);

        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchView.setText(adapter.getItem(position).toString());
            }
        });

        //Setup the actual search view
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search for a bar");
        searchView.setIconifiedByDefault(false);

        //Simple search mechanism..
        searchView.setOnQueryTextListener(new ArrayAdapterSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                for (Marker marker : markers) {
                    String barName = marker.getTitle().toString();
                    if(barName.contains(s) || barName.equals(s)){

                        // Zoom to bar
                        float zoom = 18;
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), zoom);
                        mMap.animateCamera(update);
                        marker.showInfoWindow();
                        break;
                    }
                }

                searchView.clearFocus();
                searchView.setText("");
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
                finish();
                return true;

            //Go to list view
            case R.id.action_list:
                Intent i = new Intent(getApplicationContext(), MainActivity.class);

                //Disabling animation
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(i, 0);
                overridePendingTransition(0, 0);

                startActivity(i);
                finish();
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

//    /**
//     * Appending distances to our bars
//     * @return a JSON String repesenting the bars
//     */
//    public String calculateDistances(){
//        try {
//            //Setup our arrays
//            JSONArray barsWithoutDistances = new JSONArray(session.getBars());
//            JSONArray barsWithDistances = new JSONArray();
//
//            for( int i = 0; i < barsWithoutDistances.length(); i++ ) {
//
//                //Grab bar and setup the barLocation
//                JSONObject bar = barsWithoutDistances.getJSONObject(i);
//                Location barLoc = new Location("Bar");
//                barLoc.setLatitude(bar.getDouble("lat"));
//                barLoc.setLongitude(bar.getDouble("long"));
//
//                //Set location if valid
//                if ( myLocation == null ) {
//                    bar.put("distance", 0.000);
//                } else {
//                    double distance = myLocation.distanceTo(barLoc);
//                    if ( distance == Double.NaN ) {
//                        bar.put("distance", 0.000);
//                    } else {
//                        bar.put("distance", (double) Math.round((distance / 1609) * 1000) / 1000);
//                    }
//                }
//
//                //Add this bar to the array
//                barsWithDistances.put(bar);
//            }
//
//            return barsWithDistances.toString();
//
//        } catch (JSONException e) {
//            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//            return null;
//        }
//    }
}