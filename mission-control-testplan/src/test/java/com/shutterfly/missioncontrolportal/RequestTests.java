package com.shutterfly.missioncontrolportal;

import com.shutterfly.missioncontrol.config.ConfigLoaderWeb;
import com.shutterfly.missioncontrolportal.Utils.PageUtils;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.PortalPage;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RequestTests extends ConfigLoaderWeb {

    private PortalPage portalPage;
    private String portalUrl;

    @BeforeClass
    public void setup() {
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
        PageUtils.login(loginPage, config);

        portalUrl = config.getProperty("QaPortalUrl");
        if (portalUrl == null) {
            throw new RuntimeException("QaPortalUrl property not found");
        }
        portalPage = PageFactory.initElements(driver, PortalPage.class);
    }

    @Test
    public void requestIdTest() {
        driver.get(portalUrl);
        portalPage.setRequestIdTxt("1");
        portalPage.clickSubmitBtn();
        Assert.assertTrue(portalPage.areSearchResultsVisible());
    }

}
