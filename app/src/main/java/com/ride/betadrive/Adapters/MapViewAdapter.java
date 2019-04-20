package com.ride.betadrive.Adapters;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.viewpager.widget.PagerAdapter;

import com.ride.betadrive.DataModels.DriverContract;
import com.ride.betadrive.R;

import java.util.ArrayList;

public class MapViewAdapter extends PagerAdapter {

    ArrayList<DriverContract> driversList;
    Context context;

    public MapViewAdapter(Context context, ArrayList<DriverContract> argList) {
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
        container.removeView((View) object);
    }

    @Override
    public View instantiateItem(ViewGroup container, int position) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.fragment_driver, null);

        TextView driverName = itemView.findViewById(R.id.driver_name);
        driverName.setText(driversList.get(position).getDriverName());

        container.addView(itemView);

        return itemView;
    }

}
