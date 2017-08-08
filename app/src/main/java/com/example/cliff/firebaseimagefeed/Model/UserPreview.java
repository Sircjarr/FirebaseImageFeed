package com.example.cliff.firebaseimagefeed.Model;

// Class used with an adapter for a user's name and profile picture in a ListView

public class UserPreview {

    private String username;
    private String profileImageURL;

    public UserPreview(String username, String profileURL) {
        this.username = username;
        this.profileImageURL = profileURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public void setProfileURL(String profileURL) {
        this.profileImageURL = profileURL;
    }
}
