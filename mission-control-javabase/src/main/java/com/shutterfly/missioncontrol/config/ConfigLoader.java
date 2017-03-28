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
			System.setProperty("webdriver.chrome.driver", "D:/Drivers/ChromeDriver");
			driver = new ChromeDriver();

		} else if (config.getProperty("Browser").equalsIgnoreCase("firefox")) {
			System.setProperty("webdriver.gecko.driver", "D:/Drivers/FirefoxDriver/geckodriver.exe");
			driver = new FirefoxDriver();
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