package com.tabsaver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class DealsArrayAdapter extends BaseAdapter {

    // Declare Variables
    Context context;
    LayoutInflater inflater;
    HashMap<String, String> barHash = new HashMap<String, String>();
    String[] dealArr;

    public DealsArrayAdapter(Context context, ArrayList<HashMap<String, String>> arraylist) {
        this.context = context;
        barHash = arraylist.get(0);

        // Determine Day of Week
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        String dealsStr = "";
        switch (day) {
            case Calendar.SUNDAY:
                dealsStr = barHash.get("Sunday");
                break;
            case Calendar.MONDAY:
                dealsStr = barHash.get("Monday");
                break;
            case Calendar.TUESDAY:
                dealsStr = barHash.get("Tuesday");
                break;
            case Calendar.WEDNESDAY:
                dealsStr = barHash.get("Wednesday");
                break;
            case Calendar.THURSDAY:
                dealsStr = barHash.get("Thursday");
                break;
            case Calendar.FRIDAY:
                dealsStr = barHash.get("Friday");
                break;
            case Calendar.SATURDAY:
                dealsStr = barHash.get("Saturday");
                break;
        }
        dealArr = dealsStr.split(",");
    }

    public View getView(final int position, View convertView, ViewGroup parent) {

        // Set view up
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.deal_item, parent, false);

        // Assign TextViews
        TextView deal = (TextView) itemView.findViewById(R.id.deal);
        deal.setText(dealArr[position]);

        return itemView;
    }

    @Override
    public int getCount() {
        return dealArr.length;
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