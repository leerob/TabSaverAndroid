package com.tabsaver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    JSONArray jsonarray;
    ProgressDialog mProgressDialog;
    ArrayList<HashMap<String, String>> arraylist;
    ListView listview;
    ListArrayAdapter adapter;
    HashMap<String, String> barHashMap = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        new DownloadJSON().execute();
        setUpMapIfNeeded();
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

            List<Marker> markers = new ArrayList<Marker>();

            // Add markers to the map
            for(int i = 0; i < arraylist.size(); i++){
                // Get the current bar HashMap
                barHashMap = arraylist.get(i);

                double latitude = Double.parseDouble(barHashMap.get("lat"));
                double longitude = Double.parseDouble("-" + barHashMap.get("long"));
                String name = barHashMap.get("name");

                Log.d("Name", name);
                Log.d("Lat", "" + latitude);
                Log.d("Long", "" + longitude);
                Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(name).snippet("Deals"));
                markers.add((m));
            }
            // Zoom in camera to Ames, will have to have separate arrays of markers to zoom to based on location
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                //i.putExtra(jsonarray);
                startActivity(i);
                return true;
            case R.id.action_settings:
                //openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
