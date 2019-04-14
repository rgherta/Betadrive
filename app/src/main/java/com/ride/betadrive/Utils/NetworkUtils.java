package com.ride.betadrive.Utils;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;

public class NetworkUtils {

    private static final String DIRECTIONS_BASE = "https://maps.googleapis.com/maps/api/directions";
    private static final String FUNCTIONS_BASE = "https://us-central1-beta-94f76.cloudfunctions.net";

    public static URL buildDirectionsUrl(String apiType, String origin, String destination, String API_KEY){
    Uri builtUri = Uri.parse(DIRECTIONS_BASE).buildUpon()
            .appendPath(apiType)
            .appendQueryParameter("origin", origin)
            .appendQueryParameter("destination", destination)
            .appendQueryParameter("key", API_KEY)
            .build();
    URL url = null;

    try{
            url = new URL(builtUri.toString());
        } catch(MalformedURLException e){
            e.printStackTrace();
        }

        return url;

    }



    public static URL buildHailUrl(String apiBase, String hailEndpoint){
        Uri builtUri = Uri.parse(FUNCTIONS_BASE).buildUpon()
                .appendPath(apiBase)
                .appendPath(hailEndpoint)
                .build();
        URL url = null;

        try{
            url = new URL(builtUri.toString());
        } catch(MalformedURLException e){
            e.printStackTrace();
        }

        return url;

    }


}
