package com.tabsaver._Screens.Active;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.tabsaver.Helpers.ParseAnalyticsFunctions;
import com.tabsaver.Helpers.SessionStorage;
import com.tabsaver.Helpers.JSONFunctions;
import com.tabsaver.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class BarDetail extends ActionBarActivity implements OnItemSelectedListener {

    //All of the bars information
    HashMap<String, String> bar = new HashMap<>();

    //For filling in the deals array
    ListView listview;
    ArrayAdapter<String> arrayAdapter;
    String[] dealsForSelectedDay;

    //For the deals out of date
    String barName;
    String dayOfWeek;
    String barId;
    Boolean dealOutOfDateSent;

    //Storing and retrieving session information
    SessionStorage session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_detail);

        //Setup the session
        session = new SessionStorage(getApplicationContext());

        //Grab our barID
        Intent intent = getIntent();
        barId = intent.getStringExtra("BarId");
        dealOutOfDateSent = false;

        //Load this bars information
        getBarHashmap(barId);

        //Now display everything
        setupViews();
    }

    private class SendDealsOutOfDateMessage extends AsyncTask<Void, Void, Void> {

        JSONArray result;

        @Override
        protected Void doInBackground(Void... params) {
            result = JSONFunctions.getJSONfromURL("http://tabsaver.info/tabsaver/dealsOutdated.php?bar=" + barName + "&day=" + dayOfWeek);
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            //TODO: Not always reporting
            if ( true ) {
                Toast.makeText(BarDetail.this, "Thanks! Report received. Give us a day or two to make an update.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(BarDetail.this, "Sorry something went wrong.. Please try again later!", Toast.LENGTH_SHORT).show();
            }

            dealOutOfDateSent = true;
        }
    }

    public void setupViews(){
        //Setup our views
        final TextView barWebsite = (TextView) findViewById(R.id.barWebsite);
        final TextView barPhone = (TextView) findViewById(R.id.barPhone);
        final TextView barAddress = (TextView) findViewById(R.id.barAddress);
        final TextView yelp = (TextView) findViewById(R.id.yelp);
        final TextView foursquare = (TextView) findViewById(R.id.foursquare);
        listview = (ListView) findViewById(R.id.listView);

        //Set the bar's name
        ((TextView) findViewById(R.id.barName)).setText(bar.get("name"));

        //Set the bar's hours
        ((TextView) findViewById(R.id.barHours)).setText(getHoursForBar(determineDayOfWeekForBar()));

        //Set the bar's address
        barAddress.setText(bar.get("address") + ", " + bar.get("city") + ", " + bar.get("state"));

        //Setup the bar's phone number
        if(bar.get("number").equals("No Number")){
            barPhone.setText("No Number");
        } else {
            Log.d("PHONE", bar.get("number"));
            barPhone.setText("(" + bar.get("number").substring(0, 3) + ") " + bar.get("number").substring(3, 6) + " - " + bar.get("number").substring(6, 10));
        }

        //Set the bar's website URL
        barWebsite.setText(bar.get("website"));

        //Load our image from cache or main memory
        loadImage();

        //Listener to dial the number on click
        barPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bar.get("number").equals("No Number")) {
                    //Update analytics
                    ParseAnalyticsFunctions.incrementBarAnalyticsValue(barId, "phoneCalls");

                    //Parse phone number, send off the call
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + bar.get("number")));
                    startActivity(intent);
                }
            }
        });

        //Listener to navigate to a site on click
        barWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Update analytics
                ParseAnalyticsFunctions.incrementBarAnalyticsValue(barId, "siteVisits");

                //Navigate to website
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(bar.get("website")));
                startActivity(browserIntent);
            }
        });

        //Listener to navigate to yelp
        yelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Update analytics
                ParseAnalyticsFunctions.incrementAndroidAnalyticsValue("YelpNavigation", "Clicks");

                //Navigate to website
                //TODO: Navigate to yelp search
            }
        });

        //Listener to navigate to foursquare
        foursquare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Update analytics
                ParseAnalyticsFunctions.incrementAndroidAnalyticsValue("FourSquareNavigation", "Clicks");

                //Navigate to website
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://foursquare.com/v/venue/" + bar.get("foursquare")));
                        startActivity(browserIntent);
            }
        });

        //Listener to navigate to the address on click
        barAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Update analytics
                ParseAnalyticsFunctions.incrementBarAnalyticsValue(barId, "directionsRequests");

                //Use google maps service to navigate
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr="+bar.get("lat") +","+( Double.valueOf(bar.get("long")) * -1 )));
                startActivity(intent);
            }
        });

        //Parse and display the current deals for the day
        dayOfWeek = getDayOfWeekAsString();

        getDealsForDay(dayOfWeek);

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dealsForSelectedDay);
        listview.setAdapter(arrayAdapter);
        listview.setVerticalScrollbarPosition(View.SCROLLBAR_POSITION_LEFT);

        //Make it scrollable so that the scrollview doesn't intercept the scrolling
        listview.setOnTouchListener(new ListView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                // Handle ListView touch events.
                v.onTouchEvent(event);
                return true;
            }
        });

        ((TextView) findViewById(R.id.dealsOutdated)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!dealOutOfDateSent) {
                    new SendDealsOutOfDateMessage().execute();
                }
            }
        });

        //Setup the spinner selection listener
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.days_of_week, R.layout.custom_spinner);
        adapter.setDropDownViewResource(R.layout.custom_dropdown);
        spinner.setAdapter(adapter);
        spinner.setSelection(getIndex(spinner, dayOfWeek));
        spinner.setOnItemSelectedListener(this);

        getSupportActionBar().hide();
    }

    public void getDealsForDay(String dayOfWeek){
        JSONObject dealsArray;
        JSONArray todaysDeals;
        try {
            dealsArray = new JSONObject(bar.get("deals"));
            todaysDeals = dealsArray.getJSONArray(dayOfWeek);
            dealsForSelectedDay = new String[todaysDeals.length()];

            for(int i = 0; i < todaysDeals.length(); i++ ){
                dealsForSelectedDay[i] = todaysDeals.getString(i);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //Determines which day's deals apply (Today or yesterday - i.e, the bar hasn't closed from last night)
    private int determineDayOfWeekForBar(){
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
        String[] prevDayHours = getHoursForBar(prevDay).split("-");

        //Closed bars situation
        if ( prevDayHours[0].equals("Closed") ) {
            return day;
        }

        //Parse the close time into an integer
        String closeTimeString = prevDayHours[1];
        int closeTime;

        //If they didn't close at night, they could be open today
        if ( !closeTimeString.contains("PM") ) {
            closeTime = Integer.valueOf(closeTimeString.replace("AM", ""));
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

    //Grabs the hours for the given day int
    private String getHoursForBar(int day){

        try {
            JSONObject hours = new JSONObject(bar.get("hours"));

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

    public void loadImage(){
        final ImageView barImage = (ImageView) findViewById(R.id.imageView);

        //Setup to read the file
        String imageFilePath = getApplicationContext().getFilesDir() + "/" + bar.get("id");
        File imageFile = new File( imageFilePath );
        int size = (int) imageFile.length();
        byte[] bytesForImageFile = new byte[size];

        //Try and read it in
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(imageFile));
            buf.read(bytesForImageFile, 0, bytesForImageFile.length);
            buf.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        //Turn it into an image file
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytesForImageFile, 0, bytesForImageFile.length);

        //If we find the bitmap - use it
        if (bitmap != null) {
            barImage.setImageBitmap(bitmap);

            //Otherwise we have to download the photo
        } else {
            //query and load up that image.
            final ParseQuery findImage = new ParseQuery("BarPhotos");
            findImage.whereEqualTo("barName", bar.get("name"));

            findImage.findInBackground(new FindCallback<ParseObject>() {
                public void done(List<ParseObject> objects, ParseException e) { //TODO: what is this warning? What to do
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
                            File storedImage = new File(getApplicationContext().getFilesDir(), bar.get("id"));
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(storedImage));
                            bos.write(imageFile);
                            bos.flush();
                            bos.close();

                            //Turn it into a bitmap and set our display image
                            Bitmap bmp = BitmapFactory.decodeByteArray(imageFile, 0, imageFile.length);

                            //Now compress it down to a low quality (5 = quality)
                            ByteArrayOutputStream bytearroutstream = new ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytearroutstream);

                            //Set our image
                            barImage.setImageBitmap(bmp);

                        } catch (Exception ex) {
                            Toast.makeText(getApplicationContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }


    public String getDayOfWeekAsString(){

        Calendar calendar = Calendar.getInstance();
        int day = determineDayOfWeekForBar();

        switch (day) {
            case Calendar.SUNDAY:
                return "Sunday";
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
        }

        return "";
    }

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        //Show the deals for the selected day
        getDealsForDay(parent.getItemAtPosition(pos).toString());

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dealsForSelectedDay);
        listview.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

    private int getIndex(Spinner spinner, String myString){

        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).equals(myString)){
                index = i;
            }
        }
        return index;
    }

    /**
     * Create the hashmap representation of this bar
     * @param id the current bar
     */
    public void getBarHashmap(String id) {

        try {
            //Grab bars from session
            JSONArray allBars = new JSONArray(session.getBars());

            //Find our bar in the mix
            for (int i = 0; i < allBars.length(); i++) {
                JSONObject barJSON = allBars.getJSONObject(i);

                //Setup the hashmap
                if (barJSON.getString("id").equals(id)) {
                    bar.put("id", barJSON.getString("id"));
                    bar.put("name", barJSON.getString("name"));
                    barName = barJSON.getString("name");
                    barName = barName.replace(" ", "");
                    bar.put("address", barJSON.getString("address"));
                    bar.put("city", barJSON.getString("city"));
                    bar.put("state", barJSON.getString("state"));
                    bar.put("number", barJSON.getString("number"));
                    bar.put("lat", barJSON.getString("lat"));
                    bar.put("long", barJSON.getString("long"));
                    bar.put("website", barJSON.getString("website"));
                    bar.put("foursquare", barJSON.getString("foursquare"));

                    //Make sure the website is navigable
                    if ( !bar.get("website").startsWith("http://") && !bar.get("website").startsWith("https://") ) {
                        bar.put("website", "http://" + bar.get("website"));
                    }

                    bar.put("deals", barJSON.getString("deals"));
                    bar.put("hours", barJSON.getString("hours"));

                    break;
                }
            }

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_bar_detail, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
