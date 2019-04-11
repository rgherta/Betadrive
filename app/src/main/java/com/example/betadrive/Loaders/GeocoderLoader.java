package com.example.betadrive.Loaders;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

public class GeocoderLoader extends AsyncTaskLoader<Bundle> {

    private static String TAG = GeocoderLoader.class.getSimpleName();

    private Bundle myBundle;
    private Context mContext;


    public GeocoderLoader(@NonNull Context context, Bundle args) {
        super(context);
        myBundle = args;
        mContext = context;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Nullable
    @Override
    public Bundle loadInBackground() {

        LatLng latLng       = new LatLng(myBundle.getDouble("lat"), myBundle.getDouble("long"));
        Geocoder geocoder   = new Geocoder(mContext);
        Bundle result       = null;

        try {

            List<Address> addressList=geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                Log.w(TAG, address.toString());
                result = new Bundle();
                result.putParcelable("address", address);

            }

        } catch (IOException e) {
            e.printStackTrace();
        }


        return result;
    }
}
