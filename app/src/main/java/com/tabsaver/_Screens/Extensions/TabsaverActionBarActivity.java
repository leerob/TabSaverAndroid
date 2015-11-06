package com.tabsaver._Screens.Extensions;

import android.support.v7.app.ActionBarActivity;

import com.tabsaver.R;

/**
 * Created by Paul on 11/5/2015.
 */
public abstract class TabsaverActionBarActivity extends ActionBarActivity {

    /**
     * Add logo to action bar
     */
    public void setIconAsLogo(ActionBarActivity actionBarActivity){
        actionBarActivity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        actionBarActivity.getSupportActionBar().setLogo(R.mipmap.ic_actionbar_logo);
        actionBarActivity.getSupportActionBar().setDisplayUseLogoEnabled(true);
    }
}
