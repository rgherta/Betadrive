package com.example.betadrive.Utils;

import android.content.Context;
import android.net.Uri;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.net.MalformedURLException;
import java.net.URL;

public class NetworkUtils {

    private static final String DIRECTIONS_BASE = "https://maps.googleapis.com/maps/api/directions";

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

}
