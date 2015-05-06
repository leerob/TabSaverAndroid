package com.tabsaver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class MainActivity extends ActionBarActivity {

    JSONArray jsonarray;
    ArrayList<HashMap<String, String>> arraylist;
    ListView listview;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get JSON information on bars
        Intent intent = getIntent();
        String jsonArray = intent.getStringExtra("jsonArray");


        try {
            jsonarray = new JSONArray(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        displayListView(false);

    }

    public void displayListView(boolean sortByDeals) {

        //Sort by bar
        if ( !sortByDeals ) {
            arraylist = new ArrayList<HashMap<String, String>>();

            try {
                for (int i = 0; i < jsonarray.length(); i++) {
                    HashMap<String, String> map = new HashMap<String, String>();
                    JSONObject obj = jsonarray.getJSONObject(i);
                    // Retrieve JSON Objects
                    map.put("id",  String.valueOf(i));
                    map.put("name", obj.getString("name"));
                    map.put("Monday", obj.getString("Monday"));
                    map.put("Tuesday", obj.getString("Tuesday"));
                    map.put("Wednesday", obj.getString("Wednesday"));
                    map.put("Thursday", obj.getString("Thursday"));
                    map.put("Friday", obj.getString("Friday"));
                    map.put("Saturday", obj.getString("Saturday"));
                    map.put("Sunday", obj.getString("Sunday"));
                    map.put("distance", obj.getString("distance"));

                    // Set the JSON Objects into the array
                    arraylist.add(map);
                }

            } catch (JSONException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            //Sort the list
            ArrayList<HashMap<String, String>> newList = new ArrayList<HashMap<String, String>>();

            while (arraylist.isEmpty() != true) {
                HashMap<String, String> min = arraylist.get(0);

                for (int j = 0; j < arraylist.size(); j++) {
                    HashMap<String, String> current = arraylist.get(j);

                    if (Double.valueOf(min.get("distance")) > Double.valueOf(current.get("distance"))) {
                        min = current;
                    }

                }

                arraylist.remove(min);
                newList.add(min);
            }

            listview = (ListView) findViewById(R.id.listview);
            ListArrayAdapter adapter = new ListArrayAdapter(MainActivity.this, newList, jsonarray);
            listview.setAdapter(adapter);
            //Sort by deals
        } else {
            //Sort the list by deals

            String deals = "";
            String day = getDayOfWeek();
            ArrayList<String> barAssociation = new ArrayList<String>();
            ArrayList<String> newListOfDeals = new ArrayList<String>(10);

            //Make an arraylist of the deals for this week
            try {
                for (int i = 0; i < jsonarray.length(); i++) {
                    String cur = jsonarray.getJSONObject(i).getString(day) ;
                    String thisBar = jsonarray.getJSONObject(i).getString("name");
                    String[] theseDeals = cur.split(",");
                    for(int j = 0; j < theseDeals.length; j++ ) {
                        if ( theseDeals[j].contains("$")) {
                            newListOfDeals.add(theseDeals[j]);
                            barAssociation.add(thisBar);
                        }
                    }
                }

                for(int i = 0; i < newListOfDeals.size(); i++ ) {
                    double minVal = Double.valueOf(newListOfDeals.get(i).split(" ")[0].replace("$", ""));
                    int min = i;
                    for(int j = i + 1; j < newListOfDeals.size(); j++){
                        double curVal = Double.valueOf(newListOfDeals.get(j).split(" ")[0].replace("$", ""));
                        if ( curVal < minVal ) {
                            minVal = curVal;
                            min = j;
                        }

                    }

                    //Switch deal
                    String temp = newListOfDeals.get(i);
                    newListOfDeals.set(i, newListOfDeals.get(min));
                    newListOfDeals.set(min, temp);

                    //Switch bar association
                    temp = barAssociation.get(i);
                    barAssociation.set(i, barAssociation.get(min));
                    barAssociation.set(min, temp);

                }
            } catch (JSONException e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }




            listview = (ListView) findViewById(R.id.listview);
            DealsArrayAdapter adapter = new DealsArrayAdapter(MainActivity.this, newListOfDeals, barAssociation, jsonarray);
            listview.setAdapter(adapter);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list_view, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.listByDeals:
                displayListView(true);
                return true;
            case R.id.listByBars:
                displayListView(false);
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getDayOfWeek(){
        // Determine Day of Week
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        String dealsStr = "";
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

        return null;
    }
}
