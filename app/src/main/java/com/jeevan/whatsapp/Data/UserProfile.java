package com.jeevan.whatsapp.Data;

import java.util.ArrayList;
import java.util.List;

public class UserProfile {
    private String firstName, middleName , lastName;
    private String username ="Username";
    private String profileImageSrc;
    private String profileBio ="User Bio";
    private long timeAccountCreated;
    private String userID;
    private String userPhoneNumber;
    private List<String> groupList = new ArrayList<>();


    public UserProfile()
    {

    }


    public String getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(String userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public List<String> getGroupList() {
        return groupList;
    }

    public String getProfileImageSrc() {
        return profileImageSrc;
    }

    public void setProfileImageSrc(String profileImageSrc) {
        this.profileImageSrc = profileImageSrc;
    }

    public void setGroupList(List<String> groupList) {
        this.groupList = groupList;
    }

    public String getUserID(){
        return userID;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileBio() {
        return profileBio;
    }

    public long getTimeAccountCreated() {
        return timeAccountCreated;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProfileBio(String profileBio) {
        this.profileBio = profileBio;
    }

    public void setTimeAccountCreated(long timeAccountCreated) {
        this.timeAccountCreated = timeAccountCreated;
    }
}
