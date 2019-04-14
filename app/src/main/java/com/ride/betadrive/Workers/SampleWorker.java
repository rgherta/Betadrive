package com.ride.betadrive.Workers;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SampleWorker extends Worker {

    private static String TAG = SampleWorker.class.getSimpleName();


    public SampleWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);


    }

    @NonNull
    @Override
    public Result doWork() {

        Context applicationContext = getApplicationContext();

        try{



        } catch (Throwable throwable) {

            Log.e(TAG, "Error applying blur", throwable);
            return Result.failure();
        }

        return Worker.Result.success();

    };


}
