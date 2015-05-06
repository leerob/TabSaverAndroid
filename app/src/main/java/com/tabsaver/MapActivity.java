package com.tabsaver;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.Parse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends ActionBarActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    JSONArray jsonarray;
    ProgressDialog mProgressDialog;
    ArrayList<HashMap<String, String>> arraylist;
    ListView listview;
    ListArrayAdapter adapter;
    HashMap<String, String> barHashMap = new HashMap<String, String>();
    List<Marker> markers = new ArrayList<Marker>();
    Location myLocation;
    Marker myLoc;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setUpMapIfNeeded();
        mProgressDialog = null;
        new DownloadJSON().execute();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(getApplicationContext(), "mZ1wJCdlDowI28IzRpZ9ycIFkm0TXUYA33EoC3n8", "4TaNynj1NN0UDlXMP3iQQb6WGAAE5Gp9IOBcVMkW");

    }

    // DownloadJSON AsyncTask
    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a Progress Dialog
            mProgressDialog = new ProgressDialog(MapActivity.this);
            mProgressDialog.setTitle("Downloading Bar Data");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Create an array
            arraylist = new ArrayList<HashMap<String, String>>();

            try {
                // Retrieve JSON Objects from the given URL address
                jsonarray = JSONFunctions.getJSONfromURL("http://tabsaver.info/connectAmes.php");

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

            } catch (JSONException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void args) {

            // Add markers to the map
            for(int i = 0; i < arraylist.size(); i++){
                // Get the current bar HashMap
                barHashMap = arraylist.get(i);

                double latitude = Double.parseDouble(barHashMap.get("lat"));
                double longitude = Double.parseDouble("-" + barHashMap.get("long"));
                String name = barHashMap.get("name");

                String day = getDayOfWeekStr();
                String[] dealArr = barHashMap.get(day).split(",");


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
            // Zoom in camera to Ames, will have to have separate arrays of markers to zoom to based on location
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

            if(mProgressDialog != null)
                mProgressDialog.dismiss();
        }
    }

    @Override
    public void onPause(){

        super.onPause();
        if(mProgressDialog != null)
            mProgressDialog.dismiss();
    }

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

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location  location) {
            myLocation = location;
            myLocation.setLongitude(myLocation.getLongitude() * -1);
        }
    };

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

            mMap.setMyLocationEnabled(true);

            mMap.setOnMyLocationChangeListener(myLocationChangeListener);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }

        }
    }

    private void setUpMap() {
        // Zoom in, animating the camera.
        //mMap.animateCamera(CameraUpdateFactory.zoomIn());

        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setQueryHint("Search for a bar");
        searchView.setIconifiedByDefault(false);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                for (Marker marker : markers) {
                    if(marker.getTitle().toLowerCase().contains(s)){
                        Toast.makeText(getApplicationContext(), "Found bar: " + marker.getTitle(), Toast.LENGTH_SHORT).show();
                        float zoom = 18;
                        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(marker.getPosition(), zoom);
                        mMap.animateCamera(update);
                        marker.showInfoWindow();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Bar not found!", Toast.LENGTH_SHORT).show();
                    }
                }
                searchView.clearFocus();
                // Zoom to bar
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }

        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_list:
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                String test = addDistances(jsonarray);
                i.putExtra("jsonArray", test);
                startActivity(i);
                return true;
            case R.id.action_client:
                Intent clientLogin = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(clientLogin);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

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