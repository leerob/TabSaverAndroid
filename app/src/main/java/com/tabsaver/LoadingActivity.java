package com.tabsaver;

import android.app.Activity;
import android.content.Intent;
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
import java.util.List;


public class LoadingActivity extends Activity {

    //Storing and retrieving session information
    private ClientSessionManager session;

    //Message to display to the user
    private TextView loadingMessage;

    //Integer tracking the number of images loaded for the loading message
    private int numImagesLoaded;

    //Tracking status of all the information we've downloaded
    private boolean barsLoaded = false;
    private boolean barHoursLoaded = false;
    private boolean barDealsLoaded = false;
    private boolean citiesLoaded = false;
    private boolean imagesLoaded = false;

    private JSONArray bars;
    private JSONArray hours;
    private JSONArray deals;

    private static final int API_MAX_LIMIT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        //Setup the session
        session = new ClientSessionManager(getApplicationContext());

        //Set now as the current update time
        session.setLastUpdateTime();

        //TextView showing our current loading message
        loadingMessage = (TextView) findViewById(R.id.loadingMessage);

        //Always update deals/bar hours/etc. -- Only update images when needed
        setWaitingMessage("Taking shots...");
        getBarData();
        getBarDeals();
        getBarHours();
        getCities();
        session.setLastUpdateTime();

    }

    public void getBarData(){
        setWaitingMessage("Drinking a beer");

        //query and load up the bars.
        final ParseQuery getBars = new ParseQuery("Bars").setLimit(API_MAX_LIMIT);
        final JSONArray barData = new JSONArray();

        getBars.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) { //TODO: what is this error? What to do
                if (e == null) {
                    try {

                        //If the number of bars has changed (new bars)
                        if ( session.getNumberOfBars() != objects.size() ) {
                            getBarImages();
                            session.setNumberOfBars(objects.size());
                        } else {
                            imagesLoaded = true;
                        }

                        //Iterate over each bar
                        for (int i = 0; i < objects.size(); i++) {
                            ParseObject PO = objects.get(i);
                            JSONObject bar = new JSONObject();
                            bar.put("id", PO.getObjectId());
                            bar.put("name", PO.getString("name"));
                            bar.put("lat", PO.getDouble("lat"));
                            bar.put("long", PO.getDouble("long"));
                            bar.put("website", PO.getString("website"));
                            bar.put("number", PO.getString("number"));
                            bar.put("address", PO.getString("address"));
                            bar.put("city", PO.getString("city"));
                            bar.put("state", PO.getString("state"));
                            bar.put("foursquare", PO.getString("foursquare"));
                            barData.put(bar);
                        }



                        //Store information in our session
                        bars = barData;
                        barsLoaded = true;

                        //If everythings done, now we can get our images
                        if (barHoursLoaded && barDealsLoaded && citiesLoaded && imagesLoaded){
                            assembleBarDealInformation();
                        }

                    } catch (Exception ex) {
                        yell("Failed to load image");
                        yell("If issues persist, reinstall with a reliable internet connection.");
                        finish();
                    }
                } else {
                    yell(e.getMessage());
                    yell("If issues persiste, reinstall with a reliable internet connection.");
                    finish();
                }

            }
        });

    }

    public void yell(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void getCities(){
        setWaitingMessage("Taking shots... (This may take a while)");

        //query and load up the bars.
        final ParseQuery getBars = new ParseQuery("Locations").setLimit(API_MAX_LIMIT);
        final JSONArray cities = new JSONArray();

        getBars.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    try {
                        //Iterate over each bar
                        for (int i = 0; i < objects.size(); i++) {
                            ParseObject PO = objects.get(i);
                            JSONObject city = new JSONObject();
                            city.put("id", PO.getString("objectId"));
                            city.put("name", PO.getString("cityName"));
                            city.put("state", PO.getString("state"));
                            city.put("lat", PO.getDouble("lat"));
                            city.put("long", PO.getDouble("long"));
                            city.put("taxiService", PO.getString("taxiService"));
                            city.put("taxiNumber", PO.getString("taxiNumber"));
                            cities.put(city);
                        }

                        //Store information in our session
                        session.setCities(cities.toString());
                        citiesLoaded = true;

                        //If everythings done, now we can get our images
                        if (barHoursLoaded && barDealsLoaded && barsLoaded && imagesLoaded){
                            assembleBarDealInformation();
                        }
                    } catch (Exception ex) {
                        yell("Failed to load image.");
                        yell("If issues persiste, reinstall with a reliable internet connection.");
                    }
                } else {
                    yell(e.getMessage());
                    yell("If issues persiste, reinstall with a reliable internet connection.");
                }

            }
        });

    }

    public void getBarDeals(){
        setWaitingMessage("Three tequila, floor.");

        //query and load up that image.
        final ParseQuery getBars = new ParseQuery("Deals").setLimit(API_MAX_LIMIT);
        final JSONArray barDeals = new JSONArray();

        getBars.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    try {
                        //Iterate over each bar
                        for (int i = 0; i < objects.size(); i++) {
                            ParseObject PO = objects.get(i);
                            JSONObject bar = new JSONObject();
                            bar.put("barId", PO.getString("barsId"));
                            bar.put("Monday", PO.getJSONArray("Monday"));
                            bar.put("Tuesday", PO.getJSONArray("Tuesday"));
                            bar.put("Wednesday", PO.getJSONArray("Wednesday"));
                            bar.put("Thursday", PO.getJSONArray("Thursday"));
                            bar.put("Friday", PO.getJSONArray("Friday"));
                            bar.put("Saturday", PO.getJSONArray("Saturday"));
                            bar.put("Sunday", PO.getJSONArray("Sunday"));
                            bar.put("Sunday", PO.getJSONArray("Sunday"));
                            barDeals.put(bar);
                        }

                        deals = barDeals;
                        barDealsLoaded = true;

                        //If everythings done, now we can get our images
                        if (barHoursLoaded && barsLoaded && citiesLoaded && imagesLoaded){
                            assembleBarDealInformation();
                        }
                    } catch (Exception ex) {
                        yell("Failed to load image");
                        yell("If issues persiste, reinstall with a reliable internet connection.");
                    }
                } else {
                    yell(e.getMessage());
                    yell("If issues persiste, reinstall with a reliable internet connection.");
                }

            }
        });

    }

    public void getBarHours(){
        setWaitingMessage("Checking the time..");

        //query and load up that image.
        final ParseQuery getBars = new ParseQuery("BarHours").setLimit(API_MAX_LIMIT);
        final JSONArray barHours = new JSONArray();

        getBars.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) { //TODO: what is this error? What to do
                if (e == null) {
                    try {
                        //Iterate over each bar
                        for (int i = 0; i < objects.size(); i++) {
                            ParseObject PO = objects.get(i);
                            JSONObject bar = new JSONObject();
                            bar.put("barId", PO.getString("barsId"));
                            bar.put("Monday", PO.getString("monday"));
                            bar.put("Tuesday", PO.getString("tuesday"));
                            bar.put("Wednesday", PO.getString("wednesday"));
                            bar.put("Thursday", PO.getString("thursday"));
                            bar.put("Friday", PO.getString("friday"));
                            bar.put("Saturday", PO.getString("saturday"));
                            bar.put("Sunday", PO.getString("sunday"));
                            barHours.put(bar);
                        }

                        hours = barHours;
                        barHoursLoaded = true;

                        //If everythings done, now we can get our images
                        if (barDealsLoaded && barsLoaded && citiesLoaded && imagesLoaded) {
                            assembleBarDealInformation();
                        }
                    } catch (Exception ex) {
                        yell("Failed to load image");
                        yell("If issues persiste, reinstall with a reliable internet connection.");
                    }
                } else {
                    yell(e.getMessage());
                    yell("If issues persiste, reinstall with a reliable internet connection.");
                }

            }
        });
    }

    public void setWaitingMessage(String message){
        loadingMessage.setText(message);
    }

    public void getBarImages(){
        setWaitingMessage("One tequila, two tequila..");
        //query and load up that image.
        final ParseQuery getImages = new ParseQuery("BarPhotos").setLimit(API_MAX_LIMIT);

        getImages.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) { //TODO: what is this error? What to do
                if (e == null) {
                    try {
                        //Iterate over each bar
                        for (int i = 0; i < objects.size(); i++) {
                            ParseObject PO = objects.get(i);
                            final String barId = PO.getString("barsId");

                            //Setup to read the file
                            String imageFilePath = getApplicationContext().getFilesDir() + "/" + barId;
                            File imageFile = new File( imageFilePath );
                            int size = (int) imageFile.length();

                            //If the file does not exist
                            if ( size == 0 ) {
                                try {

                                    ParseFile image = (ParseFile) PO.get("imageFile");
                                    byte[] imageFileBytes = image.getData();

                                    //Now store the file locally
                                    File storedImage = new File(getApplicationContext().getFilesDir(), barId + "");
                                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(storedImage));
                                    bos.write(imageFileBytes);
                                    bos.flush();
                                    bos.close();

                                } catch (Exception ex) {
                                    yell("Failed to load image");
                                    yell("If issues persiste, reinstall with a reliable internet connection.");
                                }
                            }

                            setWaitingMessage("Selfie " + i + " out of " + objects.size() + " complete!");
                        }

                        imagesLoaded = true;

                        if (barHoursLoaded && barsLoaded && barDealsLoaded && citiesLoaded) {
                            assembleBarDealInformation();
                        }
                    } catch (Exception ex) {
                        yell("Failed to load image");
                        yell("If issues persiste, reinstall with a reliable internet connection.");
                    }
                } else {
                    yell(e.getMessage());
                    yell("If issues persiste, reinstall with a reliable internet connection.");
                }

            }
        });
    }

    /**
     * Finish this activity and move on!
     */
    public void moveToStartScreen(){
        Intent i = new Intent(LoadingActivity.this, MainActivity.class);
        LoadingActivity.this.startActivity(i);
        LoadingActivity.this.finish();
    }

    public void assembleBarDealInformation(){
        setWaitingMessage("Piecing together the evening");

        try {
            for(int i = 0; i < bars.length(); i++ ) {
                JSONObject bar = bars.getJSONObject(i);

                //Grab bars deals
                for(int j = 0; j < deals.length(); j++){
                    JSONObject deal = deals.getJSONObject(j);

                    if (deal.getString("barId").equals(bar.getString("id"))) {
                        bar.put("deals", deal.toString());
                        break;
                    }
                }

                //Grab bar hours
                for( int j = 0; j < hours.length(); j++ ) {
                    JSONObject hour = hours.getJSONObject(j);

                    if (hour.getString("barId").equals(bar.getString("id"))) {
                        bar.put("hours", hour.toString());
                        break;
                    }
                }
            }

            session.setBars(bars.toString());
            moveToStartScreen();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
