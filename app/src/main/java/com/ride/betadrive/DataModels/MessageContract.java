package com.ride.betadrive.DataModels;

import java.time.LocalTime;

public class MessageContract {

    private String message;
    private String sender;
    private String receiver;
    private LocalTime timestamp;

    public String getRide() {
        return ride;
    }

    public void setRide(String ride) {
        this.ride = ride;
    }

    private String ride;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }


    public LocalTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalTime timestamp) {
        this.timestamp = timestamp;
    }

    public MessageContract(String uid, String sender, String receiver, LocalTime stamp, String ride){
        this.setRide(ride);
        this.setSender(sender);
        this.setReceiver(receiver);
        this.setTimestamp(stamp);

    }
}
