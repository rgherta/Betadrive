package com.ride.betadrive.DataModels;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;

public class DriverContract implements Parcelable {

    private String uid;
    private String cur;
    private double locLat;
    private double locLong;
    private String driverName;
    private double ppk;
    private String plate;
    private short status;
    private int rides;
    private double rating;

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public int getRides() { return rides; }
    public void setRides(int rides) { this.rides = rides; }

    public double getLocLat() { return locLat; }
    public void setLocLat(double locLat) { this.locLat = locLat; }

    public double getLocLong() { return locLong; }
    public void setLocLong(double locLong) { this.locLong = locLong; }


    public String getCur() { return cur; }
    public void setCur(String cur) { this.cur = cur; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String name) { this.driverName = name; }

    public double getPpk() { return ppk; }
    public void setPpk(double ppk) { this.ppk = ppk; }

    public String getPlate() { return plate; }
    public void setPlate(String plate) { this.plate = plate; }

    public short getStatus() { return status; }
    public void setStatus(short status) { this.status = status; }

    public String getUid() { return uid; }

    public void setUid(String uid) { this.uid = uid; }


    public DriverContract( String uid, String driverName, String plate, short status, LatLng loc, String cur, double ppk, int rides, double rating ){

        this.setUid(uid);
        this.setDriverName(driverName);
        this.setPlate(plate);
        this.setStatus(status);
        this.setLocLat(loc.latitude);
        this.setLocLong(loc.longitude);
        this.setCur(cur);
        this.setPpk(ppk);
        this.setRides(rides);
        this.setRating(rating);

    }



    @NonNull
    @Override
    public String toString() {
        return  " uid: " + this.getUid() +
                " driverName: " + this.getDriverName() +
                " plate: " + this.getPlate() +
                " status: " + this.getStatus() +
                " locLong: " + this.getLocLong() +
                " locLat: " + this.getLocLat() +
                " cur: " + this.getCur() +
                " ppk: " + this.getPpk() +
                " rides: " + this.getRides() +
                " rating: " + this.getRating();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    protected DriverContract(Parcel in) {

        uid = in.readString();
        cur = in.readString();
        locLat = in.readDouble();
        locLong = in.readDouble();
        driverName = in.readString();
        ppk = in.readDouble();
        plate = in.readString();
        status = (short) in.readInt();
        rides = in.readInt();
        rating = in.readDouble();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uid);
        dest.writeString(cur);
        dest.writeDouble(locLat);
        dest.writeDouble(locLong);
        dest.writeString(driverName);
        dest.writeDouble(ppk);
        dest.writeString(plate);
        dest.writeInt(status);
        dest.writeInt(rides);
        dest.writeDouble(rating);

    }



    public static final Creator<DriverContract> CREATOR = new Creator<DriverContract>() {
        @Override
        public DriverContract createFromParcel(Parcel in) {
            return new DriverContract(in);
        }

        @Override
        public DriverContract[] newArray(int size) {
            return new DriverContract[size];
        }
    };


}
