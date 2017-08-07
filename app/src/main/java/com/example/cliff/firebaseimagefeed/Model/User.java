package com.example.cliff.firebaseimagefeed.Model;

public class User {

    private String username;
    private String email;
    private String profileURL;

    public User(String email, String username) {
        this.email = email;
        this.username = username;
        this.profileURL = "none";
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
