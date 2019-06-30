package com.ride.betadrive.Utils;

import android.location.Address;
import android.net.Uri;
import android.util.Log;

import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.firestore.GeoPoint;
import com.ride.betadrive.DataModels.MessageContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NetworkUtils {

    private static final String DIRECTIONS_BASE = "https://maps.googleapis.com/maps/api/directions";
    private static final String FUNCTIONS_BASE = "https://europe-west1-beta-94f76.cloudfunctions.net";
    private static final String TAG = NetworkUtils.class.getSimpleName();

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



    public static URL buildUrl(String mycase){
        Uri builtUri=null;
        URL url = null;

        switch(mycase){
            case "hail":
                builtUri = Uri.parse(FUNCTIONS_BASE).buildUpon()
                        .appendPath("api")
                        .appendPath("rides")
                        .appendPath("hail")
                        .build();
                break;
            case "addUser":
                builtUri = Uri.parse(FUNCTIONS_BASE).buildUpon()
                        .appendPath("api")
                        .appendPath("users")
                        .appendPath("add")
                        .build();
                break;
            case "updateRideDriver":
                builtUri = Uri.parse(FUNCTIONS_BASE).buildUpon()
                        .appendPath("api")
                        .appendPath("rides")
                        .appendPath("accept")
                        .build();

                break;
            case "sendMessage":
                builtUri = Uri.parse(FUNCTIONS_BASE).buildUpon()
                        .appendPath("api")
                        .appendPath("messages")
                        .appendPath("send")
                        .build();

                break;
        }


        try{
            url = new URL(builtUri.toString());
        } catch(MalformedURLException e){
            e.printStackTrace();
        }

        return url;

    }

    public static JSONObject createRideRequest(Address pickupAddress, Address destAddress, int payment, String requester){

        JSONObject requestJson = null;

        try {

            requestJson = new JSONObject();
            JSONObject pickup = new JSONObject();
            JSONObject destination = new JSONObject();

            pickup.put("lat", pickupAddress.getLatitude());
            pickup.put("long", pickupAddress.getLongitude());
            destination.put("lat", destAddress.getLatitude());
            destination.put("long", destAddress.getLongitude());
            requestJson.put("payment", payment);
            requestJson.put("pickup", pickup);
            requestJson.put("destination", destination);
            requestJson.put("requester", requester);
            Log.w(TAG, "instantiated here");


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.w(TAG, requestJson.toString());

        return requestJson;

    }

    public static JSONObject createAddUserJSON(String uid, String fcmToken, String email){

        JSONObject requestJson = null;

        try {

            requestJson = new JSONObject();
            requestJson.put("uid", uid);
            requestJson.put("email", email);
            requestJson.put("category", 0);
            requestJson.put("fcm_token", fcmToken);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.w(TAG, requestJson.toString());

        return requestJson;

    }


    public static JSONObject acceptDriverJSON(String ride, String driver){

        JSONObject requestJson = null;

        try {

            requestJson = new JSONObject();
            requestJson.put("ride", ride);
            requestJson.put("driver", driver);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.w(TAG, requestJson.toString());

        return requestJson;

    }

    public static JSONObject sendMessageJSON(MessageContract message){

        JSONObject requestJson = null;

        try {

            requestJson = new JSONObject();
            requestJson.put("sender", message.getSender());
            requestJson.put("receiver", message.getReceiver());
            requestJson.put("message", message.getMessage());
            requestJson.put("timestamp", message.getTimestamp());
            requestJson.put("ride", message.getRide());

            Log.w(TAG, requestJson.toString());
            return requestJson;


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.w(TAG, requestJson.toString());

        return requestJson;

    }



}
