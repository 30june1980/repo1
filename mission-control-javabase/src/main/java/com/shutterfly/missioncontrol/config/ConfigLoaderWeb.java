package com.shutterfly.missioncontrol.config;

import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class ConfigLoaderWeb {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    protected static WebDriver driver = null;
    private static Properties elements = null;


    protected static Properties config = null;

    private static final String LOCAL = "local";
    private static final String chromeDriver = "webdriver.chrome.driver";
    private static final String geckoDriver = "webdriver.gecko.driver";
    private static final String ieDriver = "webdriver.ie.driver";
    private static final String nodeUrl = "nodeUrl";


    /*
 *  To get basic configurations from property files
 */
    @BeforeClass
    @Parameters({"platform", "browser"})
    public static void basicConfigLoaderWeb(String platform, String browser) {

        if (driver == null) {
            config = new Properties();
            try {
                config.load(ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties"));
            } catch (IOException e) {
                logger.error("Unable to load config property file: " + e);
            }

            elements = new Properties();
            try {
                elements.load(ConfigLoader.class.getClassLoader().getResourceAsStream("elements.properties"));
            } catch (IOException e) {
                logger.error("Unable to load element property file: " + e);
            }
        }

        URL url;
        try {
            url = new URL(config.getProperty(nodeUrl));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL specified in property file");
        }

        String environment = config.getProperty("environment").toLowerCase();
        Platform operatingSystem = Platform.fromString(platform.toUpperCase());
        setBrowser(environment, operatingSystem, browser, url);
    }

    @SuppressWarnings("deprecation")
	private static void setBrowser(String environment, Platform os, String browser, URL url) {
        DesiredCapabilities desiredCapabilities = null;
        switch (browser) {

            case "chrome":
                if (LOCAL.equalsIgnoreCase(environment)) {
                    System.setProperty(chromeDriver, System.getProperty(chromeDriver));
                    driver = new ChromeDriver();
                } else {
                    desiredCapabilities = DesiredCapabilities.chrome();
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.setHeadless(true);
                    desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
                    desiredCapabilities.setPlatform(os);
                    driver = new RemoteWebDriver(url, desiredCapabilities);
                }
                break;

            case "firefox":
                if (LOCAL.equalsIgnoreCase(environment)) {
                    System.setProperty(geckoDriver, System.getProperty(geckoDriver));
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

            case "ie":
                desiredCapabilities = DesiredCapabilities.internetExplorer();
                desiredCapabilities.setCapability("ignoreZoomSetting", true);

                if (LOCAL.equalsIgnoreCase(environment)) {
                    System.setProperty(ieDriver, System.getProperty(ieDriver));
                    driver = new InternetExplorerDriver(desiredCapabilities);
                } else {
                    driver = new RemoteWebDriver(url, desiredCapabilities);
                }
                break;

            default:
        }
    }
}
