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

/**
 * @author Diptman Gupta
 *
 */
public class ConfigLoader {

	public static Properties config = null;
	public static Properties elements = null;
	public static WebDriver driver = null;
	public static WebElement element = null;

	// To get basic configurations from property files
	public void basicConfigWeb() {
		if (driver == null) {
			config = new Properties();
			try {
				config.load(ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}

			elements = new Properties();
			try {
				elements.load(ConfigLoader.class.getClassLoader().getResourceAsStream("elements.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (config.getProperty("Browser").equalsIgnoreCase("chrome")) {
			System.setProperty("webdriver.chrome.driver", "C:/Users/dgupta/Desktop/Mission Control/Drivers/ChromeDriver/chromedriver.exe");
			driver = new ChromeDriver();

		} else if (config.getProperty("Browser").equalsIgnoreCase("firefox")) {
			System.setProperty("webdriver.gecko.driver", "C:/Users/dgupta/Desktop/Mission Control/Drivers/FirefoxDriver/geckodriver.exe");
			driver = new FirefoxDriver();
		} else if (config.getProperty("Browser").equalsIgnoreCase("ie")) {
			DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
			caps.setCapability("ignoreZoomSetting", true);
			System.setProperty("webdriver.ie.driver", "C:/Users/dgupta/Desktop/Mission Control/Drivers/InternetExplorer/IEDriverServer.exe");
			driver = new InternetExplorerDriver(caps);
		}
	}

	public void basicConfigNonWeb() {
		{
			config = new Properties();
			try {
				config.load(ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}

			elements = new Properties();
			try {
				elements.load(ConfigLoader.class.getClassLoader().getResourceAsStream("elements.properties"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}