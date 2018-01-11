package com.shutterfly.missioncontrolportal;

import com.shutterfly.missioncontrol.config.ConfigLoaderWeb;
import com.shutterfly.missioncontrolportal.Utils.PageUtils;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.PortalPage;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class PortalTests extends ConfigLoaderWeb {

    private String portalUrl;
    private String loginPageUrl;
    private String userName;
    private PortalPage portalPage;

    @BeforeClass
    public void setup() {
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
        portalPage = PageFactory.initElements(driver, PortalPage.class);
        userName = config.getProperty("QaPortalUserName");
        portalUrl = config.getProperty("QaPortalUrl");
        loginPageUrl = config.getProperty("QaPortalLoginUrl");

        PageUtils.login(loginPage, config);

        Assert.assertEquals("FulfillmenthubWeb", driver.getTitle()); // login test
        Assert.assertTrue(portalPage.getUserName().contains(userName.toLowerCase()));
    }

    @Test
    public void headerTest() {
        driver.get(portalUrl);
        Assert.assertTrue(portalPage.isUhcLogoPresent());
    }

    @Test
    public void footerTest() {
        driver.get(portalUrl);
        Assert.assertTrue(portalPage.getFooterLbl().contains("unitedhealth"));
    }

    @Test
    public void logoutTest() {
        PageUtils.logout(portalPage);
        Assert.assertEquals(loginPageUrl, driver.getCurrentUrl());
    }
}
