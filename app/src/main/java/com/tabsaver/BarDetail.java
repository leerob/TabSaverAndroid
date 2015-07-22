package com.tabsaver;

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
import java.util.Locale;


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
    Boolean dealOutOfDateSent;

    //Storing and retrieving session information
    ClientSessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_detail);

        //Setup the session
        session = new ClientSessionManager(getApplicationContext());

        //Grab our barID
        Intent intent = getIntent();
        barName = intent.getStringExtra("BarName");
        dealOutOfDateSent = false;

        //Load this bars information
        getBarHashmap(barName);

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
            if ( true ) {
                Toast.makeText(BarDetail.this, "We've received your update. We've sent the interns to investigate!", Toast.LENGTH_SHORT).show();
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
        listview = (ListView) findViewById(R.id.listView);

        //Set the bar's name
        ((TextView) findViewById(R.id.barName)).setText(bar.get("name"));

        //Set the bar's address
        barAddress.setText(bar.get("address") + ", " + bar.get("town") + ", " + bar.get("state"));

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
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(bar.get("website")));
                startActivity(browserIntent);
            }
        });

        //Listener to navigate to the address on click
        barAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr="+bar.get("lat") +","+( Double.valueOf(bar.get("long")) * -1 )));
                startActivity(intent);
            }
        });

        //Parse and display the current deals for the day
        dayOfWeek = getDayOfWeekAsString();
        dealsForSelectedDay = bar.get(dayOfWeek).split(","); //TODO: Get rid of this damn comma stuff
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
        int day = calendar.get(Calendar.DAY_OF_WEEK);

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

        switch (parent.getItemAtPosition(pos).toString()) {
            case "Sunday":
                dealsForSelectedDay = bar.get("Sunday").split(",");
                break;
            case "Monday":
                dealsForSelectedDay = bar.get("Monday").split(",");
                break;
            case "Tuesday":
                dealsForSelectedDay = bar.get("Tuesday").split(",");
                break;
            case "Wednesday":
                dealsForSelectedDay = bar.get("Wednesday").split(",");
                break;
            case "Thursday":
                dealsForSelectedDay = bar.get("Thursday").split(",");
                break;
            case "Friday":
                dealsForSelectedDay = bar.get("Friday").split(",");
                break;
            case "Saturday":
                dealsForSelectedDay = bar.get("Saturday").split(",");
                break;
        }

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
     * @param barName the current bar
     */
    public void getBarHashmap(String barName) {

        try {
            //Grab bars from session
            JSONArray allBars = new JSONArray(session.getBars());

            //Find our bar in the mix
            for (int i = 0; i < allBars.length(); i++) {
                JSONObject barJSON = allBars.getJSONObject(i);

                //Setup the hashmap
                if (barJSON.getString("name").equals(barName)) {
                    bar.put("id", barJSON.getString("BarId"));
                    bar.put("name", barJSON.getString("name"));
                    bar.put("address", barJSON.getString("address"));
                    bar.put("town", barJSON.getString("town"));
                    bar.put("state", barJSON.getString("state"));
                    bar.put("number", barJSON.getString("number"));
                    bar.put("lat", barJSON.getString("lat"));
                    bar.put("long", barJSON.getString("long"));
                    bar.put("website", barJSON.getString("website"));

                    //Make sure the website is navigable
                    if ( !bar.get("website").startsWith("http://") ) {
                        bar.put("website", "http://" + bar.get("website"));
                    }

                    bar.put("Monday", barJSON.getString("Monday"));
                    bar.put("Tuesday", barJSON.getString("Tuesday"));
                    bar.put("Wednesday", barJSON.getString("Wednesday"));
                    bar.put("Thursday", barJSON.getString("Thursday"));
                    bar.put("Friday", barJSON.getString("Friday"));
                    bar.put("Saturday", barJSON.getString("Saturday"));
                    bar.put("Sunday", barJSON.getString("Sunday"));
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
