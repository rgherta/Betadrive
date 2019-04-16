package com.ride.betadrive;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ride.betadrive.Utils.NetworkUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.PolyUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;

public class ConfirmationActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    public static final String TAG = ConfirmationActivity.class.getSimpleName();

    private Address  pickupAddress;
    private Address  destAddress;
    private String payment = "cash";

    //Google map
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds.Builder mBounds = new LatLngBounds.Builder();

    //Voley
    RequestQueue queue;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);


        Intent intent = getIntent();
        pickupAddress = intent.getParcelableExtra("pickupAddress");
        destAddress = intent.getParcelableExtra("destAddress");

        TextView pickupTxt = findViewById(R.id.pickup_address);
        pickupTxt.setText(pickupAddress.getAddressLine(0));

        TextView destTxt = findViewById(R.id.dir_address);
        destTxt.setText(destAddress.getAddressLine(0));


        //get directions
        queue = Volley.newRequestQueue(this);

        Log.w(TAG, pickupAddress.toString());
        Log.w(TAG, destAddress.toString());

       FloatingActionButton fab = findViewById(R.id.next_fab);
       fab.setOnClickListener(this);


    }



    private StringRequest makeRequest(int method, URL url){
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(method, url.toString(),
                (Response.Listener<String>) response -> {
                    JSONObject mResponse = null;
                    try {
                        mResponse = new JSONObject(response.toString());

                        String points = mResponse.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                        List<LatLng> pointsArray = PolyUtil.decode(points);

                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for(LatLng poi : pointsArray){
                            builder.include(poi);
                        }
                        LatLngBounds bounds = builder.build();

                       PolylineOptions lineOptions = new PolylineOptions();
                       lineOptions.addAll(pointsArray);

                       mMap.addPolyline(lineOptions);

                       mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 50));


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, (Response.ErrorListener) error -> {
                    Log.w(TAG, error.toString());

                });

        stringRequest.setTag(TAG);
        // Add the request to the RequestQueue.
        return stringRequest;
    }



    @Override
    protected void onStart() {
        super.onStart();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_route);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);

        //Get Directions
        String pickup = String.valueOf(pickupAddress.getLatitude()) + ',' + String.valueOf(pickupAddress.getLongitude());
        String destination = String.valueOf(destAddress.getLatitude()) + ',' + String.valueOf(destAddress.getLongitude());
        URL url = NetworkUtils.buildDirectionsUrl("json", pickup, destination, getString(R.string.google_maps_key));
        queue.add( makeRequest(Request.Method.GET, url) );


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.next_fab:
                Bundle message = new Bundle();
                message.putParcelable("pickup_address", pickupAddress);
                message.putParcelable("dest_address", destAddress);
                message.putString("payment", payment);

                Intent intent = new Intent(this, ResponseActivity.class);
                intent.putExtras(message);

                startActivity(intent);
                break;
        }
    }
}
