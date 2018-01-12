package com.shutterfly.missioncontrolportal.Utils;

import com.shutterfly.missioncontrol.util.AppConstants;
import com.shutterfly.missioncontrol.util.Encryption;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.PortalPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import javax.annotation.Nonnull;
import javax.crypto.SecretKey;
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
            password = Encryption.decrypt(properties.getProperty(AppConstants.QA_PORTAL_PASSWORD), secretKey);
        } catch (Exception exception) {
            throw new RuntimeException("Failed to decrypt username / password");
        }
        loginPage.login(url, userName, password);
    }

    public static void logout(@Nonnull PortalPage portalPage) {
        logger.info("Logging out...");
        portalPage.clickOnLogout();
    }

    public static void waitForLoadingToComplete(WebDriver driver, WebElement loader) {
        WebDriverWait wait = new WebDriverWait(driver, 10000L);
        wait.until(ExpectedConditions.visibilityOf(loader));
        wait.until(ExpectedConditions.invisibilityOf(loader));
    }

    public static void testPagination(@Nonnull final WebDriver driver, @Nonnull final PortalPage portalPage,
                                      int possiblePagesForPagination, boolean moveAhead) {
        assertThatButtonIsNotClickable(driver, portalPage, moveAhead);

        int count = 0;

        while (count < possiblePagesForPagination) {
            ++count;
            if (moveAhead) {
                portalPage.clickOnNextLbl();
            } else {
                portalPage.clickOnBackLbl();
            }
        }
        Assert.assertEquals(count, possiblePagesForPagination);

        assertThatButtonIsNotClickable(driver, portalPage, !moveAhead);
    }

    private static void assertThatButtonIsNotClickable(WebDriver driver, PortalPage portalPage, boolean moveAhead) {
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
        Assert.assertFalse(moveAhead ? portalPage.isPrevLblClickable() : portalPage.isNextLblClickable());
        driver.manage().timeouts().implicitlyWait(0, TimeUnit.SECONDS);
    }

}
