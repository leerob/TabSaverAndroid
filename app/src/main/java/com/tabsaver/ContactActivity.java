package com.tabsaver;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class ContactActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        ((Button) findViewById(R.id.contact_send)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView email = (TextView) findViewById(R.id.contact_email);
                TextView message = (TextView) findViewById(R.id.contact_message);

                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , email.getText());
                i.putExtra(Intent.EXTRA_SUBJECT, "TabSaver Contact Us");
                i.putExtra(Intent.EXTRA_TEXT   , message.getText());
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));

                    //Now finish and navigate back to settings
                    finish();
                    Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(settings);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(ContactActivity.this, "Unable to send message. You don't have any email clients!", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
