package com.shutterfly.missioncontrolportal.Utils;

import com.shutterfly.missioncontrol.util.Encryption;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import com.shutterfly.missioncontrolportal.pageobject.PortalPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.crypto.SecretKey;
import java.util.Properties;

public class PageUtils {

    private static final Logger logger = LoggerFactory.getLogger(PageUtils.class);

    private PageUtils() {
    }

    public static void login(@Nonnull LoginPage loginPage, @Nonnull Properties properties) {
        logger.info("Logging in...");
        SecretKey secretKey = Encryption.keyGenerator();
        String url = properties.getProperty("QaPortalLoginUrl");
        String userName = properties.getProperty("QaPortalUserName");
        String password = null;
        try {
            password = Encryption.decrypt(properties.getProperty("QaPortalPassword"), secretKey);
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

}
