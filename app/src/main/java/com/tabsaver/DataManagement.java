package com.tabsaver;

import android.content.Context;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Paul on 9/28/2015.
 */
public class DataManagement {

    //TODO: Extract hashmap functions for bars & cities
    //TODO: Extract determine closest city function

    /**
     * Given a hashmap of cities and our location, determine which city we are closest to.
     * @param cities
     * @param myLocation
     * @return
     */
    public static HashMap<String, String> determineClosestCity(ArrayList<HashMap<String, String>> cities, Location myLocation, Context context) {
        //Get our session
        ClientSessionManager session = new ClientSessionManager(context);

        //Current location
        Location cur = new Location("BS");

        //Minimum location
        Location min = new Location("BS");

        //City name
        String name = "None";

        //Set our minimum city to the first
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

        AnalyticsFunctions.incrementAndroidAnalyticsValue("LocationBasedCityChange", name);

        session.setCity(minCity);

        //Set our minimum city in the session
        return minCity;
    }


    /**
     * Given an application context, we generate the arraylist - hashmap representation of cities
     * @param context
     * @return
     */
    public static ArrayList<HashMap<String,String>> setupCitiesHashmap(Context context) {
        //Need our context for collecting the cities
        ClientSessionManager session = new ClientSessionManager(context);

        ArrayList<HashMap<String,String>> cities = new ArrayList<>();

        try {
            JSONArray citiesJSON = new JSONArray(session.getCities());

            for (int i = 0; i < citiesJSON.length(); i++) {
                //Grab the bar objects
                HashMap<String, String> city = new HashMap<>();
                JSONObject cityJSON = citiesJSON.getJSONObject(i);

                city.put("name", cityJSON.getString("name"));
                city.put("lat", cityJSON.getString("lat"));
                city.put("long", cityJSON.getString("long"));
                city.put("taxiService", cityJSON.getString("taxiService"));
                city.put("taxiNumber", cityJSON.getString("taxiNumber"));

                cities.add(city);
            }
        } catch (JSONException e ) {
        }

        return cities;
    }

    /**
     * Create a hashmap representation of all of our bars
     * @throws JSONException
     */
    public static ArrayList<HashMap<String,String>> setupBarsHashmap(Context context, Location myLocation) throws JSONException {
        //setup our context
        ClientSessionManager session = new ClientSessionManager(context);
        JSONArray barsJSON = new JSONArray(session.getBars());

        ArrayList<HashMap<String,String>> bars = new ArrayList<>();

        //Setup the bar info
        try {
            for (int i = 0; i < barsJSON.length(); i++) {
                HashMap<String, String> bar = new HashMap<>();
                JSONObject barJSON = barsJSON.getJSONObject(i);

                // Retrieve JSON Objects
                bar.put("id",  barJSON.getString("id"));
                bar.put("name", barJSON.getString("name"));
                bar.put("lat", barJSON.getString("lat"));
                bar.put("long", barJSON.getString("long"));

                //Put bars and hours
                bar.put("hours", barJSON.getString("hours"));
                bar.put("deals", barJSON.getString("deals"));


                if ( myLocation != null ) {
                    //Setup the distance
                    Location barLocation = new Location("BarLocation");
                    barLocation.setLatitude(barJSON.getDouble("lat"));
                    barLocation.setLongitude(barJSON.getDouble("long"));
                    bar.put("distance", (myLocation.distanceTo(barLocation) / 1609.34) + "");
                } else {
                    bar.put("distance", "0.0");
                }

                // Set the JSON Objects into the array
                bars.add(bar);
            }

        } catch (JSONException e) {

        }

        return bars;
    }
}
