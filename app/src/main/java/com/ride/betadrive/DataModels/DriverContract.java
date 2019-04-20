package com.ride.betadrive.DataModels;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;

public class DriverContract implements Parcelable {
    private String cur;
    private double locLat;
    private double locLong;
    private String driverName;
    private double ppk;
    private String plate;
    private short status;

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

    public DriverContract( String driverName, String plate, short status, LatLng loc, String cur, double ppk ){

        this.setDriverName(driverName);
        this.setPlate(plate);
        this.setStatus(status);
        this.setLocLat(loc.latitude);
        this.setLocLong(loc.longitude);
        this.setCur(cur);
        this.setPpk(ppk);

    }



    @NonNull
    @Override
    public String toString() {
        return  "driverName: " + this.getDriverName() +
                "plate: " + this.getPlate() +
                "status: " + this.getStatus() +
                "locLong: " + this.getLocLong() +
                "locLat: " + this.getLocLat() +
                "cur: " + this.getCur() +
                "ppk: " + this.getPpk();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    protected DriverContract(Parcel in) {

        cur = in.readString();
        locLat = in.readDouble();
        locLong = in.readDouble();
        driverName = in.readString();
        ppk = in.readDouble();
        plate = in.readString();
        status = (short) in.readInt();

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cur);
        dest.writeDouble(locLat);
        dest.writeDouble(locLong);
        dest.writeString(driverName);
        dest.writeDouble(ppk);
        dest.writeString(plate);
        dest.writeInt(status);

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
