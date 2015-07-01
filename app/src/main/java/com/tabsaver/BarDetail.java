package com.tabsaver;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class BarDetail extends ActionBarActivity implements OnItemSelectedListener {


    JSONArray jsonarray;
    HashMap<String, String> hashMap = new HashMap<String, String>();
    ListView listview;
    ArrayAdapter<String> arrayAdapter;
    String[] dealArr;
    String bar;

    //Views
    TextView barName;
    TextView barAddress;

    //Values
    String barPhoneNumber;
    String barWebsiteAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_detail);


        //Setup our textviews
        TextView barAddress = (TextView) findViewById(R.id.barAddress);
        final TextView barWebsite = (TextView) findViewById(R.id.barWebsite);
        final TextView barPhone = (TextView) findViewById(R.id.barPhone);
        final TextView barName = (TextView) findViewById(R.id.barName);

        //Grab some passed along data
        Intent intent = getIntent();
        String jsonArray = intent.getStringExtra("jsonArray");

        //convert to json
        try {
            jsonarray = new JSONArray(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        bar = intent.getStringExtra("bar");
        barName.setText(bar);


        loadImage();

        //Grab our deal info
        try {
            for (int i = 0; i < jsonarray.length(); i++) {

                JSONObject obj = jsonarray.getJSONObject(i);

                if (obj.getString("name").equals(bar)) {
                    //Setup bar address
                    barAddress.setText(obj.getString("address") + ", " + obj.getString("town") + ", " + obj.getString("state"));

                    //Setup our phone number
                    barPhoneNumber = obj.getString("number");
                    if(!barPhoneNumber.equals("No Number")){
                        Log.d("PHONE", barPhoneNumber);
                        barPhone.setText("(" + barPhoneNumber.substring(0,3) + ") " + barPhoneNumber.substring(3,6) + " - " + barPhoneNumber.substring(6,10));
                    }
                    else{
                        barPhone.setText("No Number");
                    }

                    //Setup our website address
                    barWebsiteAddress = obj.getString("website");
                    barWebsite.setText(barWebsiteAddress);

                    //Store our deals
                    hashMap.put("Monday", obj.getString("Monday"));
                    hashMap.put("Tuesday", obj.getString("Tuesday"));
                    hashMap.put("Wednesday", obj.getString("Wednesday"));
                    hashMap.put("Thursday", obj.getString("Thursday"));
                    hashMap.put("Friday", obj.getString("Friday"));
                    hashMap.put("Saturday", obj.getString("Saturday"));
                    hashMap.put("Sunday", obj.getString("Sunday"));


                }
            }

        } catch (JSONException e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        //Set our phone number listener and intent
        barPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!barPhoneNumber.equals("No Number")){
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + barPhoneNumber));
                    startActivity(intent);
                }
            }
        });

        //Set our website listener and intent
        barWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( !barWebsiteAddress.startsWith("http://") ) {
                    barWebsiteAddress = "http://" + barWebsiteAddress;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(barWebsiteAddress));
                startActivity(browserIntent);
            }
        });

        //Parse and display the current deals for the day
        listview = (ListView) findViewById(R.id.listView);
        String day = getDayOfWeekStr();
        dealArr = hashMap.get(day).split(",");
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dealArr);
        listview.setAdapter(arrayAdapter);

        //Setup the spinner selection listener
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.days_of_week, R.layout.custom_spinner);
        adapter.setDropDownViewResource(R.layout.custom_dropdown);
        spinner.setAdapter(adapter);
        spinner.setSelection(getIndex(spinner, day));
        spinner.setOnItemSelectedListener(this);

        getSupportActionBar().hide();
    }

    public void loadImage(){
        final ImageView barImage = (ImageView) findViewById(R.id.imageView);

        ParseQuery findImage = new ParseQuery("BarPhotos");
        findImage.whereEqualTo("barName", bar);


        try {
            //Query for barName's photo
            ArrayList<ParseObject> temp = (ArrayList<ParseObject>) findImage.find();

            //now get objectId
            String objectId = temp.get(0).getObjectId();

            //Do some weird shit and get and cast our image
            ParseObject imageHolder = findImage.get(objectId);
            ParseFile image = (ParseFile) imageHolder.get("imageFile");
            byte[] imageFile = image.getData();

            //Turn it into a bitmap and set our display image
            Bitmap bmp = BitmapFactory.decodeByteArray(imageFile, 0, imageFile.length);

            barImage.setImageBitmap(bmp);

        } catch (ParseException e ) {
            Toast.makeText(getApplicationContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
        } catch (NullPointerException e ) {
            Toast.makeText(getApplicationContext(), "Failed to load image.", Toast.LENGTH_SHORT).show();
        }

    }

    public String getDayOfWeekStr(){

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
                dealArr = hashMap.get("Sunday").split(",");
                break;
            case "Monday":
                dealArr = hashMap.get("Monday").split(",");
                break;
            case "Tuesday":
                dealArr = hashMap.get("Tuesday").split(",");
                break;
            case "Wednesday":
                dealArr = hashMap.get("Wednesday").split(",");
                break;
            case "Thursday":
                dealArr = hashMap.get("Thursday").split(",");
                break;
            case "Friday":
                dealArr = hashMap.get("Friday").split(",");
                break;
            case "Saturday":
                dealArr = hashMap.get("Saturday").split(",");
                break;
        }

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dealArr);
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
