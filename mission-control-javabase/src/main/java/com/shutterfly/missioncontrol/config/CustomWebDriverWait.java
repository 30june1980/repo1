/**
 * 
 */
package com.shutterfly.missioncontrol.config;

import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * @author Diptman Gupta
 *
 */

public class CustomWebDriverWait extends ConfigLoader {

	public WebDriverWait universalWait() {
		int time = Integer.parseInt(config.getProperty("WebDriverWaitDurationSeconds"));
		WebDriverWait wait = new WebDriverWait(driver, time);
		return wait;
	}
}
