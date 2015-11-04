package com.tabsaver._Screens.Inactive;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.tabsaver.Helpers.SessionStorage;
import com.tabsaver.Helpers.JSONFunctions;
import com.tabsaver.R;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class AdminActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    //Current view and day
    private ArrayList<String> dayOptions = new ArrayList<String>();
    private ArrayList<String> currentOptions = new ArrayList<String>();

    //EditTexts to show deals
    private EditText deal1;
    private EditText deal2;
    private EditText deal3;
    private EditText deal4;
    private EditText deal5;
    private EditText deal6;
    private EditText deal7;
    private EditText deal8;
    private EditText deal9;
    private EditText deal10;


    public int currentDay = -1;
    private SessionStorage session;
    private View mProgressView;
    private String bar;
    private String city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        session = new SessionStorage(getApplicationContext());
        bar = session.getBar();
        city = session.getCity();

        Spinner spinner = (Spinner) findViewById(R.id.daySpinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.daysSpinner, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        populateDays();

        // Deal with submit button
        Button button = (Button)findViewById(R.id.clientSubmitButton);

        button.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.dayView);
                        mProgressView.setVisibility(View.VISIBLE);
                        linearLayout.setVisibility(View.INVISIBLE);
                        ArrayList<String> newOptions = new ArrayList<String>();
                        for (int i = 0; i < currentOptions.size(); i++ ) {
                            if ( !currentOptions.get(i).toUpperCase().contains("NONE") && currentOptions.get(i) != "") {
                                newOptions.add(currentOptions.get(i));
                            }
                        }
                        SubmitDealUpdate SDU = new SubmitDealUpdate(bar, city, dayOptions.get(currentDay), newOptions);
                        SDU.execute();
                    }
                }
        );

        mProgressView = findViewById(R.id.progressBar);

    }

    public void populateListeners(){
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.dayView);
        deal1 = (EditText) ((LinearLayout) linearLayout).getChildAt(0);
        deal2 = (EditText) ((LinearLayout) linearLayout).getChildAt(1);
        deal3 = (EditText) ((LinearLayout) linearLayout).getChildAt(2);
        deal4 = (EditText) ((LinearLayout) linearLayout).getChildAt(3);
        deal5 = (EditText) ((LinearLayout) linearLayout).getChildAt(4);
        deal6 = (EditText) ((LinearLayout) linearLayout).getChildAt(5);
        deal7 = (EditText) ((LinearLayout) linearLayout).getChildAt(6);
        deal8 = (EditText) ((LinearLayout) linearLayout).getChildAt(7);
        deal9 = (EditText) ((LinearLayout) linearLayout).getChildAt(8);
        deal10 = (EditText) ((LinearLayout) linearLayout).getChildAt(9);

        deal1.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                currentOptions.set(0, deal1.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        deal2.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                currentOptions.set(1, deal2.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        deal3.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                currentOptions.set(2, deal3.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        deal4.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                currentOptions.set(3, deal4.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        deal5.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                currentOptions.set(4, deal5.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        deal6.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                currentOptions.set(5, deal6.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        deal7.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                currentOptions.set(6, deal7.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        deal8.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                currentOptions.set(7, deal8.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        deal9.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                currentOptions.set(8, deal9.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        deal10.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                currentOptions.set(9, deal10.getText().toString());
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

    }




    public void populateDays(){
        dayOptions.add("Select...");
        dayOptions.add("Monday");
        dayOptions.add("Tuesday");
        dayOptions.add("Wednesday");
        dayOptions.add("Thursday");
        dayOptions.add("Friday");
        dayOptions.add("Saturday");
        dayOptions.add("Sunday");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.clientLogout:
                finish();
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.dayView);

        if ( id != 0 ) {
            mProgressView.setVisibility(View.VISIBLE);
            linearLayout.setVisibility(View.INVISIBLE);
            GetDealsForDay grabDeals = new GetDealsForDay(bar, city, dayOptions.get(pos));
            grabDeals.execute((Void) null);
            currentDay = pos;

        } else {
            ((EditText) ((LinearLayout) linearLayout).getChildAt(0)).setText("No Day Selected");
        }


    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    private class GetDealsForDay extends AsyncTask<Void, Void, Boolean> {

        public ArrayList<String> deals;
        public String day;
        private String bar;
        private String city;
        private String message;

        GetDealsForDay(String bar, String city, String day) {
            deals = new ArrayList<String>();
            this.bar = bar;
            this.city = city;
            this.day = day;
        }
        public void setDay(String day){
            this.day = day;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            List<NameValuePair> postParams = new ArrayList<NameValuePair>(4);
            postParams.add(new BasicNameValuePair("city", city));
            postParams.add(new BasicNameValuePair("day", day));
            postParams.add(new BasicNameValuePair("bar", bar));

            JSONObject requestProps = JSONFunctions.getJSONfromURLPost("http://tabsaver.info/grabDealsForDay.php", postParams);

            while ( null == requestProps ) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Try and check if it succeeded

            try {
                String success = requestProps.getString("Success");

                //Return true on success
                if ( success.equals("1") ) {
                    //TODO: add token json response to session variable
                    for(int i = 1; i <= 10; i++) {
                        deals.add(requestProps.getString(String.valueOf(i)));
                    }

                    return true;

                    //Set error message and return false.
                } else {
                    message = "Invalid username or password.";
                    return false;
                }

                //Off chance that some weird shit happens
            } catch (JSONException e) {
                //Something went wrong - typically JSON value doesn't exist (success).
                message = "An error occured. Please try again later.";
                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {


            if (success) {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.dayView);
                int num = 0;

                for (int i = 0; i < ((LinearLayout) linearLayout).getChildCount(); i++) {
                    ((EditText)((LinearLayout) linearLayout).getChildAt(i)).setText(deals.get(i));
                    num++;
                }

                for (int i = num; i < 10; i++){
                    ((EditText)((LinearLayout) linearLayout).getChildAt(i)).setText("    ");
                }

                linearLayout.setVisibility(View.VISIBLE);
                mProgressView.setVisibility(View.INVISIBLE);

                populateListeners();

                currentOptions = deals;
            } else {
                Toast.makeText(AdminActivity.this, "Failed!", Toast.LENGTH_SHORT).show();// display toast
            }
        }

    }

    private class SubmitDealUpdate extends AsyncTask<Void, Void, Boolean> {

        public ArrayList<String> deals;
        public String day;
        private String bar;
        private String city;
        private String message;

        SubmitDealUpdate(String bar, String city, String day, ArrayList<String> deals) {
            this.deals = deals;
            this.bar = bar;
            this.city = city;
            this.day = day;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            List<NameValuePair> postParams = new ArrayList<NameValuePair>(4);
            postParams.add(new BasicNameValuePair("city", city));
            postParams.add(new BasicNameValuePair("day", day));
            postParams.add(new BasicNameValuePair("bar", bar));

            //Add 1->10 to array
            for(int i = 0; i < deals.size(); i++ ) {
                postParams.add(new BasicNameValuePair("deal"+(i+1), deals.get(i)));
            }

            JSONObject requestProps = JSONFunctions.getJSONfromURLPost("http://tabsaver.info/processChange.php", postParams);

            while ( null == requestProps ) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            //Try and check if it succeeded

            try {
                String success = requestProps.getString("Success");

                //Return true on success
                if ( success.equals("1") ) {

                    return true;

                    //Set error message and return false.
                } else {
                    message = "Invalid username or password.";
                    return false;
                }

                //Off chance that some weird shit happens
            } catch (JSONException e) {
                //Something went wrong - typically JSON value doesn't exist (success).
                message = "An error occured. Please try again later.";
                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {


            if (success) {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.dayView);
                mProgressView.setVisibility(View.INVISIBLE);
                linearLayout.setVisibility(View.VISIBLE);
                Toast.makeText(AdminActivity.this, "Updated deals succesfully!", Toast.LENGTH_LONG).show();// display toast

            } else {
                Toast.makeText(AdminActivity.this, "Failed to post changes!", Toast.LENGTH_SHORT).show();// display toast
            }
        }

    }



}