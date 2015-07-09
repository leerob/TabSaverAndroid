package com.tabsaver;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;
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
    ArrayList<HashMap<String, String>> bars;

    //Listview
    ListView listview;
    ListArrayAdapter adapter;

    //Storing and retrieving session information
    ClientSessionManager session;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setup the session
        session = new ClientSessionManager(getApplicationContext());

        //Grab bar information from online if we have to. TODO: Add a once daily sync.
        if ( session.getBars().equals("none") ) {
            new DownloadJSON().execute();
        } else {
            try {
                //grab bar and cities json
                JSONArray barsJSON = new JSONArray(session.getBars());

                //Setup hash maps for efficient data access
                setupBarsHashmap(barsJSON);
                sortBarsByDistance();
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        //TODO: Abstract data syncing into it's own class? We also should grab images there and cache them when we open them
        //TODO: Weird behaviour when going back
        displayListView();
    }

    /**
     * Sync our bar and city data with the online database
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

            setupBarsHashmap(barsJSON);

            return null;
        }


        @Override
        protected void onPostExecute(Void args) {

        }
    }

    /**
     * Add each of our items to the view
     */
    public void displayListView() {
        listview = (ListView) findViewById(R.id.listview);
        adapter = new ListArrayAdapter(MainActivity.this, bars);
        listview.setAdapter(adapter);
    }

    public void setupBarsHashmap(JSONArray barsJSON){
       bars = new ArrayList<>();

        //Setup the bar info
        try {
            for (int i = 0; i < barsJSON.length(); i++) {
                HashMap<String, String> bar = new HashMap<>();
                JSONObject barJSON = barsJSON.getJSONObject(i);

                // Retrieve JSON Objects
                bar.put("id",  barJSON.getString("BarId"));
                bar.put("name", barJSON.getString("name"));
                bar.put("Monday", barJSON.getString("Monday"));
                bar.put("Tuesday", barJSON.getString("Tuesday"));
                bar.put("Wednesday", barJSON.getString("Wednesday"));
                bar.put("Thursday", barJSON.getString("Thursday"));
                bar.put("Friday", barJSON.getString("Friday"));
                bar.put("Saturday", barJSON.getString("Saturday"));
                bar.put("Sunday", barJSON.getString("Sunday"));
               bar.put("distance", 0.00 + ""); //TODO: We have to determine distance before loading this screen somehow..

                // Set the JSON Objects into the array
                bars.add(bar);
            }

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        downloadBarImages();
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

    public void downloadBarImages(){
        for(int i = 0; i < bars.size() ; i++ ) {
            String barName = bars.get(i).get("name");
            final String barId = bars.get(i).get("id");

            //Setup to read the file
            String imageFilePath = getApplicationContext().getFilesDir() + "/" + barId;
            File imageFile = new File( imageFilePath );
            int size = (int) imageFile.length();
            byte[] bytesForImageFile = new byte[size];

            //Set our bitmap
            Bitmap bitmap = null;

            //If the file exists
            if ( size == 0 ) {

                //query and load up that image.
                final ParseQuery findImage = new ParseQuery("BarPhotos");
                findImage.whereEqualTo("barName", barName);

                findImage.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> objects, ParseException e) { //TODO: what is this error? What to do
                        if (e == null) {
                            try {
                                //Grab the image
                                ArrayList<ParseObject> temp = (ArrayList<ParseObject>) objects;

                                //now get objectId
                                String objectId = temp.get(0).getObjectId();

                                //Do some weird shit and cast our image to a byte array
                                ParseObject imageHolder = findImage.get(objectId);
                                ParseFile image = (ParseFile) imageHolder.get("imageFile");
                                byte[] imageFile = image.getData();

                                //Now store the file locally
                                File storedImage = new File(getApplicationContext().getFilesDir(), barId + "");
                                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(storedImage));
                                bos.write(imageFile);
                                bos.flush();
                                bos.close();

                            } catch (Exception ex) {
                                Toast.makeText(getApplicationContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
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
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        //Setting up the search view
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
