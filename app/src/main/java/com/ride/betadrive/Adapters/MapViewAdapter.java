package com.ride.betadrive.Adapters;

import android.content.Context;
import android.location.Address;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.room.util.StringUtil;
import androidx.viewpager.widget.PagerAdapter;

import com.ride.betadrive.DataModels.AcceptanceContract;
import com.ride.betadrive.Interfaces.IFragmentToActivity;
import com.ride.betadrive.R;

import java.util.ArrayList;

public class MapViewAdapter extends PagerAdapter {

    private ArrayList<AcceptanceContract> driversList;
    private Context context;
    private IFragmentToActivity mCallback;

    public MapViewAdapter(Context context, ArrayList<AcceptanceContract> argList) {
        this.context = context;
        this.driversList = argList;
    }

    @Override
    public int getCount() {
        return driversList.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //mCallback = null;
        container.removeView((View) object);
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        try {
            mCallback = (IFragmentToActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement IFragmentToActivity");
        }


        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.fragment_driver, null);

        TextView driverName = itemView.findViewById(R.id.driver_name);
        driverName.setText(driversList.get(position).getName());

        TextView arrivalTime = itemView.findViewById(R.id.title_eta);
        arrivalTime.setText(driversList.get(position).getEstimatedArrival());

        TextView driverRating = itemView.findViewById(R.id.rating);
        driverRating.setText( String.valueOf(driversList.get(position).getRating()) );

        TextView ratingView = itemView.findViewById(R.id.rating);
        ratingView.setText( String.valueOf(driversList.get(position).getRating()) );

        TextView addinfo = itemView.findViewById(R.id.add_info);
        String additionalInfo = (String) TextUtils.concat( "Service value is ", String.valueOf(driversList.get(position).getEstimatedPrice()), " RON"  );
        addinfo.setText(additionalInfo);

        Button acceptButton = itemView.findViewById(R.id.accept_ride);
        acceptButton.setOnClickListener(v -> {
            mCallback.acceptDriver(driversList.get(position));
        });

        container.addView(itemView);

        return itemView;
    }

}
