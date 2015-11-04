package com.tabsaver.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.tabsaver.R;

/**
 * Created by Paul on 11/2/2015.
 */
public class CustomMapsWindowAdapter implements GoogleMap.InfoWindowAdapter{

    LayoutInflater inflater=null;

    public CustomMapsWindowAdapter(LayoutInflater inflater) {
        this.inflater=inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        //Custom View
        View infoWindow = inflater.inflate(R.layout.custom_info_window, null);

        //Set title
        TextView title = (TextView) infoWindow.findViewById(R.id.info_title);
        title.setText(marker.getTitle());

        //Set snippet
        TextView snippet = (TextView) infoWindow.findViewById(R.id.info_title);
        snippet.setText(marker.getSnippet());

        return infoWindow;
    }

    @Override
    public View getInfoContents(Marker marker) {
        //Just replace contents - We dont use this
        return null;
    }
}
