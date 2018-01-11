/**
 * 
 */
package com.shutterfly.missioncontrol.common;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import com.shutterfly.missioncontrol.config.ConfigLoaderWeb;
import com.shutterfly.missioncontrol.config.CustomWebDriverWait;

/**
 * @author Diptman Gupta
 *
 */
public class LoginToApplication extends ConfigLoaderWeb {

	@Test
	public void loginToUHCFulfillmentHub() {

		driver.get(config.getProperty("ApplicationUrl"));
		WebDriverWait wait = new CustomWebDriverWait().universalWait();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("userName"))).sendKeys("MC_DEV_OPS");
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password"))).sendKeys("password");
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-btn"))).submit();

	}

	public String getAccessToken() {
		loginToUHCFulfillmentHub();
		String accessToken = driver.manage().getCookieNamed("ACCESS_TOKEN").getValue();
		driver.quit();
		return accessToken;

	}
}
