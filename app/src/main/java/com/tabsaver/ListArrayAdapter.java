package com.tabsaver;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class ListArrayAdapter extends BaseAdapter {

    // Declare Variables
    Context context;
    LayoutInflater inflater;
    ArrayList<HashMap<String, String>> data;
    HashMap<String, String> resultp = new HashMap<String, String>();

    public ListArrayAdapter(Context context, ArrayList<HashMap<String, String>> arraylist) {
        this.context = context;
        data = arraylist;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        // Declare Variables
        TextView name;
        TextView distance;
        TextView deals;

        // Set view up
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.list_item, parent, false);

        // Get the position in list
        resultp = data.get(position);

        // Assign TextViews
        name = (TextView) itemView.findViewById(R.id.name);
        distance = (TextView) itemView.findViewById(R.id.distance);
        deals = (TextView) itemView.findViewById(R.id.deals);

        String dealsStr = resultp.get("Monday");
        //String[] dealArr = dealsStr.split(",");

        String formattedDeals = dealsStr.replace(",", "\n");

        // Capture position and set results to the TextViews
        name.setText(resultp.get("name"));
        deals.setText(formattedDeals);
        distance.setText("0.000 mi");


        // Capture ListView item click
        itemView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //resultp = data.get(position);
                //Intent intent = new Intent(context, SingleItemView.class);
                //intent.putExtra("rank", resultp.get(MainActivity.RANK));
                //context.startActivity(intent);

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