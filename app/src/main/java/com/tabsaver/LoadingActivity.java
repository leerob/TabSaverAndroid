package com.tabsaver;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
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
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LoadingActivity extends Activity {

    //All bars information
    private ArrayList<HashMap<String, String>> bars;

    //Storing and retrieving session information
    private ClientSessionManager session;

    //Message to display to the user
    private TextView loadingMessage;

    //Integer tracking the number of images loaded
    private int numImagesLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        //Setup the session
        session = new ClientSessionManager(getApplicationContext());

        //TextView showing our current loading message
        loadingMessage = (TextView) findViewById(R.id.loadingMessage);

        //Grab bar information from online if we have to. TODO: Add a once daily sync.
        if ( session.getBars().equals("none") ) {
            new DownloadJSON().execute();
            setWaitingMessage("Taking a shot...");
        } else {
            try {
                //grab bar and cities json
                JSONArray barsJSON = new JSONArray(session.getBars());

                setWaitingMessage("Drinking some beers...");
                //Setup hash maps for efficient data access
                setupBarsHashmap(barsJSON);
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

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

            return null;
        }


        @Override
        protected void onPostExecute(Void args) {
            try {
                setupBarsHashmap(new JSONArray(session.getBars()));
                setWaitingMessage("Drinking some beers...");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void setWaitingMessage(String message){
        loadingMessage.setText(message);
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

                // Set the JSON Objects into the array
                bars.add(bar);
            }

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        downloadBarImages();
    }

    public void downloadBarImages(){
        setWaitingMessage("Taking advantage of dollar shots.. \n This might take a few seconds.");
        numImagesLoaded = 0;

        for(int i = 0; i < bars.size() ; i++ ) {
            String barName = bars.get(i).get("name");
            final String barId = bars.get(i).get("id");

            //Setup to read the file
            String imageFilePath = getApplicationContext().getFilesDir() + "/" + barId;
            File imageFile = new File( imageFilePath );
            int size = (int) imageFile.length();

            //If the file does not exist
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

                        moveToStartScreen();
                    }
                });
            } else {
                moveToStartScreen();
            }
        }

    }

    /**
     * Finish this activity and move on!
     */
    public void moveToStartScreen(){
        numImagesLoaded++;

        setWaitingMessage("Drinking margarita " + numImagesLoaded + " out of " + bars.size() + "...");

        //Basically have to have some way to track the asynchronous downloading of images so we don't move on too soon
        if ( numImagesLoaded == bars.size() ) {
            //Move to homescreen
            Intent i = new Intent(LoadingActivity.this, MainActivity.class);
            LoadingActivity.this.startActivity(i);
            LoadingActivity.this.finish();
        }

    }

}
