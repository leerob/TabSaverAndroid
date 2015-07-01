package com.tabsaver;

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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ListArrayAdapter extends BaseAdapter {

    // Declare Variables
    Context context;
    LayoutInflater inflater;
    View itemView;


    //Storage of our data and the current deals
    ArrayList<HashMap<String, String>> data;
    HashMap<String, String> resultp = new HashMap<String, String>();
    JSONArray jsonarray;
    String barName;

    //Caching images for better performance
    public LruCache<String, Bitmap> mMemoryCache;

    public ListArrayAdapter(Context context, ArrayList<HashMap<String, String>> arraylist, JSONArray json) {
        this.context = context;
        data = arraylist;
        jsonarray = json;

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 4;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        //The textviews
        TextView name;
        TextView distance;
        TextView deals;

        // Set up our views
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        itemView = inflater.inflate(R.layout.list_item, parent, false);
        final ImageView barImage = (ImageView) itemView.findViewById(R.id.bar_thumbnail);

        // Get the position in list
        resultp = data.get(position);

        // Assign TextViews
        name = (TextView) itemView.findViewById(R.id.deal);
        distance = (TextView) itemView.findViewById(R.id.distance);
        deals = (TextView) itemView.findViewById(R.id.deals);

        // Determine Day of Week
        String dealsStr = getDealsString();

        //TODO: Replace this god damn comma stuff
        String formattedDeals = dealsStr.replace(",", ", ");

        //setup formater for distance
        NumberFormat formatter = new DecimalFormat("#0.0");

        //Set the textview portions
        barName = resultp.get("name");
        name.setText(barName);
        deals.setText(formattedDeals);
        distance.setText(formatter.format(Double.valueOf(resultp.get("distance"))) + " mi");

        // Capture ListView item click
        itemView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(context, BarDetail.class);
                i.putExtra("jsonArray", jsonarray.toString());
                i.putExtra("bar", data.get(position).get("name"));
                context.startActivity(i);

            }
        });

        //Try and grab bar display image from cache
        final Bitmap bitmap = getBitmapFromMemCache(position + "");

        //If we find the bitmap - use it
        if (bitmap != null) {
            barImage.setImageBitmap(bitmap);

            //Download the photo
        } else {
            //query and load up that image.
            final ParseQuery findImage = new ParseQuery("BarPhotos");
            findImage.whereEqualTo("barName", barName);

            findImage.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null) {
                        try {
                            //Grab this image
                            ArrayList<ParseObject> temp = (ArrayList<ParseObject>) objects;

                            //now get objectId
                            String objectId = temp.get(0).getObjectId();

                            //Do some weird shit and get and cast our image
                            ParseObject imageHolder = findImage.get(objectId);
                            ParseFile image = (ParseFile) imageHolder.get("imageFile");
                            byte[] imageFile = image.getData();

                            //Turn it into a bitmap and set our display image
                            Bitmap bmp = BitmapFactory.decodeByteArray(imageFile, 0, imageFile.length);

                            //Now compress it down to a low quality (5 = quality)
                            ByteArrayOutputStream bytearroutstream = new ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100,bytearroutstream);

                            //Set our image and cache it
                            barImage.setImageBitmap(bmp);
                            addBitmapToMemoryCache(String.valueOf(position + ""), bmp);

                        } catch (Exception ex) {
                            Toast.makeText(context, "Failed to load image.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        //failed
                    }
                }
            });
        }

        return itemView;
    }
    /**
     * Determine the deals for the day
     * @return
     */
    private String getDealsString(){
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        String dealsStr = "";

        switch (day) {
            case Calendar.SUNDAY:
                dealsStr = resultp.get("Sunday");
                break;
            case Calendar.MONDAY:
                dealsStr = resultp.get("Monday");
                break;
            case Calendar.TUESDAY:
                dealsStr = resultp.get("Tuesday");
                break;
            case Calendar.WEDNESDAY:
                dealsStr = resultp.get("Wednesday");
                break;
            case Calendar.THURSDAY:
                dealsStr = resultp.get("Thursday");
                break;
            case Calendar.FRIDAY:
                dealsStr = resultp.get("Friday");
                break;
            case Calendar.SATURDAY:
                dealsStr = resultp.get("Saturday");
                break;
        }

        return dealsStr;
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }

    @Override
    public int getCount() {
        return data.size();
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