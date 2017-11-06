/**
 * 
 */
package com.shutterfly.missioncontrol.config;

import java.io.IOException;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Diptman Gupta
 *
 */
public class ConfigLoader {
	
	private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

	public static  Properties config = null;
	public static  Properties elements = null;
	public static  WebDriver driver = null;
	public static  WebElement element = null;
	 static final String BROWSER = "Browser";
	// To get basic configurations from property files
	public static void basicConfigLoaderWeb() {

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
		
		if (config.getProperty(BROWSER).equalsIgnoreCase("chrome")) {
			System.setProperty("webdriver.chrome.driver", "D:\\Drivers\\ChromeDriver\\chromedriver.exe");
			driver = new ChromeDriver();

		} else if (config.getProperty(BROWSER).equalsIgnoreCase("firefox")) {
			System.setProperty("webdriver.gecko.driver",
					"C:/Users/dgupta/Desktop/Mission Control/Drivers/FirefoxDriver/geckodriver.exe");
			driver = new FirefoxDriver();
		} else if (config.getProperty(BROWSER).equalsIgnoreCase("ie")) {
			DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
			caps.setCapability("ignoreZoomSetting", true);
			System.setProperty("webdriver.ie.driver",
					"C:/Users/dgupta/Desktop/Mission Control/Drivers/InternetExplorer/IEDriverServer.exe");
			driver = new InternetExplorerDriver(caps);
		}
	}

	public static void basicConfigNonWeb() {
		
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
}