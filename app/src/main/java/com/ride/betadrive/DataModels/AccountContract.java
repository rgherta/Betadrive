package com.ride.betadrive.DataModels;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class AccountContract implements Parcelable {
    private String USER_ID;
    private String FULL_NAME;
    private String USER_EMAIL;
    private String USER_PHOTO;
    private String USER_TOKEN;

    public String getUSER_ID() {
        return USER_ID;
    }

    public void setUSER_ID(String USER_ID) {
        this.USER_ID = USER_ID;
    }

    public String getFULL_NAME() {
        return FULL_NAME;
    }

    public void setFULL_NAME(String FULL_NAME) {
        this.FULL_NAME = FULL_NAME;
    }

    public String getUSER_EMAIL() {
        return USER_EMAIL;
    }

    public void setUSER_EMAIL(String USER_EMAIL) {
        this.USER_EMAIL = USER_EMAIL;
    }

    public String getUSER_PHOTO() {
        return USER_PHOTO;
    }

    public void setUSER_PHOTO(String USER_PHOTO) {
        this.USER_PHOTO = USER_PHOTO;
    }

    public String getUSER_TOKEN() {
        return USER_TOKEN;
    }

    public void setUSER_TOKEN(String USER_TOKEN) {
        this.USER_TOKEN = USER_TOKEN;
    }

    public AccountContract( String USER_ID, String FULL_NAME, String USER_EMAIL, String USER_PHOTO, String USER_TOKEN ){
        this.setUSER_ID(USER_ID);
        this.setFULL_NAME(FULL_NAME);
        this.setUSER_EMAIL(USER_EMAIL);
        this.setUSER_PHOTO(USER_PHOTO);
        this.setUSER_TOKEN(USER_TOKEN);
    };

    @NonNull
    @Override
    public String toString() {
        return "Id: "        + this.getUSER_ID() +
                " | FullName: " + this.getFULL_NAME() +
                " | Email: "    + this.getUSER_EMAIL() +
                " | Photo: "    + this.getUSER_PHOTO() +
                " | Token: "    + this.getUSER_TOKEN();
    }

    protected AccountContract(Parcel in) {
        USER_ID = in.readString();
        FULL_NAME = in.readString();
        USER_EMAIL = in.readString();
        USER_PHOTO = in.readString();
        USER_TOKEN = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(USER_ID);
        dest.writeString(FULL_NAME);
        dest.writeString(USER_EMAIL);
        dest.writeString(USER_PHOTO);
        dest.writeString(USER_TOKEN);
    }

    public static final Creator<AccountContract> CREATOR = new Creator<AccountContract>() {
        @Override
        public AccountContract createFromParcel(Parcel in) {
            return new AccountContract(in);
        }

        @Override
        public AccountContract[] newArray(int size) {
            return new AccountContract[size];
        }
    };


}
