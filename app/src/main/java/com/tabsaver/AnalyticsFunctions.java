package com.tabsaver;

import android.content.Context;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by Paul on 9/21/2015.
 */
public class AnalyticsFunctions {


    public static void incrementBarClickThrough(String barId){
        //query and load up the bars.
        ParseQuery getAnalytics = ParseQuery.getQuery("BarAnalytics");
        getAnalytics.whereEqualTo("barId", barId);

        // Retrieve the object by id
        getAnalytics.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {

                    //Should only return 1 bar - but check just in case
                    if ( objects.size() > 0 ) {
                        ParseObject bar = objects.get(0);
                        bar.increment("pageClicks");
                        bar.saveInBackground();
                    }

                }
            }
        });
    }

    /**
     * Used right after installation to set the installations location
     * @param city
     */
    public static void setInstallationCity(final String city){
        //Grab the current installation
        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();

        //Set the location and save
        currentInstallation.put("location", city);
        currentInstallation.saveInBackground();
    }

    //Save the entered search term
    public static void saveSearchTerm(String screenSearchedFrom, String value, Context context){
        //Need session to grab stored city name
        ClientSessionManager session = new ClientSessionManager(context);

        ParseObject searchTerm = new ParseObject("BarSearchAnalytics");
        searchTerm.put("value", value);
        searchTerm.put("device", ParseInstallation.getCurrentInstallation().getInstallationId());
        searchTerm.put("deviceType", "Android");
        searchTerm.put("ScreenSearchedFrom", screenSearchedFrom);
        searchTerm.put("location", session.getCityName());
        searchTerm.saveInBackground();
    }

    //Given the action and value (as shown in parse) increment it.
    public static void incrementAndroidAnalyticsValue(String action, String value){
        //query and load up the bars.
        ParseQuery getAnalytics = ParseQuery.getQuery("AndroidAnalytics");

        //Updating City Incrememnt property
        getAnalytics.whereEqualTo("value", value);
        getAnalytics.whereEqualTo("action", action);

        getAnalytics.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {

                    //Should only return 1 bar - but check just in case
                    if ( objects.size() > 0 ) {
                        ParseObject city = objects.get(0);
                        city.increment("count");
                        city.saveInBackground();
                    }

                }
            }
        });
    }

    //Given the action and value (as shown in parse) increment it.
    public static void incrementBarAnalyticsValue(String barId, final String value){
        //query and load up the bars.
        ParseQuery getAnalytics = ParseQuery.getQuery("BarAnalytics");

        //Updating City Incrememnt property
        getAnalytics.whereEqualTo("barId", barId);

        getAnalytics.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {

                    //Should only return 1 bar
                    if ( objects.size() > 0 ) {
                        ParseObject city = objects.get(0);
                        city.increment(value);
                        city.saveInBackground();
                    }

                }
            }
        });
    }
}
