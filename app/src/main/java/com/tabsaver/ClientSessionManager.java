package com.tabsaver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
public class ClientSessionManager {

    //The shared preference file
    public SharedPreferences pref;

    public Editor edit;

    //The context
    public Context _context;


    //The name of the shared preference storing the data
    private static final String prefName = "userState";

    /**
     * Createa a new user session with the given context. Suppressed a warning that pref.edit
     * isn't actually commiting any changes.
     * @param context
     */
    @SuppressLint("CommitPrefEdits") public  ClientSessionManager(Context context) {
        _context = context;
        pref = context.getSharedPreferences(prefName, 0); // 0 - for private mode
        edit = pref.edit();

    }

    /**
     * Log the user in and store session variables
     */
    public void login(String username, String token, String bar, String city){
        //Stores KEY - VALUE
        edit.putBoolean("loggedIn", true); // Stores that we're logged in
        edit.putString("user", username); // Stores the username
        edit.putString("token", token);
        edit.putString("bar", bar);
        edit.putString("city", city);
        edit.commit(); // commit changes

    }


    /**
     * Returns the user name that is currently logged in
     * @return
     */
    public String getUser(){
        return pref.getString("user", null);
    }

    /**
     * Returns if the user is logged in or not
     * @return
     */
    public boolean isLoggedIn(){
        return pref.getBoolean("loggedIn", false);
    }

    /**
     * Returns the user's token
     * @return
     */
    public String getToken(){
        return pref.getString("token", null);
    }

    /**
     * Returns the bar
     * @return
     */
    public String getBar(){
        return pref.getString("bar", null);
    }

    /**
     * Returns the city
     * @return
     */
    public String getCity(){
        return pref.getString("city", null);
    }


    /**
     * Logout the user
     */
    public void logout() {

        edit.putBoolean("loggedIn", false); // Logged out
        edit.putString("username", null); // null user
        edit.putString("token", null); // null token
        edit.commit(); // commit changes

    }

}