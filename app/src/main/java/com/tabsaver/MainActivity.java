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
import java.util.HashMap;


public class MainActivity extends ActionBarActivity {

    JSONArray jsonarray;
    ArrayList<HashMap<String, String>> arraylist;
    ListView listview;
    ListArrayAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        String jsonArray = intent.getStringExtra("jsonArray");

        try {
            jsonarray = new JSONArray(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

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

                // Set the JSON Objects into the array
                arraylist.add(map);
            }

        } catch (JSONException e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        listview = (ListView) findViewById(R.id.listview);
        adapter = new ListArrayAdapter(MainActivity.this, arraylist);
        listview.setAdapter(adapter);

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
            case R.id.search:
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
