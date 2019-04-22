package com.ride.betadrive.Services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class HttpService {
    private static HttpService instance;
    private RequestQueue requestQueue;
    private static Context ctx;

    private HttpService(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();

    }

    public static synchronized HttpService getInstance(Context context) {
        if (instance == null) {
            instance = new HttpService(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }


}