package com.shutterfly.missioncontrolportal.Utils;

import com.shutterfly.missioncontrolservices.util.AppConstants;
import com.shutterfly.missioncontrolservices.util.Encryption;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.PortalPage;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import javax.annotation.Nonnull;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class PageUtils {

    private static final Logger logger = LoggerFactory.getLogger(PageUtils.class);

    private PageUtils() {
    }

    public static void login(@Nonnull LoginPage loginPage, @Nonnull Properties properties) {
        logger.info("Logging in...");
        SecretKey secretKey = Encryption.keyGenerator();
        String url = properties.getProperty(AppConstants.QA_PORTAL_LOGIN_URL);
        String userName = properties.getProperty(AppConstants.QA_PORTAL_USERNAME);
        String password = null;
        try {
            password = Encryption.decrypt(properties.getProperty(AppConstants.QA_PORTAL_PASS), secretKey);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to decrypt username / password");
        }
        loginPage.login(url, userName, password);
    }

    public static void logout(@Nonnull PortalPage portalPage) {
        logger.info("Logging out...");
        portalPage.clickOnLogout();
    }

    public static void testPagination(@Nonnull final WebDriver driver, @Nonnull final PortalPage portalPage,
                                      int totalNumberOfRecords, boolean moveAhead) {
        int numberOfPages = totalNumberOfRecords / 20;
        if (totalNumberOfRecords % 20 == 0) {
            --numberOfPages;
        }

        logger.info("Total number of records: ", totalNumberOfRecords);
        logger.info("Number of pages: ", totalNumberOfRecords);
        assertThatButtonIsNotClickable(driver, portalPage, moveAhead);

        int count = 0;
        while (count++ < numberOfPages) {
            if (moveAhead) {
                portalPage.clickOnNextLbl();
            } else {
                portalPage.clickOnBackLbl();
            }
        }
        assertThatButtonIsNotClickable(driver, portalPage, !moveAhead);
    }

    private static void assertThatButtonIsNotClickable(WebDriver driver, PortalPage portalPage, boolean moveAhead) {
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        Assert.assertFalse(moveAhead ? portalPage.isPrevLblClickable() : portalPage.isNextLblClickable());
        driver.manage().timeouts().implicitlyWait(AppConstants.IMPLICIT_WAIT_SECONDS, TimeUnit.SECONDS);
    }

    public static void takeScreenshot(@Nonnull Properties properties, @Nonnull WebDriver driver) {
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        try {
            String fileName = properties.getProperty(AppConstants.SCREENSHOT_PATH) +
                    "Screenshot" + String.valueOf(System.currentTimeMillis()) +
                    ".png";
            FileUtils.copyFile(screenshot, new File(fileName));
        } catch (IOException exception) {
            throw new RuntimeException("Failed to write screenshot to a file", exception);
        }
    }

}
