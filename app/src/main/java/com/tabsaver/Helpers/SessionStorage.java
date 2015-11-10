package com.tabsaver.Helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

public class SessionStorage {

    //The shared preference file
    public SharedPreferences pref;

    public Editor edit;

    //The context
    public Context _context;

    //The name of the shared preference storing the data
    public static final String prefName = "com.tabsaver.userState";

    /**
     * Createa a new user session with the given context. Suppressed a warning that pref.edit
     * isn't actually commiting any changes.
     * @param context - The application context
     */
    @SuppressLint("CommitPrefEdits") public SessionStorage(Context context) {
        _context = context;
        pref = context.getSharedPreferences(prefName, 0); // 0 - for private mode
        edit = pref.edit();
    }

    /**
     * Log the user in and store session variables
     */
    public void login(String username, String token, String bar, String city){
        //Stores KEY - VALUE
        edit.putBoolean("loggedIn", true); // Stores that we're logged in
        edit.putString("user", username); // Stores the username
        edit.putString("token", token);
        edit.putString("bar", bar);
        edit.putString("city", city);
        edit.commit(); // commit changes
    }

    //Sets the city setting item
    public void setCity(HashMap<String, String> city){
        edit.putString("city", city.get("name"));
        edit.putString("lat" , city.get("lat"));
        edit.putString("lon", city.get("long"));
        edit.putString("state", city.get("state"));
        edit.putString("taxiService", city.get("taxiService"));
        edit.putString("taxiNumber", city.get("taxiNumber"));
        edit.commit();
    }

    public void setBars(String barsJSON) {
        edit.putString("bars", barsJSON);
        edit.commit();
    }

    public void setCities(String citiesJSON) {
        edit.putString("cities", citiesJSON);
        edit.commit();
    }

    public void setLastUpdateTime(){
        edit.putLong("lastUpdateTime", System.currentTimeMillis());
        edit.commit();
    }

    public void incrementTimesLoaded(){
        edit.putLong("timesUpdate", getTimesLoaded() + 1);
        edit.commit();
    }

    public boolean getShowClosedBars(){
        return pref.getBoolean("showClosedBars", true);
    }

    public boolean getShowBarsWithNoDeals(){
        return pref.getBoolean("showBarsWithNoDeals", true);
    }

    public long getTimesLoaded(){
        return pref.getLong("timesUpdate", 0);
    }

    public long getLastUpdateTime(){
        return pref.getLong("lastUpdateTime", 0);
    }

    public String getCities(){
        return pref.getString("cities", "none");
    }

    public String getBars(){
        return pref.getString("bars", "none");
    }
    /**
     * Get name of saved closest city
     * @return - The name of the nearest city
     */
    public String getCityName() {
        return pref.getString("city", "none");
    }

    /**
     * Get the taxi service for this city's name
     * @return - The name of the taxi service for the nearest city
     */
    public String getTaxiName(){
        return pref.getString("taxiService", "none");
    }

    /**
     * Get the taxi service for this city's number
     * @return - The phone number of the taxi service for the nearest city
     */
    public String getTaxiNumber(){
        return pref.getString("taxiNumber", "none");
    }

    /**
     * Get the latitude setting of city
     * @return - The latitude of the nearest city
     */
    public Double getLat(){
        return Double.valueOf(pref.getString("lat", "" + 0.0));
    }

    /**
     * Get the longitude setting of city
     * @return - The longitude of the nearest city
     */
    public Double getLong(){
        return Double.valueOf(pref.getString("lon", "" + 0.0));
    }


//    /**
//     * Returns the user name that is currently logged in
//     * @return - The current user logged in to the Admin Portal
//     */
//    public String getUser(){
//        return pref.getString("user", null);
//    }
//
//    /**
//     * Returns if the user is logged in or not
//     * @return - If we are logged in to the admin portal
//     */
//    public boolean isLoggedIn(){
//        return pref.getBoolean("loggedIn", false);
//    }
//
//    /**
//     * Returns the user's token
//     * @return - The unique token for communicating with the back-end API
//     */
//    public String getToken(){
//        return pref.getString("token", "none");
//    }

    /**
     * Returns the bar
     * @return - Not sure
     */
    public String getBar(){
        return pref.getString("bar", "none");
    }

    /**
     * Returns the city
     * @return - The current city's JSON representation
     */
    public String getCity(){
        return pref.getString("city", "none");
    }

    /**
     * Construct and return the hashmap representation of the current ity
     * @return The hashmap representation of a city
     */
    public HashMap<String, String> getCityObject(){
        HashMap<String, String> city = new HashMap<>();
        city.put("name", pref.getString("city", "none"));
        city.put("lat", pref.getString("lat", "none"));
        city.put("long", pref.getString("lon", "none"));
        city.put("state", pref.getString("state", "none"));
        city.put("taxiService", pref.getString("taxiService", "none"));
        city.put("taxiNumber", pref.getString("taxiNumber", "none"));
        return city;
    }

    public String getCityState(){
        return pref.getString("state", "none");
    }

    /**
     * Returns the distance preference
     * @return - The distance preference
     */
    public int getDistancePreference(){
        return pref.getInt("distance", 10);
    }

//    /**
//     * Logout the user
//     */
//    public void logout() {
//
//        edit.putBoolean("loggedIn", false); // Logged out
//        edit.putString("username", null); // null user
//        edit.putString("token", null); // null token
//        edit.commit(); // commit changes
//
//    }

}