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
    JSONArray jsonarray;
    JSONArray cities;

    //More accessible collection of bar data
    ArrayList<HashMap<String, String>> arraylist = new ArrayList<HashMap<String, String>>();

    //All of the bar markers on the map
    List<Marker> markers = new ArrayList<Marker>();

    //Our current location
    Location myLocation;

    //Keeping track of our state
    boolean locationUndetermined = true;
    boolean markersLoaded = false;

    //Storing and retrieving session information
    ClientSessionManager session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        session = new ClientSessionManager(getApplicationContext());
        setUpMapIfNeeded();


        //Try and grab our closest city from sessions
        if ( !session.getCityName().equals("none") ) {
            zoomToCurrentCity();
            locationUndetermined = false;
        }

        //Grab bar and city information online if we have to. TODO: Add a once daily sync.
        if ( session.getBars().equals("none") ) {
            new DownloadJSON().execute();
        } else {
            try {
                //grab json
                jsonarray = new JSONArray(session.getBars());
                cities = new JSONArray(session.getCities());

                //setup map
                setupBarsArraylist();
                setupBarMarkers();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Sync our bar and city data with the online database
     */
    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // Retrieve JSON Objects from the given URL address and store them.
            jsonarray = JSONFunctions.getJSONfromURL("http://tabsaver.info/retrieveBars.php");
            session.setBars(jsonarray.toString());

            cities = JSONFunctions.getJSONfromURL("http://tabsaver.info/retrieveCities.php");
            session.setCities(cities.toString());

            //Now setup the hashmap for each bar from the JSON
            try {
                setupBarsArraylist();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void args) {
            setupBarMarkers();
        }
    }

    /**
     * Translate JSON array into something useful and efficient
     * @throws JSONException
     */
    public void setupBarsArraylist() throws JSONException {
        for (int i = 0; i < jsonarray.length(); i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            JSONObject obj = jsonarray.getJSONObject(i);
            // Retrieve JSON Objects
            map.put("id",  String.valueOf(i));
            map.put("name", obj.getString("name"));
            map.put("lat", obj.getString("lat"));
            map.put("long", obj.getString("long"));
            map.put("Monday", obj.getString("Monday"));
            map.put("Tuesday", obj.getString("Tuesday"));
            map.put("Wednesday", obj.getString("Wednesday"));
            map.put("Thursday", obj.getString("Thursday"));
            map.put("Friday", obj.getString("Friday"));
            map.put("Saturday", obj.getString("Saturday"));
            map.put("Sunday", obj.getString("Sunday"));

            // Set the JSON Objects into the array
            arraylist.add(map);
        }

    }

    /**
     * Setup and place the markers
     */
    public void setupBarMarkers(){
        String[] bars = new String[arraylist.size()];

        // Add markers to the map
        for(int i = 0; i < arraylist.size(); i++){
            // Get the current bar HashMap
            HashMap<String, String> barHashMap = arraylist.get(i);

            double latitude = Double.parseDouble(barHashMap.get("lat"));
            double longitude = Double.parseDouble("-" + barHashMap.get("long"));
            String name = barHashMap.get("name");

            String day = getDayOfWeekStr();
            String[] dealArr = barHashMap.get(day).split(",");

            bars[i] = name;

            BitmapFactory.Options o = new BitmapFactory.Options();
            Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mug, o);

            Marker m = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title(name)
                    .snippet(dealArr[0])
                    .icon(BitmapDescriptorFactory.fromBitmap(img)));
            markers.add((m));

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

                @Override
                public void onInfoWindowClick(Marker marker) {
                    // Make new intent to detail view
                    Intent i = new Intent(getApplicationContext(), BarDetail.class);
                    i.putExtra("jsonArray", jsonarray.toString());
                    i.putExtra("bar", marker.getTitle());
                    startActivity(i);
                }
            });

        }

        markersLoaded = true;
    }

    /**
     * Determines the day of the week
     * @return
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

            if ( locationUndetermined && markersLoaded) {
                determineClosestCity();
                zoomToCurrentCity();
            }
        }
    };

    /**
     * Zoom the map to the current city once
     */
    private void zoomToCurrentCity(){
//        LatLng currentCityLocation = new LatLng(session.getLat(), session.getLong());
//        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(currentCityLocation, 10);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(session.getLat(), session.getLong()), 10.0f));

//        mMap.animateCamera(update);

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
        }
    }

    /**
     * Setup our menu items
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        //This is close - but not quite right - on replacing the grey icon.
        //This is actually a non-public attribute (that little grey search icon in the SearchView) and can't be changed
        //through typical/simple means
//        int searchIconId = searchView.getContext().getResources().
//                getIdentifier("android:id/search_button", null, null);
//        ImageView searchIcon = (ImageView) searchView.findViewById(searchIconId);
//        searchIcon.setImageResource(R.drawable.ic_search);

        searchView.setQueryHint("Search for a bar");
        searchView.setIconifiedByDefault(false);


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
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settings);
                return true;
            case R.id.action_list:
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                String distances = addDistances(jsonarray);
                session.setBars(distances);
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

        //Error handling
        if ( session.getCityName().equals("none") || cities.length() == 0) {
            return;
        }

        try {

            Location cur = new Location("BS");
            Location min = new Location("BS");
            String name = "None";
            int minIndex = 0;

            JSONObject o = cities.getJSONObject(0);
            cur.setLatitude(o.getDouble("lat"));
            cur.setLongitude(o.getDouble("long"));
            min.setLatitude(o.getDouble("lat"));
            min.setLongitude(o.getDouble("long"));

            for(int i = 0; i < cities.length(); i++ ) {
                o = cities.getJSONObject(i);
                cur.setLatitude(o.getDouble("lat"));
                cur.setLongitude(o.getDouble("long"));

                if (myLocation.distanceTo(cur) <= myLocation.distanceTo(min) ) {
                    minIndex = i;
                    min.setLatitude(cur.getLatitude());
                    min.setLongitude(cur.getLongitude());
                    name = o.getString("name");
                }
            }

            min.setLongitude(min.getLongitude());

            session.setCity(name, min.getLatitude(), min.getLongitude());

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Failed to determine closest city.", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Appending distances to our bars.. TODO: Can we move this into another method?
     * @param json
     * @return
     */
    public String addDistances(JSONArray json){
        JSONArray newArray = new JSONArray();

        for( int i = 0; i < json.length(); i++ ) {
            try {
                JSONObject obj = json.getJSONObject(i);
                Location barLoc = new Location("Bar");
                barLoc.setLatitude(Double.valueOf(obj.getString("lat")));
                barLoc.setLongitude(Double.valueOf(obj.getString("long")));
                if ( myLocation == null ) {
                    obj.put("distance", 0.000);
                } else {
                    double distance = myLocation.distanceTo(barLoc);
                    if ( distance == Double.NaN ) {
                        obj.put("distance", 0.000);
                    } else {
                        obj.put("distance", (double) Math.round((distance / 1609) * 1000) / 1000);
                    }
                }
                newArray.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        return newArray.toString();
    }
}