package com.tabsaver._Screens.Active;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.tabsaver.Helpers.JSONFunctions;
import com.tabsaver.R;

import org.json.JSONArray;


public class ContactActivity extends ActionBarActivity {

    //Contact information
    TextView email;
    TextView message;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        ((Button) findViewById(R.id.contact_send)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                email = (TextView) findViewById(R.id.contact_email);
                message = (TextView) findViewById(R.id.contact_message);

                if( isEmailValid( email.getText() ) ) {
                    new SendContactEmail().execute();
                }
                else{
                    email.setError(getString(R.string.error_invalid_email));
                }
            }
        });
    }

    private class SendContactEmail extends AsyncTask<Void, Void, Void> {

        JSONArray result;

        @Override
        protected Void doInBackground(Void... params) {
            result = JSONFunctions.getJSONfromURL("http://tabsaver.info/tabsaver/contactForm.php?email=" + email.getText().toString() + "&message=" + message.getText().toString());
            return null;
        }

        @Override
        protected void onPostExecute(Void args) {
            if ( true ) {
                Toast.makeText(ContactActivity.this, "Message sent! We'll try and respond within 2 business days.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ContactActivity.this, "Message send failed.. Please try again later!", Toast.LENGTH_SHORT).show();
            }

            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.closeContact) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Setup our menu items
     * @param menu Menu for this page
     * @return Not sure
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_contact, menu);
        return true;
    }


}
