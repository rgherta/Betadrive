package com.ride.betadrive;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.ride.betadrive.Utils.NetworkUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class ResponseActivity extends AppCompatActivity {

    public static final String TAG = ResponseActivity.class.getSimpleName();
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    //Voley
    RequestQueue queue;

    //Fields
    Address pickupAddress;
    Address destAddress;
    String payment;
    JSONObject mRequest;
    TextView mResponse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        pickupAddress = bundle.getParcelable("pickup_address");
        destAddress = bundle.getParcelable("dest_address");
        payment = bundle.getString("payment");

        queue = Volley.newRequestQueue(this);


        mResponse = findViewById(R.id.response);

        currentUser.getIdToken(false).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                Log.w(TAG,"Token found single thread after force refresh " + task.getResult().getToken());
                //response.setText(task.getResult().getToken());
                String token = task.getResult().getToken();
                mRequest = createRequest(pickupAddress, destAddress, payment, currentUser.getUid());
                URL hailUrl = NetworkUtils.buildHailUrl();
                queue.add( makeJsonRequest(Request.Method.POST, hailUrl, mRequest, token) );



            }
        });

    }

    private static JSONObject createRequest(Address pickupAddress, Address destAddress, String payment, String requester){

        JSONObject response = null;

        try {

            response = new JSONObject();
            JSONObject pickup = new JSONObject();
            JSONObject destination = new JSONObject();

            pickup.put("lat", pickupAddress.getLatitude());
            pickup.put("long", pickupAddress.getLongitude());
            destination.put("lat", destAddress.getLatitude());
            destination.put("long", destAddress.getLongitude());

            response.put("payment", payment);
            response.put("pickup", pickup);
            response.put("destination", destination);
            response.put("requester", requester);



        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.w(TAG, response.toString());

        return response;

    }



    private JsonObjectRequest makeJsonRequest(int method, URL url, JSONObject myRequest, String token){

       return new JsonObjectRequest(method
                                    , url.toString()
                                    , myRequest
                                    , response -> {
                                           mResponse.setText("Response: " + response.toString());
                                       }
                                    , error -> {
                                            mResponse.setText("Response: " + error.toString());
                                            Log.w(TAG, "Json request error");
                                        }

       ){
           @Override
           public Map<String, String> getHeaders() {
               Map<String, String> params = new HashMap<>();
               params.put("authorization", token);
               return params;
           }
       };


    }



    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

}
