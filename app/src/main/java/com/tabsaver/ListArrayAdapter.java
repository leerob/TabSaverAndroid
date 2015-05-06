package com.tabsaver;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class ListArrayAdapter extends BaseAdapter {

    // Declare Variables
    Context context;
    LayoutInflater inflater;
    ArrayList<HashMap<String, String>> data;
    HashMap<String, String> resultp = new HashMap<String, String>();
    JSONArray jsonarray;
    String barName;

    public ListArrayAdapter(Context context, ArrayList<HashMap<String, String>> arraylist, JSONArray json) {
        this.context = context;
        data = arraylist;
        jsonarray = json;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        // Declare Variables
        TextView name;
        TextView distance;
        TextView deals;

        // Set view up
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View itemView = inflater.inflate(R.layout.list_item, parent, false);

        // Get the position in list
        resultp = data.get(position);

        // Assign TextViews
        name = (TextView) itemView.findViewById(R.id.deal);
        distance = (TextView) itemView.findViewById(R.id.distance);
        deals = (TextView) itemView.findViewById(R.id.deals);

        // Determine Day of Week
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        String dealsStr = "";
        switch (day) {
            case Calendar.SUNDAY:
                dealsStr = resultp.get("Sunday");
                break;
            case Calendar.MONDAY:
                dealsStr = resultp.get("Monday");
                break;
            case Calendar.TUESDAY:
                dealsStr = resultp.get("Tuesday");
                break;
            case Calendar.WEDNESDAY:
                dealsStr = resultp.get("Wednesday");
                break;
            case Calendar.THURSDAY:
                dealsStr = resultp.get("Thursday");
                break;
            case Calendar.FRIDAY:
                dealsStr = resultp.get("Friday");
                break;
            case Calendar.SATURDAY:
                dealsStr = resultp.get("Saturday");
                break;
        }

        String formattedDeals = dealsStr.replace(",", "\n");

        // Capture position and set results to the TextViews
        barName = resultp.get("name");
        name.setText(barName);
        deals.setText(formattedDeals);
        distance.setText(resultp.get("distance") + " mi");


        // Capture ListView item click
        itemView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(context, BarDetail.class);
                i.putExtra("jsonArray", jsonarray.toString());
                i.putExtra("bar", data.get(position).get("name"));
                context.startActivity(i);

            }
        });
        return itemView;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}