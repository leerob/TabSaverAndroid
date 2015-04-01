package com.tabsaver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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


public class MainActivity extends Activity {

    // Declare Variables
    JSONArray jsonarray;
    ProgressDialog mProgressDialog;
    ArrayList<HashMap<String, String>> arraylist;
    ListView listview;
    ListArrayAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new DownloadJSON().execute();
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
            case R.id.action_list:
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                return true;
            case R.id.action_settings:
                //openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // DownloadJSON AsyncTask
    private class DownloadJSON extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Create a Progress Dialog
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Android JSON Parse Tutorial");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Create an array
            arraylist = new ArrayList<HashMap<String, String>>();
            // Retrieve JSON Objects from the given URL address
            jsonarray = JSONFunctions.getJSONfromURL("http://tabsaver.info/connectAmes.php");

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
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {

            listview = (ListView) findViewById(R.id.listview);
            adapter = new ListArrayAdapter(MainActivity.this, arraylist);
            listview.setAdapter(adapter);
            mProgressDialog.dismiss();


        }


    }
}
