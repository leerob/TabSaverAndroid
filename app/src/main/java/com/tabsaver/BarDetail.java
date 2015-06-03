package com.tabsaver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;


public class BarDetail extends ActionBarActivity implements OnItemSelectedListener {


    JSONArray jsonarray;
    HashMap<String, String> hashMap = new HashMap<String, String>();
    ListView listview;
    ArrayAdapter<String> arrayAdapter;
    String[] dealArr;
    String bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_detail);

        // Create TextViews
        TextView barName = (TextView) findViewById(R.id.barName);
        TextView barAddress = (TextView) findViewById(R.id.barAddress);


        Intent intent = getIntent();
        String jsonArray = intent.getStringExtra("jsonArray");
        bar = intent.getStringExtra("bar");
        barName.setText(bar);

        try {
            jsonarray = new JSONArray(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            for (int i = 0; i < jsonarray.length(); i++) {

                JSONObject obj = jsonarray.getJSONObject(i);

                if (obj.getString("name").equals(bar)) {
                    barAddress.setText(obj.getString("address") + "\n" + obj.getString("city") + ", " + obj.getString("state"));

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


        listview = (ListView) findViewById(R.id.listView);
        String day = getDayOfWeekStr();
        dealArr = hashMap.get(day).split(",");
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dealArr);
        listview.setAdapter(arrayAdapter);


        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.days_of_week, android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(getIndex(spinner, day));
        spinner.setOnItemSelectedListener(this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bar_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
