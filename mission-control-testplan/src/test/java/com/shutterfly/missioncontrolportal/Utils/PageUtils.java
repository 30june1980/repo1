package com.shutterfly.missioncontrolportal.Utils;

import com.shutterfly.missioncontrol.util.Encryption;
import com.shutterfly.missioncontrolportal.pageobject.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.crypto.SecretKey;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

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

    public static WebElement waitAndFindWebElement(final By locator, final WebDriver driver) {
        Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                .withTimeout(10, TimeUnit.SECONDS)
                .pollingEvery(1, TimeUnit.SECONDS)
                .ignoring(NoSuchElementException.class);

        WebElement foo = wait.until(driver1 -> driver1.findElement(locator));
        return foo;
    }
}
