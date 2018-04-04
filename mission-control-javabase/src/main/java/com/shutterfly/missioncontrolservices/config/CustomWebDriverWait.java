/**
 * 
 */
package com.shutterfly.missioncontrolservices.config;

import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Diptman Gupta
 *
 */

public class CustomWebDriverWait extends ConfigLoader {

	public WebDriverWait universalWait() {
		return new WebDriverWait(driver, Integer.parseInt(config.getProperty("WebDriverWaitDurationSeconds")));
	}
}
