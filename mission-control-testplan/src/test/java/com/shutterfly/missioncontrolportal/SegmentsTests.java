package com.shutterfly.missioncontrolportal;

import com.shutterfly.missioncontrol.common.DateUtils;
import com.shutterfly.missioncontrol.config.ConfigLoaderWeb;
import com.shutterfly.missioncontrol.util.AppConstants;
import com.shutterfly.missioncontrolportal.Utils.PageUtils;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.SegmentPage;
import org.openqa.selenium.support.PageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.Random;

public class SegmentsTests extends ConfigLoaderWeb {

    private String segmentsTabUrl;
    private SegmentPage segmentPage;

    @BeforeClass
    public void setup() {
        LoginPage loginPage = PageFactory.initElements(driver, LoginPage.class);
        PageUtils.login(loginPage, config);

        segmentsTabUrl = config.getProperty(AppConstants.APPLICATION_URL);
        if (segmentsTabUrl == null) {
            throw new RuntimeException("ApplicationUrl property not found");
        }
        segmentsTabUrl += "#/segments";
        segmentPage = PageFactory.initElements(driver, SegmentPage.class);
    }

     @Test
    public void addSegmentTest() {
        driver.get(segmentsTabUrl);

        Random random = new Random(System.currentTimeMillis());
        int randomNum = random.nextInt();
        String segmentName = String.format("S%s", randomNum);
        String segmentId = String.format("Sid%s", randomNum);

        segmentPage.clickOnAddSegmentBtn();
        segmentPage.setSegmentName(segmentName);
        segmentPage.clickOnSaveBtn();
        segmentPage.setSegmentId(segmentId);

        LocalDate toDate = LocalDate.now().plusDays(3);

        segmentPage.setEndDate(DateUtils.convertLocalDateToString(toDate));
        segmentPage.clickOnSaveSegmentBtn();
        Assert.assertTrue(segmentPage.isToastDisplayed());
    }

    @Test
    public void editSegmentTest() {
        driver.get(segmentsTabUrl);
        LocalDate toDate = LocalDate.now().plusDays(3);
        String segmentDate = DateUtils.convertLocalDateToString(toDate);

        segmentPage.clickOnEditSegmentBtn();
        segmentPage.setEndDate(segmentDate);
        segmentPage.clickOnSaveSegmentBtn();
        Assert.assertTrue(segmentPage.isToastDisplayed());
    }

}
