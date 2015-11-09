package com.tabsaver.Helpers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class to manage downloading information from parse
 * Created by Paul on 10/16/2015.
 */
public class ParseDownloadManager {

    //Max objects to receive at a time from Parse
    private static final int API_MAX_LIMIT = 1000;

    //Application context
    private Context context;

    //Storing and retrieving session information
    private SessionStorage session;

    //If there is a textview for loading, update it
    private TextView loadingMessageTextView;

    //Activity to move to if we want
    private Class moveToNext;

    //If we find
    public boolean foundCity;

    /**
     * Setup an instance of the parse download manager
     * @param context - The application context
     * @param textView - A textview to post updates to, if any
     * @param nextClass - The activity to move to upon completion, if any
     */
    public ParseDownloadManager(Context context, TextView textView, Class nextClass){
        this.context = context;

        //Session and bars structure for storage
        session = new SessionStorage(context);

        //Optional parameters
        loadingMessageTextView = textView;
        moveToNext = nextClass;

        foundCity = false;
    }

    /**
     * Gab the bars that are in this city.
     */
    public void getBarsInCity(String city){
        setWaitingMessage("Taking shots.. (This may take a while)");

        //query and load up the bars for this city
        final ParseQuery getBars = new ParseQuery("Bars").setLimit(API_MAX_LIMIT);
        getBars.whereEqualTo("city", city);
        getBars.include("deals");
        getBars.include("hours");
        getBars.include("image");

        getBars.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    try {
                        //All the bars
                        JSONArray bars = new JSONArray();

                        //For each bar
                        for (int i = 0; i < objects.size(); i++) {
                            //Grab bar base information (info from bars table)
                            ParseObject barPO = objects.get(i);
                            JSONObject bar = new JSONObject();
                            String barId = barPO.getObjectId();

                            bar.put("id", barId);
                            bar.put("name", barPO.getString("name"));
                            bar.put("lat", barPO.getDouble("lat"));
                            bar.put("long", barPO.getDouble("long"));
                            bar.put("website", barPO.getString("website"));
                            bar.put("number", barPO.getString("number"));
                            bar.put("address", barPO.getString("address"));
                            bar.put("city", barPO.getString("city"));
                            bar.put("state", barPO.getString("state"));
                            bar.put("foursquare", barPO.getString("foursquare"));

                            //Grab bar deals information (info from deals table)
                            ParseObject dealsPO = barPO.getParseObject("deals");
                            JSONObject deals = new JSONObject();

                            //Add the deals to the deal object
                            deals.put("Monday", dealsPO.getJSONArray("Monday"));
                            deals.put("Tuesday", dealsPO.getJSONArray("Tuesday"));
                            deals.put("Wednesday", dealsPO.getJSONArray("Wednesday"));
                            deals.put("Thursday", dealsPO.getJSONArray("Thursday"));
                            deals.put("Friday", dealsPO.getJSONArray("Friday"));
                            deals.put("Saturday", dealsPO.getJSONArray("Saturday"));
                            deals.put("Sunday", dealsPO.getJSONArray("Sunday"));

                            //Add deals to this bar object
                            bar.put("deals", deals.toString());

                            //Grab the hours info (info from hours table)
                            ParseObject hoursPO = barPO.getParseObject("hours");
                            JSONObject hours = new JSONObject();

                            //Add the deals to the deal object
                            hours.put("Monday", hoursPO.getString("monday"));
                            hours.put("Tuesday", hoursPO.getString("tuesday"));
                            hours.put("Wednesday", hoursPO.getString("wednesday"));
                            hours.put("Thursday", hoursPO.getString("thursday"));
                            hours.put("Friday", hoursPO.getString("friday"));
                            hours.put("Saturday", hoursPO.getString("saturday"));
                            hours.put("Sunday", hoursPO.getString("sunday"));

                            //Add deals to this bar object
                            bar.put("hours", hours.toString());

                            getImageForBar(barPO.getParseObject("image"), barId);

                            bars.put(i, bar);
                        }

                        session.setBars(bars.toString());

                        moveToActivity();

                    } catch (Exception ex) {
                        yell("Failed to load bars.");
                        yell("If issues persist, reinstall with a reliable internet connection.");
                        ((Activity) context).finish();
                    }
                } else {
                    yell(e.getMessage());
                    yell("If issues persist, reinstall with a reliable internet connection.");
                    ((Activity) context).finish();
                }

            }
        });

    }

    /**
     * Grab all of the cities
     */
    public void getCities(){
        setWaitingMessage("Where am I?");

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

                        //On first installation, determine the closest city and set it.
                        if ( session.getCity().equals("none") ) {
                            setWaitingMessage("Finding some bars...");
                            determineLocation(context);
                        }

                    } catch (Exception ex) {
                        yell("Failed to load cities.");
                        yell("If issues persist, reinstall with a reliable internet connection.");
                        ((Activity) context).finish();
                        ParseAnalyticsFunctions.verboseLog("No Internet Connection", "App Closed");
                    }
                } else {
                    yell(e.getMessage());
                    yell("If issues persist, reinstall with a reliable internet connection.");
                    ((Activity) context).finish();
                    ParseAnalyticsFunctions.verboseLog("No Internet Connection", "App Closed");
                }

            }
        });

    }

    public void getImageForBar(ParseObject PO, String id){

        final String barId = PO.getString("barsId");

        //Setup to read the file
        String imageFilePath = context.getFilesDir() + "/" + barId;
        File imageFile = new File(imageFilePath);
        int size = (int) imageFile.length();

        //If the file does not exist
        if (size == 0) {
            try {

                ParseFile image = (ParseFile) PO.get("imageFile");
                byte[] imageFileBytes = image.getData();

                //Now store the file locally
                File storedImage = new File(context.getFilesDir(), barId + "");
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(storedImage));
                bos.write(imageFileBytes);
                bos.flush();
                bos.close();

            } catch (Exception ex) {
                yell("Failed to load image");
                yell("If issues persiste, reinstall with a reliable internet connection.");
            }
        }

    }

    /**
     * Move to the next activity and set the last update time
     */
    public void moveToActivity(){
        session.setLastUpdateTime();

        //Only move when we provided a class to start
        if ( moveToNext != null ) {
            Intent i = new Intent(context, moveToNext);
            context.startActivity(i);
            ((Activity) context).overridePendingTransition(0, 0);
            ((Activity) context).finish();
        }

    }

    /**
     * Toasting a message
     * @param msg - The message to toast
     */
    public void yell(String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * UI waiting message
     * @param message - Message to set in the textview
     */
    public void setWaitingMessage(String message){
        if ( loadingMessageTextView != null ) {
            loadingMessageTextView.setText(message);
        }
    }

    /**
     * Establishes current location
     */
    public void determineLocation(final Context context){

        // Acquire a reference to the system Location Manager
        android.location.LocationManager locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if ( !foundCity ) {
                    determineClosestCity(location, context);
                    foundCity = true;
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        try {
            // Register the listener with the Location Manager to receive location updates
            locationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 0, 0, locationListener);

        } catch (IllegalArgumentException e ) {
            Toast.makeText(context, "Unable to access GPS, please try again.", Toast.LENGTH_SHORT).show();
            ParseAnalyticsFunctions.verboseLog("GPS", "No Conection");
        }
    }

    /**
     * Determine the closest city
     * @param myLocation - Current location of user
     */
    public void determineClosestCity(Location myLocation, Context context) {
        ArrayList<HashMap<String, String>> cities = BarObjectManager.setupCitiesHashmap(context);

        //Get our session
        SessionStorage session = new SessionStorage(context);

        //Current location
        Location cur = new Location("BS");

        //Minimum location
        Location min = new Location("BS");

        HashMap<String, String> city = cities.get(0);
        min.setLatitude(Double.valueOf(city.get("lat")));
        min.setLongitude(Double.valueOf(city.get("long")));

        HashMap<String, String> minCity = new HashMap<>();
        for (int i = 0; i < cities.size(); i++) {
            //Setting up our current city
            HashMap<String, String> thisCity = cities.get(i);
            cur.setLatitude(Double.valueOf(thisCity.get("lat")));
            cur.setLongitude(Double.valueOf(thisCity.get("long")));

            if (myLocation.distanceTo(cur) <= myLocation.distanceTo(min)) {
                min.setLatitude(cur.getLatitude());
                min.setLongitude(cur.getLongitude());

                minCity = thisCity;
            }
        }

        //Store city in shared pref
        session.setCity(minCity);

        //Update analytics
        ParseAnalyticsFunctions.setInstallationCity(session.getCity());

        //Now load the bars for this city
        getBarsInCity(session.getCity());
    }

}
