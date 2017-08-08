package com.example.cliff.firebaseimagefeed.Model;

// Class to be stored and retrieved from the FirebaseDatabase

public class User {

    private String userID;
    private String username;
    private String email;
    private String profileURL;

    public User(){}

    public User(String userID, String email, String username) {
        this.userID = userID;
        this.email = email;
        this.username = username;
        this.profileURL = "none";
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileURL() {
        return profileURL;
    }

    public void setProfileURL(String profileURL) {
        this.profileURL = profileURL;
    }
}
