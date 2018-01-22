

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

public class ServersTests extends ConfigLoaderWeb {

    private PortalPage portalPage;
    private String serversTabUrl;

    @BeforeClass
    public void setup() {
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
        PageUtils.login(loginPage, config);

        serversTabUrl = config.getProperty(AppConstants.APPLICATION_URL);
        if (serversTabUrl == null) {
            throw new RuntimeException("ApplicationUrl property not found");
        }

        serversTabUrl += "#/servers";
        portalPage = PageFactory.initElements(driver, PortalPage.class);
    }

    @Test
    public void paginationTest() {
        driver.get(serversTabUrl);
        Assert.assertTrue(portalPage.areSearchResultsVisible());

        int results = portalPage.getSearchResultCount();
        int possiblePagesForPagination = (results / 20);

        Assert.assertTrue(results > 0);
        PageUtils.testPagination(driver, portalPage, possiblePagesForPagination, true);
        PageUtils.testPagination(driver, portalPage, possiblePagesForPagination, false);
    }

}
