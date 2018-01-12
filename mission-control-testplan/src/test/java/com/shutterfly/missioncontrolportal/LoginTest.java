package com.shutterfly.missioncontrolportal;

import com.shutterfly.missioncontrol.excelobjects.ReadDataFromDataSource;
import com.shutterfly.missioncontrol.config.ConfigLoaderWeb;
import com.shutterfly.missioncontrol.util.AppConstants;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.PortalPage;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public class LoginTest extends ConfigLoaderWeb {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoaderWeb.class);

    ReadDataFromDataSource readDataFromDataSource;
    PortalPage portalPage;
    LoginPage loginPage;

    @BeforeClass
    public void setDataSource() {
        readDataFromDataSource = new ReadDataFromDataSource();
        readDataFromDataSource.setDataSource(testingScenariosXlPath);
        loginPage = PageFactory.initElements(driver, LoginPage.class);
        portalPage = PageFactory.initElements(driver, PortalPage.class);
    }

    @Test
    public void loginTest() {
        String url = config.getProperty(AppConstants.QA_PORTAL_LOGIN_URL);
        try {
            readDataFromDataSource.readLoginData(1).getPortalUsers().forEach(portalUser -> {
                loginPage.login(url, portalUser.getUserName(), portalUser.getPassword());
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
