package com.tabsaver;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

    public ListArrayAdapter(Context context, ArrayList<HashMap<String, String>> barData) {
        this.context = context;

        //Store our bar data
        this.barData = barData;
        barDataBackupForSearchFiltering = new ArrayList<>();
        barDataBackupForSearchFiltering.addAll(barData);

        //TODO: Views are loading all messed up with lots of deals.
        //TODO: Getting out of memory errors
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
        String formattedDeals = dealsStr.replace(",", ", ");

        //setup formater for distance
        NumberFormat formatter = new DecimalFormat("#0.0");

        //Set the name
        ((TextView) itemView.findViewById(R.id.deal)).setText(barName);

        //Set the deals
        ((TextView) itemView.findViewById(R.id.distance)).setText(formattedDeals);

        //Set the distance
        ((TextView) itemView.findViewById(R.id.deals)).setText(formatter.format(Double.valueOf(currentBar.get("distance"))) + " mi");

        //Now set the image for the bar
        setBarImage(barId, barName, itemView);

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

    /**
     * Setup the image for a bar
     * @param barId the ID of this bar (for image storing/retrieving purposes)
     * @param barName the name of this bar
     */
    private void setBarImage(final int barId, String barName, View itemView) {
        //Grab the specific imageView
        final ImageView barImage = (ImageView) itemView.findViewById(R.id.bar_thumbnail);

        //Setup to read the file
        String imageFilePath = context.getFilesDir() + "/" + barId;
        File imageFile = new File( imageFilePath );
        int size = (int) imageFile.length();
        byte[] bytesForImageFile = new byte[size];

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
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytesForImageFile, 0, bytesForImageFile.length, options);

        //If we find the bitmap - use it
        if (bitmap != null) {
            barImage.setImageBitmap(bitmap);

        //Otherwise we have to download the photo
        } else {
            //query and load up that image.
            final ParseQuery findImage = new ParseQuery("BarPhotos");
            findImage.whereEqualTo("barName", barName);

            findImage.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) { //TODO: what is this error? What to do
                    if (e == null) {
                        try {
                            //Grab the image
                            ArrayList<ParseObject> temp = (ArrayList<ParseObject>) objects;

                            //now get objectId
                            String objectId = temp.get(0).getObjectId();

                            //Do some weird shit and cast our image to a byte array
                            ParseObject imageHolder = findImage.get(objectId);
                            ParseFile image = (ParseFile) imageHolder.get("imageFile");
                            byte[] imageFile = image.getData();

                            //Now store the file locally
                            File storedImage = new File(context.getFilesDir(), barId + "");
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(storedImage));
                            bos.write(imageFile);
                            bos.flush();
                            bos.close();

                            //Setting up the image to create a bitmap of an appropriate size
                            final BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inJustDecodeBounds = true;
                            BitmapFactory.decodeByteArray(imageFile, 0, imageFile.length);

                            // Calculate inSampleSize
                            options.inSampleSize = calculateInSampleSize(options, 150, 150);

                            // Decode bitmap with inSampleSize set
                            options.inJustDecodeBounds = false;
                            Bitmap bmp = BitmapFactory.decodeByteArray(imageFile, 0, imageFile.length, options);

                            //Now compress it down to a low quality (5 = quality)
                            ByteArrayOutputStream bytearroutstream = new ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytearroutstream);

                            //Set our image
                            barImage.setImageBitmap(bmp);

                        } catch (Exception ex) {
                            Toast.makeText(context, "Failed to load image.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

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
        }
        else {
            for (HashMap<String, String> bar : barDataBackupForSearchFiltering)
            {
                if (bar.get("name").toLowerCase().contains(text))
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