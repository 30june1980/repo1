

package com.shutterfly.missioncontrolportal;

import com.shutterfly.missioncontrolservices.config.ConfigLoaderWeb;
import com.shutterfly.missioncontrolservices.util.AppConstants;
import com.shutterfly.missioncontrolportal.Utils.PageUtils;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.PortalPage;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SchedulesTest extends ConfigLoaderWeb {

    private PortalPage portalPage;
    private String schedulesTabUrl;

    @BeforeClass
    public void setup() {
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
        PageUtils.login(loginPage, config);

        schedulesTabUrl = config.getProperty(AppConstants.APPLICATION_URL);
        if (schedulesTabUrl == null) {
            throw new RuntimeException("ApplicationUrl property not found");
        }

        schedulesTabUrl += "#/schedules";
        portalPage = PageFactory.initElements(driver, PortalPage.class);
    }

    @Test
    public void paginationTest() {
        driver.get(schedulesTabUrl);
        Assert.assertTrue(portalPage.areSearchResultsVisible());

        int results = portalPage.getSearchResultCount();

        Assert.assertTrue(results > 0);
        PageUtils.testPagination(driver, portalPage, results, true);
        PageUtils.testPagination(driver, portalPage, results, false);
    }

}
