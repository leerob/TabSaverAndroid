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
import java.util.ArrayList;
import java.util.HashMap;
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
            getBarData();
            getBarDeals();
            getBarHours();
            getCities();
            getBarImages();
            setWaitingMessage("Taking shots...");
        } else {
            moveToStartScreen();
        }

    }

    public void getBarData(){
        setWaitingMessage("Drinking a beer");

        //query and load up the bars.
        final ParseQuery getBars = new ParseQuery("Bars");
        final JSONArray barData = new JSONArray();

        getBars.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) { //TODO: what is this error? What to do
                if (e == null) {
                    try {
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
                        Toast.makeText(getApplicationContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void getCities(){
        setWaitingMessage("One tequila, two tequila..");

        //query and load up the bars.
        final ParseQuery getBars = new ParseQuery("Locations");
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
                        Toast.makeText(getApplicationContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void getBarDeals(){
        setWaitingMessage("Three tequila, floor.");

        //query and load up that image.
        final ParseQuery getBars = new ParseQuery("Deals");
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
                        Toast.makeText(getApplicationContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    public void getBarHours(){
        setWaitingMessage("Checking the time..");

        //query and load up that image.
        final ParseQuery getBars = new ParseQuery("BarHours");
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
                        Toast.makeText(getApplicationContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    public void setWaitingMessage(String message){
        loadingMessage.setText(message);
    }
//
//    public void setupBarsHashmap() throws JSONException {
//        setWaitingMessage("Ooh dollar shots!");
//
//        JSONArray barsJSON = new JSONArray(session.getBars());
//        bars = new ArrayList<>();
//
//        //Setup the bar info
//        try {
//            for (int i = 0; i < barsJSON.length(); i++) {
//                HashMap<String, String> bar = new HashMap<>();
//                JSONObject barJSON = barsJSON.getJSONObject(i);
//
//                // Retrieve JSON Objects
//                bar.put("id",  barJSON.getString("BarId"));
//                bar.put("name", barJSON.getString("name"));
//                bar.put("Monday", barJSON.getString("Monday"));
//                bar.put("Tuesday", barJSON.getString("Tuesday"));
//                bar.put("Wednesday", barJSON.getString("Wednesday"));
//                bar.put("Thursday", barJSON.getString("Thursday"));
//                bar.put("Friday", barJSON.getString("Friday"));
//                bar.put("Saturday", barJSON.getString("Saturday"));
//                bar.put("Sunday", barJSON.getString("Sunday"));
//
//                // Set the JSON Objects into the array
//                bars.add(bar);
//            }
//
//        } catch (JSONException e) {
//            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//
//        getBarImages();
//    }

    public void getBarImages(){
        setWaitingMessage("Taking selfies...");
        //query and load up that image.
        final ParseQuery getImages = new ParseQuery("BarPhotos");
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
                                    Toast.makeText(getApplicationContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
                                }
                            }

                            setWaitingMessage("Selfie " + i + " out of " + objects.size() + " complete!");
                        }

                        imagesLoaded = true;

                        if (barHoursLoaded && barsLoaded && barDealsLoaded && citiesLoaded) {
                            assembleBarDealInformation();
                        }
                    } catch (Exception ex) {
                        Toast.makeText(getApplicationContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

//    public void downloadBarImages(){
//        setWaitingMessage("Taking selfies at all of the bars...");
//        numImagesLoaded = 0;
//
//        for(int i = 0; i < bars.size() ; i++ ) {
//            String barName = bars.get(i).get("name");
//            final String barId = bars.get(i).get("id");
//
//            //Setup to read the file
//            String imageFilePath = getApplicationContext().getFilesDir() + "/" + barId;
//            File imageFile = new File( imageFilePath );
//            int size = (int) imageFile.length();
//
//            //If the file does not exist
//            if ( size == 0 ) {
//
//                //query and load up that image.
//                final ParseQuery findImage = new ParseQuery("BarPhotos");
//                findImage.whereEqualTo("barName", barName);
//
//                findImage.findInBackground(new FindCallback<ParseObject>() {
//                    public void done(List<ParseObject> objects, ParseException e) { //TODO: what is this error? What to do
//                        if (e == null) {
//                            try {
//                                //Grab the image
//                                ArrayList<ParseObject> temp = (ArrayList<ParseObject>) objects;
//
//                                //now get objectId
//                                String objectId = temp.get(0).getObjectId();
//
//                                //Do some weird shit and cast our image to a byte array
//                                ParseObject imageHolder = findImage.get(objectId);
//                                ParseFile image = (ParseFile) imageHolder.get("imageFile");
//                                byte[] imageFile = image.getData();
//
//                                //Now store the file locally
//                                File storedImage = new File(getApplicationContext().getFilesDir(), barId + "");
//                                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(storedImage));
//                                bos.write(imageFile);
//                                bos.flush();
//                                bos.close();
//
//                            } catch (Exception ex) {
//                                Toast.makeText(getApplicationContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
//                            }
//                        } else {
//                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//
//                        moveToStartScreen();
//                    }
//                });
//            } else {
//                moveToStartScreen();
//            }
//        }
//
//    }

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
                        System.out.println("wtf");
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
