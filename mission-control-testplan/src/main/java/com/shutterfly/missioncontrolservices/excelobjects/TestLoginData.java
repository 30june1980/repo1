package com.shutterfly.missioncontrolservices.excelobjects;

import java.util.List;

public class TestLoginData {

    private List<PortalUser> portalUsers;

    public TestLoginData(
            List<PortalUser> portalUsers) {
        this.portalUsers = portalUsers;
    }

    public List<PortalUser> getPortalUsers() {
        return portalUsers;
    }

    public void setPortalUsers(
            List<PortalUser> portalUsers) {
        this.portalUsers = portalUsers;
    }
}
