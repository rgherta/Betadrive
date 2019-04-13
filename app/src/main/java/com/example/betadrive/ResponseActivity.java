package com.example.betadrive;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;

import org.json.JSONException;
import org.json.JSONObject;


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


        TextView response = findViewById(R.id.response);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        currentUser.getIdToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {

                if (task.isSuccessful()) {

                    Log.w(TAG,"Token found single thread after force refresh "+task.getResult().getToken());
                    //response.setText(task.getResult().getToken());
                    String token = task.getResult().getToken();
                    mRequest = createRequest(pickupAddress, destAddress, payment, token );



                }
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


    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

}
