package com.example.betadrive.DataModels;

public class AccountContract {
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
}
