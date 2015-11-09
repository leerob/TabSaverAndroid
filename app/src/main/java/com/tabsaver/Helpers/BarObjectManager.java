package com.tabsaver.Helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by Paul on 9/28/2015.
 */
public class BarObjectManager {


    /**
     * Given an application context, we generate the arraylist - hashmap representation of cities
     * @param context
     * @return
     */
    public static ArrayList<HashMap<String,String>> setupCitiesHashmap(Context context) {
        //Need our context for collecting the cities
        SessionStorage session = new SessionStorage(context);

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
                city.put("state", cityJSON.getString("state"));
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
        SessionStorage session = new SessionStorage(context);
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

    /**
     * Image grabbing helper
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * Grabbing an image from local file storage
     * @param barId
     * @param context
     * @return
     */
    public static Bitmap getImage(final String barId, Context context) {
        //Setup to read the file
        String imageFilePath = context.getFilesDir() + "/" + barId;
        File imageFile = new File( imageFilePath );
        int size = (int) imageFile.length();
        byte[] bytesForImageFile = new byte[size];

        //Set our bitmap
        Bitmap bitmap = null;

        //If the file exists
        if ( size != 0 ) {
            //Try and read it in
            try {
                BufferedInputStream buf = new BufferedInputStream(new FileInputStream(imageFile));
                buf.read(bytesForImageFile, 0, bytesForImageFile.length);
                buf.close();
            } catch (IOException e) {
                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            //Setting up the image to create a bitmap of an appropriate size
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytesForImageFile, 0, bytesForImageFile.length, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 150, 150);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bitmap = BitmapFactory.decodeByteArray(bytesForImageFile, 0, bytesForImageFile.length, options);

            return bitmap;
        } else {
            return null;
        }
    }

    //Determines which day's deals apply (Today or yesterday - i.e, the bar hasn't closed from last night)
    public static int determineDayOfWeekForBar(HashMap<String, String> currentBar){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int curTime = calendar.get(Calendar.HOUR_OF_DAY);
        int prevDay = day;

        //Look at the previous day
        if ( day == 1 ) {
            prevDay = 7;
        } else {
            prevDay = day - 1;
        }

        //Get yesterdays hours
        String[] prevDayHours = getHoursForBar(currentBar, prevDay).split("-");

        //Closed bars situation
        if ( prevDayHours[0].equals("Closed") ) {
            return day;
        }

        //Parse the close time into an integer
        String closeTimeString = prevDayHours[1];
        int closeTime;

        //If they didn't close at night, they could be open today
        if ( !closeTimeString.contains("PM") ) {
            if ( closeTimeString.contains(":")) {
                closeTime = Integer.valueOf(closeTimeString.replace("AM", "").substring(0, closeTimeString.indexOf(':')));
            } else {
                closeTime = Integer.valueOf(closeTimeString.replace("AM", ""));
            }
        } else {
            //They closed last night so the day we should consider for deals is the current one
            return day;
        }

        if ( curTime < closeTime ) {
            return prevDay;
        } else {
            return day;
        }
    }

    /**
     * Grab hours for the bar for the given day
     * @param currentBar
     * @param day
     * @return
     */
    public static String getHoursForBar(HashMap<String, String> currentBar, int day){

        try {
            JSONObject hours = new JSONObject(currentBar.get("hours"));

            switch(day) {
                case Calendar.SUNDAY:
                    return hours.getString("Sunday");
                case Calendar.MONDAY:
                    return hours.getString("Monday");
                case Calendar.TUESDAY:
                    return hours.getString("Tuesday");
                case Calendar.WEDNESDAY:
                    return hours.getString("Wednesday");
                case Calendar.THURSDAY:
                    return hours.getString("Thursday");
                case Calendar.FRIDAY:
                    return hours.getString("Friday");
                case Calendar.SATURDAY:
                    return hours.getString("Saturday");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Determine the deals for the day
     * @return the string representation of the day of the week
     */
    public static String getDealsString(HashMap<String, String> currentBar, String dayOfWeek) {

        int day;

        if ( dayOfWeek.equals("Sunday") ) {
            day = 1;
        } else if ( dayOfWeek.equals("Monday") ) {
            day = 2;
        } else if ( dayOfWeek.equals("Tuesday") ) {
            day = 3;
        } else if ( dayOfWeek.equals("Wednesday") ) {
            day = 4;
        } else if ( dayOfWeek.equals("Thursday") ) {
            day = 5;
        } else if ( dayOfWeek.equals("Friday") ) {
            day = 6;
        } else if ( dayOfWeek.equals("Saturday") ) {
            day = 7;
        } else {
            day = determineDayOfWeekForBar(currentBar);
        }

        try {
            JSONObject deals = new JSONObject(currentBar.get("deals"));
            JSONArray daysDeals;
            String dealsResult = "";

            switch (day) {
                case Calendar.SUNDAY:
                    daysDeals = deals.getJSONArray("Sunday");
                    for(int i = 0; i < daysDeals.length(); i++) {
                        dealsResult = dealsResult + daysDeals.getString(i);

                        //Add comas except at the end
                        if (i != daysDeals.length() - 1) {
                            dealsResult = dealsResult + ", ";
                        }
                    }
                    break;
                case Calendar.MONDAY:
                    daysDeals = deals.getJSONArray("Monday");
                    for(int i = 0; i < daysDeals.length(); i++){
                        dealsResult = dealsResult + daysDeals.getString(i);

                        //Add comas except at the end
                        if ( i != daysDeals.length() - 1  ) {
                            dealsResult = dealsResult + ", ";
                        }
                    }
                    break;
                case Calendar.TUESDAY:
                    daysDeals = deals.getJSONArray("Tuesday");
                    for(int i = 0; i < daysDeals.length(); i++){
                        dealsResult = dealsResult + daysDeals.getString(i);

                        //Add comas except at the end
                        if ( i != daysDeals.length() - 1  ) {
                            dealsResult = dealsResult + ", ";
                        }
                    }
                    break;
                case Calendar.WEDNESDAY:
                    daysDeals = deals.getJSONArray("Wednesday");
                    for(int i = 0; i < daysDeals.length(); i++){
                        dealsResult = dealsResult + daysDeals.getString(i);

                        //Add comas except at the end
                        if ( i != daysDeals.length() - 1  ) {
                            dealsResult = dealsResult + ", ";
                        }
                    }
                    break;
                case Calendar.THURSDAY:
                    daysDeals = deals.getJSONArray("Thursday");
                    for(int i = 0; i < daysDeals.length(); i++){
                        dealsResult = dealsResult + daysDeals.getString(i);

                        //Add comas except at the end
                        if ( i != daysDeals.length() - 1  ) {
                            dealsResult = dealsResult + ", ";
                        }
                    }
                    break;
                case Calendar.FRIDAY:
                    daysDeals = deals.getJSONArray("Friday");
                    for(int i = 0; i < daysDeals.length(); i++){
                        dealsResult = dealsResult + daysDeals.getString(i);

                        //Add comas except at the end
                        if ( i != daysDeals.length() - 1  ) {
                            dealsResult = dealsResult + ", ";
                        }
                    }
                    break;
                case Calendar.SATURDAY:
                    daysDeals = deals.getJSONArray("Saturday");
                    for(int i = 0; i < daysDeals.length(); i++){
                        dealsResult = dealsResult + daysDeals.getString(i);

                        //Add comas except at the end
                        if ( i != daysDeals.length() - 1  ) {
                            dealsResult = dealsResult + ", ";
                        }
                    }
                    break;
            }

            return dealsResult;
        } catch (JSONException e) {

        }

        return "";
    }

}
