package com.tabsaver._Screens.Extensions;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.tabsaver.Helpers.SessionStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AutoCompleteEditTextPreference extends EditTextPreference
{
    private String[] data = {"FOO", "BAR"};

    private Context context;

    //List of cities
    JSONArray fullCities;

    //Session information
    SessionStorage session;


    public AutoCompleteEditTextPreference(Context context)
    {
        super(context);

        session = new SessionStorage(context);
        setupCitiesData();
    }

    public AutoCompleteEditTextPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        session = new SessionStorage(context);
        setupCitiesData();
    }

    public AutoCompleteEditTextPreference(Context context, AttributeSet attrs,
                                          int defStyle)
    {
        super(context, attrs, defStyle);

        session = new SessionStorage(context);
        setupCitiesData();
    }

    public void setupCitiesData(){
        //Set the city options
        try {
            fullCities = new JSONArray(session.getCities());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Grab the string values for our settings suggestion
        data = new String[fullCities.length()];

        for(int i = 0; i < fullCities.length(); i++ ) {
            try {
                JSONObject temp = fullCities.getJSONObject(i);
                data[i] = temp.getString("name");
            } catch (JSONException e) {
                Toast.makeText(context, "Failed to read cities list.", Toast.LENGTH_SHORT).show();
            }

        }
    }

    /**
     * the default EditTextPreference does not make it easy to
     * use an AutoCompleteEditTextPreference field. By overriding this method
     * we perform surgery on it to use the type of edit field that
     * we want.
     */
    protected void onBindDialogView(View view)
    {
        super.onBindDialogView(view);

        // find the current EditText object
        final EditText editText = (EditText)view.findViewById(android.R.id.edit);
        // copy its layout params
        ViewGroup.LayoutParams params = editText.getLayoutParams();
        ViewGroup vg = (ViewGroup)editText.getParent();
        String curVal = editText.getText().toString();
        // remove it from the existing layout hierarchy
        vg.removeView(editText);
        // construct a new editable autocomplete object with the appropriate params
        // and id that the TextEditPreference is expecting
        mACTV = new AutoCompleteTextView(getContext());
        mACTV.setThreshold(0);
        mACTV.setLayoutParams(params);
        mACTV.setId(android.R.id.edit);
        mACTV.setText(curVal);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_dropdown_item_1line, data);
        mACTV.setAdapter(adapter);

        // add the new view to the layout
        vg.addView(mACTV);
    }

    /**
     * Used to set the string resource because it is dynamically generated
     * @param data
     */
    public void setData(String[] data){
        this.data = data;
    }

    /**
     * Because the baseclass does not handle this correctly
     * we need to query our injected AutoCompleteTextView for
     * the value to save 
     */
    protected void onDialogClosed(boolean positiveResult)
    {
        super.onDialogClosed(positiveResult);

        if (positiveResult && mACTV != null)
        {
            String value = mACTV.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }

    /**
     * again we need to override methods from the base class
     */
    public EditText getEditText()
    {
        return mACTV;
    }

    private AutoCompleteTextView mACTV = null;
    private final String TAG = "AutoCompleteEditTextPreference";
}