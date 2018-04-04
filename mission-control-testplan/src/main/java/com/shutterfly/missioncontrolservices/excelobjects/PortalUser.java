package com.shutterfly.missioncontrolservices.excelobjects;

// This class represents a sheet in the excel file
public class PortalUser {

    // excel sheet columns
    private String userName;
    private String password;

    public PortalUser(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
