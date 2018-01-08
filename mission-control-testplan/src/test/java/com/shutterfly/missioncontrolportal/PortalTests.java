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
    private PortalPage portalPage;

    @BeforeClass
    public void loginTest() {
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
        PageUtils.login(loginPage, config);
        Assert.assertEquals("FulfillmenthubWeb", driver.getTitle()); // login test

        portalUrl = config.getProperty("QaPortalUrl");
        portalPage = PageFactory.initElements(driver, PortalPage.class);
    }

    @Test
    public void headerTest() {
        String userName = config.getProperty("QaPortalUserName");
        if (userName == null) {
            throw new RuntimeException("QaPortalUserName property not found");
        }

        driver.get(portalUrl);
        Assert.assertTrue(portalPage.isUhcLogoPresent());
        Assert.assertTrue(portalPage.getUserName().contains(userName.toLowerCase()));
    }

    @Test
    public void footerTest() {
        driver.get(portalUrl);
        Assert.assertTrue(portalPage.getFooterLbl().contains("unitedhealth"));
    }

}
