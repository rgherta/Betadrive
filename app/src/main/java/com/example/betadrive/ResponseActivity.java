package com.example.betadrive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.betadrive.Utils.NetworkUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;


public class ResponseActivity extends AppCompatActivity {

    public static final String TAG = ResponseActivity.class.getSimpleName();
    private FirebaseAuth mAuth;

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

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        pickupAddress = bundle.getParcelable("pickup_address");
        destAddress = bundle.getParcelable("dest_address");
        payment = bundle.getString("payment");

        queue = Volley.newRequestQueue(this);


        mResponse = findViewById(R.id.response);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        currentUser.getIdToken(false).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                Log.w(TAG,"Token found single thread after force refresh "+task.getResult().getToken());
                //response.setText(task.getResult().getToken());
                String token = task.getResult().getToken();
                mRequest = createRequest(pickupAddress, destAddress, payment, token );
                URL hailUrl = NetworkUtils.buildHailUrl("api", "hail");
                queue.add( makeJsonRequest(Request.Method.POST, hailUrl, mRequest) );



            }
        });

    }

    private static JSONObject createRequest(Address pickupAddress, Address destAddress, String payment, String token){

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
            response.put("token", token);



        } catch (JSONException e) {
            e.printStackTrace();
        }

        return response;

    }



    private JsonObjectRequest makeJsonRequest(int method, URL url, JSONObject myRequest){

       return new JsonObjectRequest(method, url.toString()
                                    , myRequest
                                    , response -> mResponse.setText("Response: " + response.toString())
               , error -> {
                   // TODO: Handle error
                   Log.w(TAG, "Json request error");

               });


    }



    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

}
