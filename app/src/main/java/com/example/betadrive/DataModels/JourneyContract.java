package com.example.betadrive.DataModels;


import com.google.android.gms.maps.model.LatLng;

public class JourneyContract {

    private AccountContract account;
    private LatLng departure;
    private LatLng destination;


    public JourneyContract(){

    }

    public AccountContract getAccount() { return account; }

    public void setAccount(AccountContract account) { this.account = account; }

    public LatLng getDeparture() { return departure; }

    public void setDeparture(LatLng departure) { this.departure = departure; }

    public LatLng getDestination() { return destination; }

    public void setDestination(LatLng destination) { this.destination = destination; }

}
