package com.tabsaver._Screens.Active;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.tabsaver.Adapters.ArrayAdapterSearchView;
import com.tabsaver.Adapters.CustomMapsWindowAdapter;
import com.tabsaver.Helpers.BarObjectManager;
import com.tabsaver.Helpers.ParseAnalyticsFunctions;
import com.tabsaver.Helpers.SessionStorage;
import com.tabsaver.R;
import com.tabsaver._Screens.Inactive.OldSettingsActivity;
import com.tabsaver._Screens.Extensions.TabsaverActionBarActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class MapActivity extends TabsaverActionBarActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    //Json representations of cities and bars
    private ArrayList<HashMap<String, String>> bars = new ArrayList<>();

    //All of the bar markers on the map
    private List<Marker> markers = new ArrayList<>();

    //Our current location
    private Location myLocation = null;

    //Keeping track of our state
    private boolean myLocationDetermined = false;

    //Storing and retrieving session information
    private SessionStorage session;

    //The bars list to show up in the search view (Strings instead of the arraylist)
    private String[] barsListForSearchview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        session = new SessionStorage(getApplicationContext());

        super.setIconAsLogo(this);

        //Grab bar and city information
        try {
            //setup bars and cities datastructures
            bars = BarObjectManager.setupBarsHashmap(getApplicationContext(), myLocation);
            setupSearchStringArray();

            setUpMapIfNeeded();

            //Load our markers up
            setupBarMarkers();
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * Setup and place the markers
     */
    public void setupBarMarkers() {

        // Add markers to the map
        for (int i = 0; i < bars.size(); i++) {
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
                    String barId = getBarIdFromName(marker.getTitle().toString());
                    // Make new intent to detail view
                    Intent i = new Intent(getApplicationContext(), BarDetail.class);
                    i.putExtra("BarId", barId);

                    //Update analytics
                    ParseAnalyticsFunctions.incrementBarClickThrough(barId);

                    //Move on
                    startActivity(i);
                }
            });

        }
    }

    public String getBarIdFromName(String barName) {
        for (int i = 0; i < bars.size(); i++) {
            String curBarName = bars.get(i).get("name");

            if (curBarName.equals(barName)) {
                return bars.get(i).get("id");
            }
        }

        return "";
    }

    public String getFirstDeal(String dayOfWeek, HashMap<String, String> bar) {
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
     *
     * @return The string representation of the current day of the week
     */
    public String getDayOfWeekStr() {

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
        public void onMyLocationChange(Location location) {
            myLocation = location;

//            //Set our current city
//            if (session.getCity().equals("none")) {
//                session.setCity(BarObjectManager.determineClosestCity(cities, myLocation, getApplicationContext()));
//            }

            //Zoom to our location if we haven't yet
            if (!myLocationDetermined) {
                zoomToCurrentCity();
            }


        }
    };

    /**
     * Zoom the map to the current city
     */
    private void zoomToCurrentCity() {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(session.getLat(), session.getLong()), 12.0f));
        myLocationDetermined = true;
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

            mMap.setInfoWindowAdapter(new CustomMapsWindowAdapter(getLayoutInflater()));
        }
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
                    ParseAnalyticsFunctions.verboseLog(ParseAnalyticsFunctions.TAXI, session.getCityName());

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
                    Intent settings = new Intent(getApplicationContext(), OldSettingsActivity.class);
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
     * Setup the string for our listview
     */
    private void setupSearchStringArray() {
        barsListForSearchview = new String[bars.size()];

        for (int i = 0; i < bars.size(); i++) {
            barsListForSearchview[i] = bars.get(i).get("name");
        }

    }

    /**
     * Setup our menu items
     *
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
                    if (barName.contains(s) || barName.equals(s)) {

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
                if (s.length() > 3) {
                    ParseAnalyticsFunctions.saveSearchTerm("Map", s, getApplicationContext());
                }
                return false;
            }

        });

        return true;
    }

    /**
     * Handling menu item selection
     *
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
                Intent list = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(list);
                overridePendingTransition(0, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}