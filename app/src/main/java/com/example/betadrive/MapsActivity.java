package com.example.betadrive;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.BundleCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import com.example.betadrive.DataModels.AccountContract;
import com.example.betadrive.Loaders.GeocoderLoader;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;


import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, LoaderManager.LoaderCallbacks<Bundle>  {

    private static final String TAG = MapsActivity.class.getSimpleName() ;

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION    = 1;
    private static final int DEFAULT_ZOOM                                   = 17;
    private static final int AUTOCOMPLETE_REQUEST_CODE                      = 1;
    private static final int ASYNCTASKLOADER_ID                             = 1;


    //Google map
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds.Builder mBounds = new LatLngBounds.Builder();
    private DrawerLayout drawerLayout;
    private AccountContract account;
    private boolean mLocationPermissionGranted;


    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLastKnownLocation;
    private LatLng mDefaultLocation;
    private PlacesClient placesClient;

    //Loader
    private LoaderManager.LoaderCallbacks<Bundle> callback = MapsActivity.this;
    private LoaderManager loaderManager;
    private Loader<Bundle> asyncTaskLoader;

    //LOADERS
    private Bundle loaderBundle = new Bundle();

    //View
    private TextView                pickupPlace;
    private TextView                destPlace;
    private FloatingActionButton    fab;
    private FloatingActionButton    fab_next;
    private View                    locationButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);


        //RESOURCES & VIEWS
        pickupPlace = findViewById(R.id.pickup_place);
        destPlace = findViewById(R.id.btn_destination);
        fab = findViewById(R.id.fab);
        fab_next = findViewById(R.id.fab_next);
        locationButton =  findViewById(Integer.parseInt("2"));

        //LOADER
        loaderManager = getSupportLoaderManager();
        asyncTaskLoader = loaderManager.getLoader(ASYNCTASKLOADER_ID);

        if(asyncTaskLoader == null){
            loaderManager.initLoader(ASYNCTASKLOADER_ID, loaderBundle, callback);
        } else {
            loaderManager.restartLoader(ASYNCTASKLOADER_ID, loaderBundle, callback);
        }

        //GET PROFILE DETAILS
        getUserDetails();



    }

    @Override
    protected void onStart() {
        super.onStart();

//        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
//        LocationProvider locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
//        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        //if (!gpsEnabled) {
         //       enableLocationSettings();
        //}




        //GET LOCATION
        getLocationPermission();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mDefaultLocation = new LatLng(44.439663, 26.096306);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //SETUP PLACES API
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);

        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.ADDRESS);
        FindCurrentPlaceRequest request =  FindCurrentPlaceRequest.builder(placeFields).build();

        @SuppressLint("MissingPermission")
        Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
        placeResponse.addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                FindCurrentPlaceResponse response = task.getResult();
                PlaceLikelihood myPlace = response.getPlaceLikelihoods().get(0);
                pickupPlace.setText(myPlace.getPlace().getAddress());
                Log.w(TAG, String.format("Place '%s' has likelihood: %f",
                        myPlace.getPlace().getAddress(),
                        myPlace.getLikelihood()));

            } else {
                Exception exception = task.getException();
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                }
            }
        });


    }



    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }



    private void getUserDetails(){

        Intent intent = getIntent();
        Bundle message = intent.getExtras();
        account = message.getParcelable("account");

        Log.w(TAG, account.toString());


        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    // set item as selected to persist highlight
                    menuItem.setChecked(true);
                    // close drawer when item is tapped
                    //drawerLayout.closeDrawers();

                    switch (menuItem.getItemId()) {
                        case R.id.signout:
                            signOut();
                            break;
                    }

                    return true;
                });


        CardView cardView = findViewById(R.id.map_menu);
        cardView    .setOnClickListener(this);
        fab         .setOnClickListener(this);
        fab_next    .setOnClickListener(this);

        View nav_header = navigationView.getHeaderView(0);

        TextView username = nav_header.findViewById(R.id.header_title);
        username.setText(account.getFULL_NAME());

        TextView email = nav_header.findViewById(R.id.header_subtitle);
        email.setText(account.getUSER_EMAIL());

        ImageView photo = nav_header.findViewById(R.id.profile_photo);
        if (account.getUSER_PHOTO().equals("anon")) {
            Picasso.get().load(R.drawable.anon).into(photo);
        } else {
            Picasso.get().load(account.getUSER_PHOTO()).into(photo);
        }

    }


    public void checkOut(View view) {

        List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);
        Intent intent = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN, fields)
                .setCountry("RO") //Autodetect
                .setTypeFilter(TypeFilter.ADDRESS)
                .build(this);
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.w(TAG, place.toString());
                destPlace.setText(place.getAddress());
                fab.setVisibility(View.GONE);
                fab_next.setVisibility(View.VISIBLE);



            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                // TODO: Handle the error.
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
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

        mMap.setMyLocationEnabled(true); //TODO: Handle permission
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        if(locationButton != null) locationButton.setVisibility(View.GONE);


        getDeviceLocation();


        //MOVED IN BACKGOUDN TASK
        mMap.setOnCameraIdleListener(() -> {
            LatLng latLng=mMap.getCameraPosition().target;
            Log.w(TAG, "idle at" + latLng.toString());
            loaderBundle.putDouble("lat", latLng.latitude);
            loaderBundle.putDouble("long", latLng.longitude);
            loaderManager.restartLoader(ASYNCTASKLOADER_ID, loaderBundle, callback);

        });

        mMap.setOnCameraMoveStartedListener(i -> pickupPlace.setText("Searching address..."));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.map_menu:
                drawerLayout.openDrawer(GravityCompat.END);
                break;
            case R.id.btn_destination:
                Log.w(TAG, "Destination");
                break;
            case R.id.fab_next:
                goConfirm();
                break;
            case R.id.fab:
                if(mMap != null && locationButton != null) locationButton.callOnClick();
                break;
        }
    }

    private void signOut() {

        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getBaseContext(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                                Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
    }

    private void goConfirm(){
        Intent intent = new Intent(this, ConfirmationActivity.class);
        //intent.putExtra(MOVIE_EXTRA, movieList.get(position));
        startActivity(intent);
    }


    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    getDeviceLocation();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    mLocationPermissionGranted = false;
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }


    private void getDeviceLocation() {

        try {
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(
                        new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    mLastKnownLocation = location;

                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(mLastKnownLocation.getLatitude(),
                                                    mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));


                                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                            .findFragmentById(R.id.map);
                                    View mapView = mapFragment.getView();


                                    View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
                                    RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                                    // position on right bottom
                                    int[] ruleList = rlp.getRules();
                                    for (int i = 0; i < ruleList.length; i ++) {
                                        rlp.removeRule(i);
                                    }
                                    //rlp.addRule(RelativeLayout.ALIGN_BOTTOM, RelativeLayout.TRUE);
                                    //rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                                    locationButton.setPadding(30,0,0,0);


                                } else {
                                    Log.w("maps", "Current location is null. Using defaults.");
                                    Log.w("maps", "Exception: %s");
                                    //mMap.addMarker(new MarkerOptions().position(mDefaultLocation).title("Default Location"));
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                                    mMap.getUiSettings().setMyLocationButtonEnabled(false);

                                };

                            }

                        });
            }

            } catch(SecurityException e)  {
                Log.e("Exception: %s", e.getMessage());
            }
        }



        //LOADER INTERFACE METHODS
        @NonNull
        @Override
        public Loader<Bundle> onCreateLoader(int id, @Nullable Bundle args) {

            return new GeocoderLoader(this, args);

        }

        @Override
        public void onLoadFinished(@NonNull Loader<Bundle> loader, Bundle data) {

            if(data != null){
               Address myAddress = data.getParcelable("address");

               pickupPlace.setText(myAddress.getAddressLine(0).toString());

            }

        }

        @Override
        public void onLoaderReset(@NonNull Loader<Bundle> loader) {
        }





}