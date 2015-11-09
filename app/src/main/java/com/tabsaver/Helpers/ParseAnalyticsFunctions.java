package com.tabsaver.Helpers;

import android.content.Context;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

/**
 * Created by Paul on 9/21/2015.
 */
public class ParseAnalyticsFunctions {

    private static final boolean debugging = true;

    public static final String CHANGEDAY = "changeListviewDay";
    public static final String TAXI = "callTaxi";
    public static final String SHOWCLOSEDBARS = "showClosedBars";
    public static final String SHOWBARSNODEALS = "showBarsNowDeals";
    public static final String BARCLICK = "pageClicks";
    public static final String YELP = "yelpClicks";
    public static final String FOURSQUARE = "foursquareClicks";
    public static final String WEBSITE = "siteVisits";
    public static final String PHONECALL = "phoneCalled";
    public static final String NAVIGATE = "directionsRequests";



    public static void incrementBarClickThrough(String barId){

        if ( !debugging  ) {
            //query and load up the bars.
            ParseQuery getAnalytics = ParseQuery.getQuery("BarAnalytics");
            getAnalytics.whereEqualTo("barId", barId);

            // Retrieve the object by id
            getAnalytics.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {

                        //Should only return 1 bar - but check just in case
                        if (objects.size() > 0) {
                            ParseObject bar = objects.get(0);
                            bar.increment("pageClicks");
                            bar.saveInBackground();
                        }

                    }
                }
            });
        }
    }

    /**
     * Used right after installation to set the installations location
     * @param city
     */
    public static void setInstallationCity(final String city){

        if ( !debugging  ) {
            //Grab the current installation
            ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();

            //Set the location and save
            currentInstallation.put("location", city);
            currentInstallation.saveInBackground();
        }
    }

    //Save the entered search term
    public static void saveSearchTerm(String screenSearchedFrom, String value, Context context){
        if ( !debugging  ) {
            //Need session to grab stored city name
            SessionStorage session = new SessionStorage(context);

            ParseObject searchTerm = new ParseObject("BarSearchAnalytics");
            searchTerm.put("value", value);
            searchTerm.put("device", ParseInstallation.getCurrentInstallation().getInstallationId());
            searchTerm.put("deviceType", "Android");
            searchTerm.put("ScreenSearchedFrom", screenSearchedFrom);
            searchTerm.put("location", session.getCityName());
            searchTerm.saveInBackground();
        }
    }

    //Given the action and value (as shown in parse) increment it.
    public static void incrementAndroidAnalyticsValue(String action, String value){
        if ( !debugging  ) {
            //query and load up the bars.
            ParseQuery getAnalytics = ParseQuery.getQuery("AndroidAnalytics");

            //Updating City Incrememnt property
            getAnalytics.whereEqualTo("value", value);
            getAnalytics.whereEqualTo("action", action);

            getAnalytics.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {

                        //Should only return 1 bar - but check just in case
                        if (objects.size() > 0) {
                            ParseObject city = objects.get(0);
                            city.increment("count");
                            city.saveInBackground();
                        }

                    }
                }
            });
        }
    }

    //Given the action and value (as shown in parse) increment it.
    public static void incrementBarAnalyticsValue(String barId, final String value){
        if ( !debugging  ) {
            //query and load up the bars.
            ParseQuery getAnalytics = ParseQuery.getQuery("BarAnalytics");

            //Updating City Incrememnt property
            getAnalytics.whereEqualTo("barId", barId);

            getAnalytics.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {

                        //Should only return 1 bar
                        if (objects.size() > 0) {
                            ParseObject city = objects.get(0);
                            city.increment(value);
                            city.saveInBackground();
                        }

                    }
                }
            });
        }
    }

    //Save the entered search term
    public static void verboseLog(String action, String secondaryAction){

        if ( !debugging  ) {
            ParseObject verboseLog = new ParseObject("Logs");
            verboseLog.put("action", action);
            verboseLog.put("secondaryAction", secondaryAction);
            verboseLog.put("deviceType", "Android");
            verboseLog.saveInBackground();
        }
    }
}
