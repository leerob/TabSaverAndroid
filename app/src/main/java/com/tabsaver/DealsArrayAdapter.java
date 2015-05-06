package com.tabsaver;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;

public class DealsArrayAdapter extends BaseAdapter {

    // Declare Variables
    Context context;
    LayoutInflater inflater;
    ArrayList<String> listOfDeals;
    String resultp;
    ArrayList<String> barAssociation;
    String name;
    JSONArray jsonarray;

    public DealsArrayAdapter(Context context, ArrayList<String> listOfDeals, ArrayList<String> barAssociation, JSONArray jsonarray) {
        this.context = context;
        this.listOfDeals = listOfDeals;
        this.barAssociation = barAssociation;
        this.jsonarray = jsonarray;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        // Declare Variables
        TextView dealName;
        TextView barName;

        // Set view up
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View itemView = inflater.inflate(R.layout.deal_item, parent, false);

        // Get the position in list
        resultp = listOfDeals.get(position);
        name = barAssociation.get(position);

        // Assign TextViews
        dealName = (TextView) itemView.findViewById(R.id.deal);
        barName = (TextView) itemView.findViewById(R.id.name);
        dealName.setText(resultp);
        barName.setText(name);

        // Capture ListView item click
        itemView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(context, BarDetail.class);
                i.putExtra("jsonArray", jsonarray.toString());
                i.putExtra("bar", barAssociation.get(position));
                context.startActivity(i);

            }
        });

        return itemView;
    }

    @Override
    public int getCount() {
        return listOfDeals.size();
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