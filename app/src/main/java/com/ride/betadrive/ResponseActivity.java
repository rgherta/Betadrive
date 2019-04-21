package com.ride.betadrive;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.PackageInfoCompat;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.ride.betadrive.Adapters.MapViewAdapter;
import com.ride.betadrive.DataModels.DriverContract;
import com.ride.betadrive.Interfaces.IFragmentToActivity;
import com.ride.betadrive.Utils.NetworkUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
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

    //Fields
    Address pickupAddress;
    Address destAddress;
    String payment;
    ArrayList<DriverContract> drivers;

    String mPackageName;
    int mPackageCode;


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

        mViewPager = findViewById(R.id.drivers_pager);
        drivers = bundle.getParcelableArrayList("drivers");

        poiList = new ArrayList<>();
        poiList.add(new LatLng(pickupAddress.getLatitude(), pickupAddress.getLongitude()));



        try {
            mPackageName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            mPackageCode = (int) PackageInfoCompat.getLongVersionCode(this.getPackageManager().getPackageInfo(this.getPackageName(), 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        queue = Volley.newRequestQueue(this);



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
    public void acceptDriver(DriverContract driver) {
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
        mMap.addMarker(new MarkerOptions().position(new LatLng(driver.getLocLat(), driver.getLocLong())).title("Towing truck"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));

        mMap.setPadding(50,50,50,180);

        findViewById(R.id.bottom_sheet).setVisibility(View.VISIBLE);

    }



}
