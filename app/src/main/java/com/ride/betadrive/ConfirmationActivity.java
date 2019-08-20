package com.ride.betadrive;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ride.betadrive.DataModels.AcceptanceContract;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfirmationActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    public static final String TAG = ConfirmationActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Address  pickupAddress;
    private Address  destAddress;
    private int payment = 0;

    //Google map
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds.Builder mBounds = new LatLngBounds.Builder();

    //Voley
    RequestQueue queue;
    String mPackageName;
    String mApplicationId;
    int mPackageCode;
    String token;

    FABProgressCircle circleFab;
    SharedPreferences sharedPreferences;
    private String sharedPrefFile = "com.ride.betadrive";

    String points = "";
    int distance = 0;
    int duration = 0;

    CountDownTimer timer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        mPackageName = sharedPreferences.getString("PackageName", null);
        mPackageCode = sharedPreferences.getInt("PackageCode", 0);
        mApplicationId = sharedPreferences.getString("ApplicationId", null);

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

        //get directions
       // queue = HttpService.getInstance(this).getRequestQueue();
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

                        points = mResponse.getJSONArray("routes").getJSONObject(0).getJSONObject("overview_polyline").getString("points");
                        distance = mResponse.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("distance").getInt("value");
                        duration = mResponse.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONObject("duration").getInt("value");

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
                findViewById(R.id.next_fab).setEnabled(false);
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
                String token = task.getResult().getToken();
                this.token = token;
                JSONObject mRequest = NetworkUtils.createRideRequest(pickupAddress, destAddress, points, distance, duration, payment, currentUser.getUid());
                URL hailUrl = NetworkUtils.buildUrl("hail");
                queue.add( makeJsonRequest(Request.Method.POST, hailUrl, mRequest, token) );
            }
        });

    }




    private JsonObjectRequest makeJsonRequest(int method, URL url, JSONObject myRequest, String token){

        return new JsonObjectRequest(method
                , url.toString()
                , myRequest
                , response -> {
                                Log.w(TAG, response.toString());
                                    try {

                                        if( response.has("ride_drivers") ){
                                            JSONArray drivers = response.getJSONArray("ride_drivers");
                                            if(drivers.length() != 0) startResponseActivity(response);
                                        } else {
                                            checkStatus(response);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }


                                }
                , error -> {
                    Log.w(TAG, error.toString());
                }){

                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> params = new HashMap<>();
                        params.put("authorization", token);
                        params.put("version_code", String.valueOf(mPackageCode));
                        params.put("version_name", mPackageName);
                        params.put("application_id", mPackageName);
                        return params;
                    }
        };


    }


    private void startResponseActivity(JSONObject response){

        ArrayList<AcceptanceContract> contractList = new ArrayList<>();

        try {
            JSONArray driversJson = response.getJSONArray("ride_drivers");

            for (int i = 0; i < driversJson.length(); i++ ) {

                JSONObject location = driversJson.getJSONObject(i).getJSONObject("loc");

                AcceptanceContract contract = new AcceptanceContract(
                        driversJson.getJSONObject(i).getString("ride")
                        , driversJson.getJSONObject(i).getString("driver")
                        , driversJson.getJSONObject(i).getString("arrival")
                        , driversJson.getJSONObject(i).getDouble("price")
                        , driversJson.getJSONObject(i).getString("name")
                        , driversJson.getJSONObject(i).getString("phone")
                        , driversJson.getJSONObject(i).getString("plate")
                        , driversJson.getJSONObject(i).getDouble("ppk")
                        , driversJson.getJSONObject(i).getDouble("rating")
                        , driversJson.getJSONObject(i).getInt("rides")
                        , driversJson.getJSONObject(i).getString("vehicle")
                        , location.getDouble("_latitude")
                        , location.getDouble("_longitude")
                );
                contractList.add(contract);
            }



        } catch (JSONException e) {
            e.printStackTrace();
        }


            Bundle message = new Bundle();
            message.putParcelable("pickup_address", pickupAddress);
            message.putParcelable("dest_address", destAddress);
            message.putInt("payment", payment);
            message.putParcelableArrayList("drivers", contractList);

            circleFab.hide() ;
            timer.cancel();

            Intent intent = new Intent(this, ResponseActivity.class);
            intent.putExtras(message);
            startActivity(intent);




    }

    private void checkStatus(JSONObject response){
        String status;
        String ride;

        try {
            status = response.getString("status");
            ride = response.getString("ride");

            if(!status.equals("ok")){
               throw new JSONException("wrong status");
            }


            timer = new CountDownTimer(50000, 10000) {

                public void onTick(long millisUntilFinished) {
                    checkDrivers(currentUser.getUid(), ride );
                    Toast.makeText(getBaseContext(), "10 seconds passed", Toast.LENGTH_SHORT).show();

                }

                public void onFinish() {
                    Toast.makeText(getBaseContext(), "Done", Toast.LENGTH_SHORT).show();
                    finish();

                }
            }.start();




        } catch (JSONException e) {
            e.printStackTrace();
        }

    }



    private void checkDrivers(String user, String ride){

        JSONObject mRequest = NetworkUtils.createCheckRideRequest(user, ride);
        URL url = NetworkUtils.buildUrl("checkDrivers");
        queue.add( makeJsonRequest(Request.Method.POST, url, mRequest, token) );


    }







}
