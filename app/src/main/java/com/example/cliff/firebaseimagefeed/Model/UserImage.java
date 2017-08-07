package com.example.cliff.firebaseimagefeed.Model;

public class UserImage {

    private String userImageURL;

    public UserImage(String userImageURL) {
        this.userImageURL = userImageURL;
    }

    public String getUserImageURL() {
        return userImageURL;
    }

    public void setUserImageURL(String userImageURL) {
        this.userImageURL = userImageURL;
    }
}
