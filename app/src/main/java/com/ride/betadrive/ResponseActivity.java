package com.ride.betadrive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.ride.betadrive.Adapters.ChatMessagesAdapter;
import com.ride.betadrive.Adapters.MapViewAdapter;
import com.ride.betadrive.DataModels.AcceptanceContract;
import com.ride.betadrive.DataModels.DriverContract;
import com.ride.betadrive.DataModels.MessageContract;
import com.ride.betadrive.Interfaces.IFragmentToActivity;
import com.ride.betadrive.Services.HttpService;
import com.ride.betadrive.Services.MyFirebaseMessagingService;
import com.ride.betadrive.Utils.NetworkUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.text.DateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ResponseActivity extends AppCompatActivity  implements OnMapReadyCallback, IFragmentToActivity {

    public static final String TAG = ResponseActivity.class.getSimpleName();
    private static final int DEFAULT_ZOOM = 17;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    //Google map
    private GoogleMap mMap;
    private ViewPager mViewPager;
    private ArrayList<LatLng> poiList;

    //Voley
    RequestQueue queue;
    String mPackageName;
    String mApplicationId;
    int mPackageCode;

    //Fields
    Address pickupAddress;
    Address destAddress;
    int payment;
    ArrayList<AcceptanceContract> drivers;
    ArrayList<MessageContract> chatMessages;

    //Recycler
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    SharedPreferences sharedPreferences;
    private String sharedPrefFile = "com.ride.betadrive";

    FirebaseFirestore db;
    Marker  driverMarker = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        db = FirebaseFirestore.getInstance();

        sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        mPackageName = sharedPreferences.getString("PackageName", null);
        mPackageCode = sharedPreferences.getInt("PackageCode", 0);
        mApplicationId = sharedPreferences.getString("ApplicationId", null);

//        retrieveToken();
        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
//        if (getIntent().getExtras() != null) {
//            for (String key : getIntent().getExtras().keySet()) {
//                Object value = getIntent().getExtras().get(key);
//                Log.d(TAG, "Key: " + key + " Value: " + value);
//            }
//        }
        // [END handle_data_extras]

        //Chat Messages Recycler
        recyclerView = findViewById(R.id.recycler_messages);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        chatMessages = new ArrayList<MessageContract>();
        mAdapter = new ChatMessagesAdapter(chatMessages); //TODO: get messages arrayluist cu mesajeel
        recyclerView.setAdapter(mAdapter);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        pickupAddress = bundle.getParcelable("pickup_address");
        destAddress = bundle.getParcelable("dest_address");
        payment = bundle.getInt("payment");

        mViewPager = findViewById(R.id.drivers_pager);
        drivers = bundle.getParcelableArrayList("drivers");

        poiList = new ArrayList<>();
        poiList.add(new LatLng(pickupAddress.getLatitude(), pickupAddress.getLongitude()));

        queue = HttpService.getInstance(this).getRequestQueue();


        MyFirebaseMessagingService.BUS.observe(this, o -> {
           MessageContract newMessage = (MessageContract) o;
           chatMessages.add(newMessage);
           mAdapter.notifyDataSetChanged();
           recyclerView.smoothScrollToPosition(mAdapter.getItemCount());
        });

    }


    @Override
    protected void onStart() {
        super.onStart();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.addMarker(new MarkerOptions().position(new LatLng(pickupAddress.getLatitude(), pickupAddress.getLongitude())).title("You are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(pickupAddress.getLatitude(), pickupAddress.getLongitude()), DEFAULT_ZOOM));

        //add viewpager array
        mViewPager.setAdapter(new MapViewAdapter(this, drivers));


    }




    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }


    }

    @Override
    public void acceptDriver(AcceptanceContract driver) {
        Log.w(TAG, driver.toString());
        findViewById(R.id.drivers_pager).setVisibility(View.GONE);

        double dx = pickupAddress.getLatitude() - driver.getLocLat();
        double dy = pickupAddress.getLongitude() - driver.getLocLong();
        double myKm = 0.005;
        poiList.add(new LatLng(pickupAddress.getLatitude() + dx + myKm, pickupAddress.getLongitude() + dy + myKm));
        poiList.add(new LatLng(pickupAddress.getLatitude() + dx + myKm, pickupAddress.getLongitude() - dy - myKm));
        poiList.add(new LatLng(pickupAddress.getLatitude() - dx - myKm, pickupAddress.getLongitude() - dy - myKm));
        poiList.add(new LatLng(pickupAddress.getLatitude() - dx - myKm, pickupAddress.getLongitude() + dy + myKm));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : poiList) {
            builder.include(point);
        };

        LatLngBounds bounds = builder.build();

        MarkerOptions mo = new MarkerOptions().position(new LatLng(driver.getLocLat(), driver.getLocLong())).title("Towing truck");
        driverMarker = mMap.addMarker(mo);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

        mMap.setPadding(50,50,50,180);

        findViewById(R.id.bottom_sheet).setVisibility(View.VISIBLE);
        sendButtonListener(driver);

        currentUser.getIdToken(false).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                Log.w(TAG,"Token found single thread after force refresh " + task.getResult().getToken());
                String token = task.getResult().getToken();
                JSONObject mRequest = NetworkUtils.acceptDriverJSON(driver.getRide(), driver.getDriver());
                URL acceptDriverUrl = NetworkUtils.buildUrl("updateRideDriver");
                queue.add( makeRideUpdateJSON(Request.Method.POST, acceptDriverUrl, mRequest, token) );
            }
        });

        realtimeDriverListener(driver.getDriver());

    }


    private void sendButtonListener(AcceptanceContract driverData){
        EditText sendButton = findViewById(R.id.send_button);
        sendButton.setOnTouchListener((v, event) -> {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            if(event.getAction() == MotionEvent.ACTION_UP) {
                if(event.getRawX() >= (sendButton.getRight() - sendButton.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                    Log.w(TAG, "clicked send message");

                    Date currentTime = Calendar.getInstance().getTime();
                    MessageContract newMessage = new MessageContract(sendButton.getText().toString(), currentUser.getUid(),driverData.getDriver(), currentTime, driverData.getRide() );
                    chatMessages.add(newMessage);
                    mAdapter.notifyDataSetChanged();
                    recyclerView.smoothScrollToPosition(mAdapter.getItemCount());
                    sendButton.setText("");



                    currentUser.getIdToken(false).addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {
                            Log.w(TAG,"Token found single thread after force refresh " + task.getResult().getToken());
                            String token = task.getResult().getToken();
                            JSONObject mRequest = NetworkUtils.sendMessageJSON(newMessage);
                            URL messageUrl = NetworkUtils.buildUrl("sendMessage");
                            queue.add( sendMessageRequest(Request.Method.POST, messageUrl, mRequest, token) );
                        }
                    });



                    return true;
                }
            }
            return false;
        });
    }


    private JsonObjectRequest makeRideUpdateJSON(int method, URL url, JSONObject myRequest, String token){

        return new JsonObjectRequest(method
                , url.toString()
                , myRequest
                , response -> {
            Log.w(TAG, response.toString());
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
                params.put("application_id", mPackageName);
                return params;
            }
        };


    }



    private JsonObjectRequest sendMessageRequest(int method, URL url, JSONObject myRequest, String token){

        return new JsonObjectRequest(method
                , url.toString()
                , myRequest
                , response -> {
            Log.w(TAG, response.toString());
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
                params.put("application_id", mPackageName);
                return params;
            }
        };


    }


    private void realtimeDriverListener(String driverUid){
        final DocumentReference docRef = db.collection("drivers").document(driverUid);
        docRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed.", e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Log.w(TAG, "Current data: " + snapshot.getData());
                driverMarker.setPosition( new LatLng(snapshot.getGeoPoint("loc").getLatitude(), snapshot.getGeoPoint("loc").getLongitude()));

            } else {
                Log.w(TAG, "Current data: null");
            }
        });

    }


//    private void retrieveToken(){
//        // [START retrieve_current_token]
//        FirebaseInstanceId.getInstance().getInstanceId()
//                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                        if (!task.isSuccessful()) {
//                            Log.w(TAG, "getInstanceId failed", task.getException());
//                            return;
//                        }
//
//                        // Get new Instance ID token
//                        String token = task.getResult().getToken();
//
//                        // Log and toast
//                        String msg = token;
//                        Log.w(TAG, msg);
//                        Toast.makeText(ResponseActivity.this, msg, Toast.LENGTH_SHORT).show();
//                    }
//                });
//        // [END retrieve_current_token]
//    }



}
