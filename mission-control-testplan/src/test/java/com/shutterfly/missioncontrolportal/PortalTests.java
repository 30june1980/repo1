package com.shutterfly.missioncontrolportal;

import com.shutterfly.missioncontrol.config.ConfigLoaderWeb;
import com.shutterfly.missioncontrol.util.AppConstants;
import com.shutterfly.missioncontrolportal.Utils.PageUtils;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.PortalPage;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class PortalTests extends ConfigLoaderWeb {

    private String portalUrl;
    private String loginPageUrl;
    private String userName;
    private String pageTitle = "FulfillmenthubWeb";
    private String pageFooter = "UnitedHealth";
    private PortalPage portalPage;

    @BeforeClass
    public void setup() throws InterruptedException {
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
        portalPage = PageFactory.initElements(driver, PortalPage.class);
        userName = config.getProperty(AppConstants.QA_PORTAL_USERNAME);
        portalUrl = config.getProperty(AppConstants.QA_PORTAL_URL);
        loginPageUrl = config.getProperty(AppConstants.QA_PORTAL_LOGIN_URL);

        PageUtils.login(loginPage, config);
Assert.assertEquals(pageTitle, driver.getTitle()); // login test
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
        Assert.assertTrue(portalPage.getFooter().contains(pageFooter));
    }

    @Test
    public void logoutTest() {
        PageUtils.logout(portalPage);
        Assert.assertEquals(loginPageUrl, driver.getCurrentUrl());
    }

    @Test
    public void filterOrderTest() {
        driver.get(portalUrl);
        portalPage.clickOnAdditionalFiltersLbl();


        portalPage.clickOnFilterDropdown();
        String[] options = portalPage.getDropDownOptions().split("\n");
        Assert.assertTrue(options.length > 1);
        for (int i = 1; i < options.length - 1; i++) {
            Assert.assertTrue(options[i].compareTo(options[i + 1]) <= 0);
        }
    }

}
