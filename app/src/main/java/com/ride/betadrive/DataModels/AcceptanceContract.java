package com.ride.betadrive.DataModels;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

public class AcceptanceContract implements Parcelable {

    private String ride;
    private String driver;
    private String estimatedArrival;
    private double estimatedPrice;
    private String name;
    private String phone;
    private String plate;
    private double ppk;
    private double rating;
    private int rides;
    private String vehicle;

    private double locLat;
    private double locLong;

    public double getLocLat() {
        return locLat;
    }

    public void setLocLat(double locLat) {
        this.locLat = locLat;
    }

    public double getLocLong() {
        return locLong;
    }

    public void setLocLong(double locLong) {
        this.locLong = locLong;
    }

    public String getRide() {
        return ride;
    }

    public void setRide(String ride) {
        this.ride = ride;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getEstimatedArrival() {
        return estimatedArrival;
    }

    public void setEstimatedArrival(String estimatedArrival) {
        this.estimatedArrival = estimatedArrival;
    }

    public double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public double getPpk() {
        return ppk;
    }

    public void setPpk(double ppk) {
        this.ppk = ppk;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getRides() {
        return rides;
    }

    public void setRides(int rides) {
        this.rides = rides;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public AcceptanceContract(String ride, String driver, String arrival, double price, String name, String phone, String plate, double ppk, double rating, int rides, String vehicle, double locLat, double locLong ){
        this.ride = ride;
        this.driver = driver;
        this.estimatedArrival = arrival;
        this.estimatedPrice = price;
        this.name = name;
        this.phone = phone;
        this.plate = plate;
        this.ppk = ppk;
        this.rating = rating;
        this.rides = rides;
        this.vehicle = vehicle;
        this.locLat = locLat;
        this.locLong = locLong;
    }


    @Override
    public String toString() {
        return (String) TextUtils.concat(
                this.ride, this.driver, this.estimatedArrival, String.valueOf(this.estimatedPrice)
                , this.name, this.phone, this.plate, String.valueOf(this.ppk), String.valueOf(this.rating), String.valueOf(this.rides), this.vehicle
                , String.valueOf(this.locLat), String.valueOf(this.locLong)
        );
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ride);
        dest.writeString(driver);
        dest.writeString(estimatedArrival);
        dest.writeDouble(estimatedPrice);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(plate);
        dest.writeDouble(ppk);
        dest.writeDouble(rating);
        dest.writeInt(rides);
        dest.writeString(vehicle);
        dest.writeDouble(locLat);
        dest.writeDouble(locLong);



    }

    protected AcceptanceContract(Parcel in) {

        ride = in.readString();
        driver = in.readString();
        estimatedArrival = in.readString();
        estimatedPrice = in.readDouble();
        name = in.readString();
        phone = in.readString();
        plate = in.readString();
        ppk = in.readDouble();
        rating = in.readDouble();
        rides = in.readInt();
        vehicle = in.readString();
        locLat = in.readDouble();
        locLong = in.readDouble();
    }



    public static final Creator<AcceptanceContract> CREATOR = new Creator<AcceptanceContract>() {
        @Override
        public AcceptanceContract createFromParcel(Parcel in) {
            return new AcceptanceContract(in);
        }

        @Override
        public AcceptanceContract[] newArray(int size) {
            return new AcceptanceContract[size];
        }
    };


}