package com.shutterfly.missioncontrolportal;

import com.shutterfly.missioncontrol.common.DateUtils;
import com.shutterfly.missioncontrol.config.ConfigLoaderWeb;
import com.shutterfly.missioncontrol.util.AppConstants;
import com.shutterfly.missioncontrolportal.Utils.PageUtils;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.PortalPage;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RequestTests extends ConfigLoaderWeb {

    private String portalUrl;
    private PortalPage portalPage;

    public RequestTests() {
    }

    @BeforeClass
    public void setup() {
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
        PageUtils.login(loginPage, config);

        portalUrl = config.getProperty(com.shutterfly.missioncontrol.util.AppConstants.QA_PORTAL_URL);
        if (portalUrl == null) {
            throw new RuntimeException("QaPortalUrl property not found");
        }
        portalPage = PageFactory.initElements(driver, PortalPage.class);
    }

    @Test(dependsOnMethods = "dateFilterTest")
    public void paginationTest() {
        driver.get(portalUrl);
        portalPage.setRequestIdTxt("1");
        portalPage.clickOnSearchBtn();
        Assert.assertTrue(portalPage.areSearchResultsVisible());
        int results = portalPage.getSearchResultCount();
        int possiblePagesForPagination = (results / 20);

        Assert.assertTrue(results > 0);
        PageUtils.testPagination(driver, portalPage, possiblePagesForPagination, true);
        PageUtils.testPagination(driver, portalPage, possiblePagesForPagination, false);
    }

    @Test
    public void dateFilterTest() {
        driver.get(portalUrl);
        portalPage.setRequestIdTxt("1");

        LocalDate fromDate = LocalDate.of(2017, 1, 1);
        LocalDate toDate = LocalDate.now();

        portalPage.setFromDateTxt(DateUtils.convertLocalDateToString(fromDate));
        portalPage.setToDateTxt(DateUtils.convertLocalDateToString(toDate));
        portalPage.clickOnSearchBtn();

        Assert.assertTrue(portalPage.areSearchResultsVisible());
        int results = portalPage.getSearchResultCount();
        int possiblePagesForPagination = results / 20;
        checkDateWhilePaginating(possiblePagesForPagination, fromDate, toDate);
    }

    private void checkDateWhilePaginating(int possiblePagesForPagination, LocalDate fromDate, LocalDate toDate) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AppConstants.DATE_PATTERN);
        int count = 0;
        while (count < possiblePagesForPagination) {
            ++count;
            for (String date : portalPage.getRequestDates()) {
                LocalDate requestDate = LocalDate.parse(date, formatter);
                Assert.assertTrue(requestDate.isEqual(fromDate) || requestDate.isAfter(fromDate));
                Assert.assertTrue(requestDate.isEqual(toDate) || requestDate.isBefore(toDate));
            }
            portalPage.clickOnNextLbl();
        }
    }

    @Test
    public void requestIdFilterTest() {
        driver.get(portalUrl);
        String requestId = "1";
        portalPage.setRequestIdTxt(requestId);

        portalPage.clickOnSearchBtn();
        Assert.assertTrue(portalPage.areSearchResultsVisible());

        int results = portalPage.getSearchResultCount();
        int possiblePagesForPagination = results / 20;
        checkRequestIdWhilePaginating(possiblePagesForPagination, requestId);
    }

    private void checkRequestIdWhilePaginating(int possiblePagesForPagination, String requestId) {
        int count = 0;
        while (count < possiblePagesForPagination) {
            ++count;
            for (String id : portalPage.getRequestIds()) {
                Assert.assertTrue(id.toLowerCase().contains(requestId));
            }
            portalPage.clickOnNextLbl();
        }
    }


}
