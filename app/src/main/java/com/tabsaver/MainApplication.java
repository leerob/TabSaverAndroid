package com.tabsaver;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Paul on 6/3/2015.
 */
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable Local Datastore.
        Parse.enableLocalDatastore(getApplicationContext());
        Parse.initialize(getApplicationContext(), "mZ1wJCdlDowI28IzRpZ9ycIFkm0TXUYA33EoC3n8", "4TaNynj1NN0UDlXMP3iQQb6WGAAE5Gp9IOBcVMkW");

    }
}
