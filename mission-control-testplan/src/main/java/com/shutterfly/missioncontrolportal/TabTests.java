package com.shutterfly.missioncontrolportal;

import com.shutterfly.missioncontrolservices.config.ConfigLoaderWeb;
import com.shutterfly.missioncontrolportal.Utils.PageUtils;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.PortalPage;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class TabTests extends ConfigLoaderWeb {

    @BeforeClass
    public void setup() {
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
        PageUtils.login(loginPage, config);
    }

    @Test
    public void tabPresentTest() {
        PortalPage portalPage = PageFactory.initElements(driver, PortalPage.class);
        List<String> tabNames = Arrays.asList("Requests", "Segments", "Materials", "Participants",
                "Rules", "Schedules", "File Transfer", "Servers");
        Assert.assertEquals(tabNames, portalPage.getTabLblList());
    }

}
