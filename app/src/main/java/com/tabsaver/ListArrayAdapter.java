package com.tabsaver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ListArrayAdapter extends BaseAdapter {

    //Basic housekeeping
    Context context;
    LayoutInflater inflater;

    //Storing bar data
    ArrayList<HashMap<String, String>> barData;
    ArrayList<HashMap<String, String>> barDataBackupForSearchFiltering;

    //Caching images
    private LruCache<String, Bitmap> mMemoryCache;

    //Storing and retrieving session information
    ClientSessionManager session;

    public ListArrayAdapter(Context context, ArrayList<HashMap<String, String>> barData) {
        this.context = context;

        //Setup the session
        session = new ClientSessionManager(context);

        //Store our bar data
        this.barData = barData;
        barDataBackupForSearchFiltering = new ArrayList<>();
        barDataBackupForSearchFiltering.addAll(barData);

        //Now filter the visible data by distance
        filterByDistancePreference();

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

        //Store images in a hashmap for fast access
//        createBarImageHashmap();
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        //Grab bar information
        HashMap<String, String> currentBar = barData.get(position);
        String barName = currentBar.get("name");
        final int barId = Integer.valueOf(currentBar.get("id"));

        //Get our view
        inflater = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        View itemView = inflater.inflate(R.layout.list_item, parent, false); //TODO: What is this damn warning?

        // Determine Day of Week
        String dealsStr = getDealsString(currentBar);

        //TODO: Replace this god damn comma stuff
        String formattedDeals = dealsStr.replace(",", "\n");

        //setup formater for distance
        NumberFormat formatter = new DecimalFormat("#0.0");

        //Set the name
        ((TextView) itemView.findViewById(R.id.deal)).setText(barName);

        //Set the deals
        ((TextView) itemView.findViewById(R.id.deals)).setText(formattedDeals);

        //Set the distance
        ((TextView) itemView.findViewById(R.id.distance)).setText(formatter.format(Double.valueOf(currentBar.get("distance"))) + " mi");

        //Now set the image for the bar
        loadBitmap(barId, ((ImageView) itemView.findViewById(R.id.bar_thumbnail)));

        //Set listener
        itemView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(context, BarDetail.class);
                i.putExtra("BarId", barId + "");
                context.startActivity(i);
            }
        });

        return itemView;
    }

    public void loadBitmap(int barId, ImageView barImage) {

        Bitmap bitmap = getBitmapFromMemCache(barId+"");

        if (bitmap != null) {
            barImage.setImageBitmap(bitmap);
        } else {
            bitmap = getImage(barId+"");
            barImage.setImageBitmap(bitmap);
            addBitmapToMemoryCache(barId+"",bitmap);
        }
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
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


    public Bitmap getImage(final String barId) {
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

//    public void createBarImageHashmap(){
//        barImages = new HashMap<>();
//
//        for(int i = 0; i < barData.size(); i++ ) {
//            String name = barData.get(i).get("name");
//            String id = barData.get(i).get("id");
//            barImages.put(id, getImage(id, name));
//        }
//    }

    /**
     * Determine the deals for the day
     * @return the string representation of the day of the week
     */
    private String getDealsString(HashMap<String, String> currentBar){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        String dealsStr = "";

        switch (day) {
            case Calendar.SUNDAY:
                dealsStr = currentBar.get("Sunday");
                break;
            case Calendar.MONDAY:
                dealsStr = currentBar.get("Monday");
                break;
            case Calendar.TUESDAY:
                dealsStr = currentBar.get("Tuesday");
                break;
            case Calendar.WEDNESDAY:
                dealsStr = currentBar.get("Wednesday");
                break;
            case Calendar.THURSDAY:
                dealsStr = currentBar.get("Thursday");
                break;
            case Calendar.FRIDAY:
                dealsStr = currentBar.get("Friday");
                break;
            case Calendar.SATURDAY:
                dealsStr = currentBar.get("Saturday");
                break;
        }

        return dealsStr;
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
            filterByDistancePreference();
        }
        else {
            for (HashMap<String, String> bar : barDataBackupForSearchFiltering)
            {
                if (bar.get("name").toLowerCase().contains(text) && Double.valueOf(bar.get("distance")) <= session.getDistancePreference())
                {
                    barData.add(bar);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * Filtering by the distance preference
     */
    public void filterByDistancePreference(){

        for (HashMap<String, String> bar : barDataBackupForSearchFiltering) {

            if ( barData.contains(bar) &&  Double.valueOf(bar.get("distance")) > session.getDistancePreference()) {
                barData.remove(bar);
            } else if ( !barData.contains(bar) && Double.valueOf(bar.get("distance")) <= session.getDistancePreference() ) {
                barData.add(bar);
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