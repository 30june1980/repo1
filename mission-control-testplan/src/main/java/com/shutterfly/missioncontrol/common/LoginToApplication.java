/**
 * 
 */
package com.shutterfly.missioncontrol.common;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CustomWebDriverWait;

/**
 * @author Diptman Gupta
 *
 */
public class LoginToApplication extends ConfigLoader {

	@Test
	public void loginToUHCFulfillmentHub() {

		basicConfigWeb();
		driver.get(config.getProperty("ApplicationUrl"));
		WebDriverWait wait = new CustomWebDriverWait().universalWait();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("userName"))).sendKeys("username");
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password"))).sendKeys("password");
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-btn"))).submit();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("login-btn"))).submit();
		Assert.assertEquals("United Health Care", wait
				.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//strong[text()='United Health Care']")))
				.getText());

		Assert.assertEquals("Fulfillment Hub",
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h4[text()='Fulfillment Hub']")))
						.getText());
	}

}
