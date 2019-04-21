package com.ride.betadrive;

import androidx.core.content.pm.PackageInfoCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ride.betadrive.DataModels.DriverContract;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConfirmationActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    public static final String TAG = ConfirmationActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Address  pickupAddress;
    private Address  destAddress;
    private String payment = "cash";

    //Google map
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds.Builder mBounds = new LatLngBounds.Builder();

    //Voley
    RequestQueue queue;
    String mPackageName;
    int mPackageCode;

    FABProgressCircle circleFab;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();


        Intent intent = getIntent();
        pickupAddress = intent.getParcelableExtra("pickupAddress");
        destAddress = intent.getParcelableExtra("destAddress");

        TextView pickupTxt = findViewById(R.id.pickup_address);
        pickupTxt.setText(pickupAddress.getAddressLine(0));

        TextView destTxt = findViewById(R.id.dir_address);
        destTxt.setText(destAddress.getAddressLine(0));

        try {
            mPackageName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            mPackageCode = (int) PackageInfoCompat.getLongVersionCode(this.getPackageManager().getPackageInfo(this.getPackageName(), 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


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
                response -> {
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

                }, error -> {
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
                getApiResponse();
                break;
        }
    }

    private void getApiResponse() {
        circleFab = findViewById(R.id.fabProgressCircle);
        circleFab.show();

        currentUser.getIdToken(false).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                Log.w(TAG,"Token found single thread after force refresh " + task.getResult().getToken());
                //response.setText(task.getResult().getToken());
                String token = task.getResult().getToken();
                JSONObject mRequest = createRequest(pickupAddress, destAddress, payment, currentUser.getUid());
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
                                Log.w(TAG, response.toString());
                                startResponseActivity(response);
                                }
                , error -> {
            Log.w(TAG, error.toString());
        }

        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("authorization", token);
                params.put("version_code", String.valueOf(mPackageCode));
                params.put("version_name", mPackageName);
                return params;
            }
        };


    }

    private void startResponseActivity(JSONObject response){

        ArrayList<DriverContract> driversList = new ArrayList<>();

        try {
            JSONArray driversJson = response.getJSONArray("drivers");
            for (int i = 0; i < driversJson.length(); i++ ) {
                JSONObject location = driversJson.getJSONObject(i).getJSONObject("Loc");

                DriverContract driver = new DriverContract(
                        driversJson.getJSONObject(i).getString("uid")
                        , driversJson.getJSONObject(i).getString("Name")
                        , driversJson.getJSONObject(i).getString("Plate")
                        , (short) driversJson.getJSONObject(i).getInt("Status")
                        , new LatLng( location.getDouble("_latitude"), location.getDouble("_longitude") )
                        , driversJson.getJSONObject(i).getString("Cur")
                        , driversJson.getJSONObject(i).getDouble("PPK")
                );
                driversList.add(driver);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        Bundle message = new Bundle();
        message.putParcelable("pickup_address", pickupAddress);
        message.putParcelable("dest_address", destAddress);
        message.putString("payment", payment);
        message.putParcelableArrayList("drivers", driversList);

        Handler handler=new Handler();
        Runnable r= () -> {
            circleFab.hide() ;
            Intent intent = new Intent(this, ResponseActivity.class);
            intent.putExtras(message);
            startActivity(intent);
        };
        handler.postDelayed(r, 2000);


    }




}
