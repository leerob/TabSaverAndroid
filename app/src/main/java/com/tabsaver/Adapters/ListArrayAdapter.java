package com.tabsaver.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tabsaver.Helpers.BarObjectManager;
import com.tabsaver.Helpers.ParseAnalyticsFunctions;
import com.tabsaver.Helpers.SessionStorage;
import com.tabsaver.R;
import com.tabsaver._Screens.Active.BarDetail;
import com.tabsaver._Screens.Active.LoadingActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ListArrayAdapter extends BaseAdapter {

    //Basic housekeeping
    Context context;
    LayoutInflater inflater;

    //Storing bar data
    ArrayList<HashMap<String, String>> barData;
    ArrayList<HashMap<String, String>> barDataBackupForSearchFiltering;

    //Sorting by day
    private String dayOfWeek;

    //Caching images
    private LruCache<String, Bitmap> mMemoryCache;

    final static long SHOULDUPDATE = 1000 * 60 * 60 * 24;

    //Storing and retrieving session information
    SessionStorage session;

    public ListArrayAdapter(Context context, ArrayList<HashMap<String, String>> barData, String dayOfWeek) {
        this.context = context;

        //Setup the session
        session = new SessionStorage(context);

        this.dayOfWeek = dayOfWeek;

        setData(barData);

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 2;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

    }

    /**
     * Used to update the day of the week
     * @param day - the day to set
     */
    public void setDayOfWeek(String day){
        dayOfWeek = day;
    }

    /**
     * Set the data for the list view
     * @param barData
     */
    public void setData(ArrayList<HashMap<String, String>> barData){
        //Store our bar data
        this.barData = barData;
        barDataBackupForSearchFiltering = new ArrayList<>();
        barDataBackupForSearchFiltering.addAll(barData);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        //Grab bar information
        HashMap<String, String> currentBar = barData.get(position);
        final String barName = currentBar.get("name");
        final String barId = currentBar.get("id");

        //Get our view
        inflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        View itemView = inflater.inflate(R.layout.list_item, parent, false); //TODO: What is this damn warning?

        // Determine Day of Week
        String dealsStr = BarObjectManager.getDealsString(currentBar, dayOfWeek);

        //setup formater for distance
        NumberFormat formatter = new DecimalFormat("#0.0");

        //Set the name
        ((TextView) itemView.findViewById(R.id.deal)).setText(barName);

        //Set the deals
        ((TextView) itemView.findViewById(R.id.deals)).setText(dealsStr);

        //Set the distance
        ((TextView) itemView.findViewById(R.id.distance)).setText(formatter.format(Double.valueOf(currentBar.get("distance"))) + " mi");

        //Now set the image for the bar
        loadBitmap(barId, ((ImageView) itemView.findViewById(R.id.bar_thumbnail)));

        //Set listener
        itemView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(context, BarDetail.class);
                i.putExtra("BarId", barId);
                context.startActivity(i);

                //Update bar analytics for clickthrough
                ParseAnalyticsFunctions.incrementBarClickThrough(barId);
                ParseAnalyticsFunctions.verboseLog(barName, ParseAnalyticsFunctions.BARCLICK);
            }
        });

        return itemView;
    }

    public void loadBitmap(String barId, ImageView barImage) {

        Bitmap bitmap = getBitmapFromMemCache(barId+"");

        if (bitmap != null) {
            barImage.setImageBitmap(bitmap);
        } else {
            bitmap = BarObjectManager.getImage(barId + "", context);

            if ( bitmap != null ) {
                barImage.setImageBitmap(bitmap);
            }

            addBitmapToMemoryCache(barId + "", bitmap);
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null && bitmap != null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    /**
     * Function for filtering the data in the list
     * @param text The input text to filter
     */
    public void filter(String text) {
        text = text.toLowerCase();
        barData.clear();
        if (text.length() == 0) {
            barData.addAll(barDataBackupForSearchFiltering);
        }
        else {
            if ( text.length() > 3){
                ParseAnalyticsFunctions.saveSearchTerm("List View", text, context);
            }

            for (HashMap<String, String> bar : barDataBackupForSearchFiltering)
            {

                //If the deal or bar name contains the search term
                if ( (bar.get("name").toLowerCase().contains(text) || BarObjectManager.getDealsString(bar, dayOfWeek).toLowerCase().contains(text) ) && Double.valueOf(bar.get("distance")) <= session.getDistancePreference())
                {
                    barData.add(bar);
                }
            }
        }
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return barData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}