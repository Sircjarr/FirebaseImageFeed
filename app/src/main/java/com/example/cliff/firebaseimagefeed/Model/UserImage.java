package com.example.cliff.firebaseimagefeed.Model;

// Class used with an adapter to create a user's uploaded image in a ListView

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
