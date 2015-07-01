package com.tabsaver;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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
import com.google.android.gms.maps.model.Marker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity {

    //Array representing the bars
    JSONArray jsonarray;

    //More efficient strucutre with the bars
    ArrayList<HashMap<String, String>> arraylist;

    //Listview
    ListView listview;

    //Storing and retrieving session information
    ClientSessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup the session
        session = new ClientSessionManager(getApplicationContext());

        //Get our bars
        try {
            jsonarray = new JSONArray(session.getBars());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        displayListView(false);
    }

    /**
     * Add each of our items to the view
     * @param sortByDeals
     */
    public void displayListView(boolean sortByDeals) {

        arraylist = new ArrayList<HashMap<String, String>>();

        try {
            for (int i = 0; i < jsonarray.length(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                JSONObject obj = jsonarray.getJSONObject(i);
                // Retrieve JSON Objects
                map.put("id",  String.valueOf(i));
                map.put("name", obj.getString("name"));
                map.put("Monday", obj.getString("Monday"));
                map.put("Tuesday", obj.getString("Tuesday"));
                map.put("Wednesday", obj.getString("Wednesday"));
                map.put("Thursday", obj.getString("Thursday"));
                map.put("Friday", obj.getString("Friday"));
                map.put("Saturday", obj.getString("Saturday"));
                map.put("Sunday", obj.getString("Sunday"));
                map.put("distance", obj.getString("distance"));

                // Set the JSON Objects into the array
                arraylist.add(map);
            }

        } catch (JSONException e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        //Sort the list
        ArrayList<HashMap<String, String>> newList = new ArrayList<HashMap<String, String>>();

        while (arraylist.isEmpty() != true) {
            HashMap<String, String> min = arraylist.get(0);

            for (int j = 0; j < arraylist.size(); j++) {
                HashMap<String, String> current = arraylist.get(j);

                if (Double.valueOf(min.get("distance")) > Double.valueOf(current.get("distance"))) {
                    min = current;
                }

            }

            arraylist.remove(min);
            newList.add(min);
        }

        listview = (ListView) findViewById(R.id.listview);
        ListArrayAdapter adapter = new ListArrayAdapter(MainActivity.this, newList, jsonarray);
        listview.setAdapter(adapter);
    }

    /**
     * Setting up our settings menu
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_view, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search for a bar");
        searchView.setIconifiedByDefault(false);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {



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
     * Handle settings menu interactions
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
            case R.id.showMapView:
                Intent map = new Intent(getApplicationContext(), MapActivity.class);
                map.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivityForResult(map, 0);
                overridePendingTransition(0, 0); //0 for no animation
                startActivity(map);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Return the current day of the week
     * @return
     */
    public String getDayOfWeek(){
        // Determine Day of Week
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        String dealsStr = "";
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

        return null;
    }
}
