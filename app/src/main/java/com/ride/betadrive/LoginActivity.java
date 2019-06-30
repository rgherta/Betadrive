package com.ride.betadrive;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.ride.betadrive.DataModels.AccountContract;
import com.ride.betadrive.Registration.CustomLoginActivity;
import com.ride.betadrive.Registration.RegistrationActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.ride.betadrive.Services.HttpService;
import com.ride.betadrive.Utils.NetworkUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.pm.PackageInfoCompat;

import android.content.Context;
import android.content.Intent;

import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private int RC_SIGN_IN=0;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    static final Integer GPS_SETTINGS = 0x7;
    GoogleApiClient client;
    LocationRequest mLocationRequest;
    PendingResult<LocationSettingsResult> result;

    //Voley
    RequestQueue queue;

    String applicationId;
    String mPackageName;
    int mPackageCode;
    String fcmToken;

    SharedPreferences sharedPreferences;
    private String sharedPrefFile = "com.ride.betadrive";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        try {
            mPackageName = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
            mPackageCode = (int) PackageInfoCompat.getLongVersionCode(this.getPackageManager().getPackageInfo(this.getPackageName(), 0));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("ApplicationId", BuildConfig.APPLICATION_ID );
        editor.putString("PackageName", mPackageName);
        editor.putInt("PackageCode", mPackageCode);
        editor.commit();

        retrieveFCMToken();

        queue = HttpService.getInstance(this).getRequestQueue();



        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken( getString(R.string.auth_web_clientid) )
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(this);

        Button signupButton = findViewById(R.id.signup);
        signupButton.setOnClickListener(this);

        Button customLogin = findViewById(R.id.custom_login);
        customLogin.setOnClickListener(this);



        mAuth = FirebaseAuth.getInstance();

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
//        client.connect();
//        askForGPS();
    }

    @Override
    public void onStop() {
        super.onStop();
//        client.disconnect();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        boolean isNew = task.getResult().getAdditionalUserInfo().isNewUser();
                        if(isNew){
                            //TODO: call and add to users collection

                            mAuth.getCurrentUser().getIdToken(false).addOnCompleteListener(tokenTask -> {

                                if (tokenTask.isSuccessful()) {

                                    Log.w(TAG,"Token found single thread after force refresh " + tokenTask.getResult().getToken());
                                    String token = tokenTask.getResult().getToken();
                                    JSONObject mRequest = NetworkUtils.createAddUserJSON(mAuth.getCurrentUser().getUid(), sharedPreferences.getString("FcmToken", null), mAuth.getCurrentUser().getEmail());
                                    URL addUserUrl = NetworkUtils.buildUrl("addUser");
                                    queue.add( makeJsonRequest(Request.Method.POST, addUserUrl, mRequest, token) );
                                }
                            });



                        }
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Snackbar.make(findViewById(R.id.login_page), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        updateUI(null);
                    }

                    // ...
                });
    }


    private JsonObjectRequest makeJsonRequest(int method, URL url, JSONObject myRequest, String token){

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
                params.put("version_code", String.valueOf(sharedPreferences.getInt("PackageCode", 0)));
                params.put("version_name", sharedPreferences.getString("PackageName", null));
                params.put("application_id", sharedPreferences.getString("ApplicationId", null));
                return params;
            }
        };


    }

        private void retrieveFCMToken(){
        // [START retrieve_current_token]
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "getInstanceId failed", task.getException());
                        return;
                    }

                    // Get new Instance ID token
                    String token = task.getResult().getToken();

                    SharedPreferences.Editor preferencesEditor = sharedPreferences.edit();
                    preferencesEditor.putString("FcmToken", token);
                    preferencesEditor.apply();

                    // Log and toast
                    String msg = token;
                    Log.w(TAG, msg);
                });
        // [END retrieve_current_token]
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.signup:
                registration();
                break;
            case R.id.custom_login:
                customLogin();
                break;
        }
    }

    private void customLogin() {
        Intent intent = new Intent(this, CustomLoginActivity.class);
        startActivity(intent);
    }

    private void registration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);


    }






    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {

            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(getApplicationContext(),"Google Signin Failed", Toast.LENGTH_LONG).show();
                // ...
            }
        }
    }

    //Update your UI to display the Google Sign-in button.
    private void updateUI(FirebaseUser account){

        getLocationPermission();

        if(account != null){
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);

        }

    }





    public void askForGPS(){
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(30 * 1000);
        mLocationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        result = LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(LoginActivity.this, GPS_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }


}

