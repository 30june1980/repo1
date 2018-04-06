package com.shutterfly.missioncontrolservices.config;

import com.shutterfly.missioncontrolservices.util.AppConstants;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.shutterfly.missioncontrolservices.util.AppConstants.CHROME;
import static com.shutterfly.missioncontrolservices.util.AppConstants.FIREFOX;
import static com.shutterfly.missioncontrolservices.util.AppConstants.INTERNET_EXPLORER;

public class ConfigLoaderWeb {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoaderWeb.class);

    protected static WebDriver driver = null;
    private static Properties elements = null;


    protected static Properties config = null;


    protected static String testingScenariosXlPath = null;

    /*
     *  To get basic configurations from property files
     */
    @BeforeClass
    @Parameters({AppConstants.PLATFORM, AppConstants.BROWSER})
    public static void basicConfigLoaderWeb(String platform, String browser) {

        if (driver == null) {
            config = new Properties();
            try {
                config.load(ConfigLoaderWeb.class.getClassLoader().getResourceAsStream("config.properties"));
            } catch (IOException e) {
                logger.error("Unable to load config property file: " + e);
            }

            elements = new Properties();
            try {
                elements.load(ConfigLoaderWeb.class.getClassLoader().getResourceAsStream("elements.properties"));
            } catch (IOException e) {
                logger.error("Unable to load element property file: " + e);
            }
        }

        URL url;
        try {
            url = new URL(config.getProperty(AppConstants.NODE_URL));
            testingScenariosXlPath = config.getProperty(AppConstants.TESTING_SCENARIOS_XL_PATH_PROP);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL specified in property file");
        }

        String environment = config.getProperty(AppConstants.ENVIRONMENT).toLowerCase();
        Platform operatingSystem = Platform.fromString(platform.toUpperCase());
        browser = browser.toLowerCase();
        setBrowser(environment, operatingSystem, browser, url);
    }

    @SuppressWarnings("deprecation")
    private static void setBrowser(String environment, Platform os, String browser, URL url) {
        DesiredCapabilities desiredCapabilities;
        switch (browser) {

            case CHROME:
                if (AppConstants.LOCAL.equalsIgnoreCase(environment)) {
                    System.setProperty(AppConstants.CHROME_DRIVER, System.getProperty(AppConstants.CHROME_DRIVER));
                    driver = new ChromeDriver();
                } else {
                    ChromeOptions options = new ChromeOptions();
                    Map<String, Object> prefs = new HashMap<>();
                    prefs.put("credentials_enable_service", false);
                    prefs.put("profile.password_manager_enabled", false);
                    prefs.put("profile.default_content_settings.popups", 0);
                    prefs.put("download.default_directory", "");
                    options.setExperimentalOption("prefs", prefs);
                    desiredCapabilities = DesiredCapabilities.chrome();
                    desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, options);
                    driver = new RemoteWebDriver(url, desiredCapabilities);
                }
                break;

            case FIREFOX:
                if (AppConstants.LOCAL.equalsIgnoreCase(environment)) {
                    System.setProperty(AppConstants.GECKO_DRIVER, System.getProperty(AppConstants.GECKO_DRIVER));
                    driver = new FirefoxDriver();
                } else {
                    desiredCapabilities = DesiredCapabilities.firefox();
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    firefoxOptions.setHeadless(true);
                    desiredCapabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, firefoxOptions);
                    desiredCapabilities.setPlatform(os);
                    driver = new RemoteWebDriver(url, desiredCapabilities);
                }
                break;

            case INTERNET_EXPLORER:
                desiredCapabilities = DesiredCapabilities.internetExplorer();
                desiredCapabilities.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
                desiredCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
                desiredCapabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
                desiredCapabilities.setCapability(InternetExplorerDriver.UNEXPECTED_ALERT_BEHAVIOR, "accept");
                desiredCapabilities.setCapability(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, true);
                if (AppConstants.LOCAL.equalsIgnoreCase(environment)) {
                    System.setProperty(AppConstants.IE_DRIVER, System.getProperty(AppConstants.IE_DRIVER));
                    driver = new InternetExplorerDriver(desiredCapabilities);
                } else {
                    driver = new RemoteWebDriver(url, desiredCapabilities);
                }
                break;

            default:
                throw new IllegalArgumentException("Invalid browser specified in the XML file.");
        }

        driver.manage().timeouts().implicitlyWait(AppConstants.IMPLICIT_WAIT_SECONDS, TimeUnit.SECONDS);
        driver.manage().window().maximize();
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

}
