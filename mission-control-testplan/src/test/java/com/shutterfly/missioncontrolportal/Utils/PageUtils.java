package com.shutterfly.missioncontrolportal.Utils;

import com.shutterfly.missioncontrol.util.Encryption;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.crypto.SecretKey;
import java.util.List;
import java.util.Properties;

public class PageUtils {

    private static final Logger logger = LoggerFactory.getLogger(PageUtils.class);

    private PageUtils() {
    }

    public static boolean isWebElementPresent(WebElement webElement) {
        try {
            webElement.isDisplayed();
            return true;
        } catch (NullPointerException | NoSuchElementException exception) {
            logger.warn("webElement doesn't seem to exist");
            return false;
        }
    }

    public static boolean areWebElementsPresent(List<WebElement> webElements) {
        if (webElements.isEmpty()) {
            return false;
        } else {
            for (WebElement webElement : webElements) {
                if (!isWebElementPresent(webElement)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static void login(@Nonnull LoginPage loginPage, @Nonnull Properties properties) {
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

    public static void waitForLoadingToComplete(WebDriver driver, WebElement loader) {
        WebDriverWait wait = new WebDriverWait(driver, 10000L);
        wait.until(ExpectedConditions.visibilityOf(loader));
        wait.until(ExpectedConditions.invisibilityOf(loader));
    }

}
