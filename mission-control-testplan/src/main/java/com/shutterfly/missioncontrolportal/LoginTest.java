package com.shutterfly.missioncontrolportal;

import com.shutterfly.missioncontrolservices.excelobjects.ReadDataFromDataSource;
import com.shutterfly.missioncontrolservices.config.ConfigLoaderWeb;
import com.shutterfly.missioncontrolservices.listener.ListenerTest;
import com.shutterfly.missioncontrolservices.util.AppConstants;
import com.shutterfly.missioncontrolportal.Utils.PageUtils;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.PortalPage;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.IOException;

@Listeners(ListenerTest.class)
public class LoginTest extends ConfigLoaderWeb {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoaderWeb.class);

    private ReadDataFromDataSource readDataFromDataSource;
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
                PageUtils.logout(portalPage);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
