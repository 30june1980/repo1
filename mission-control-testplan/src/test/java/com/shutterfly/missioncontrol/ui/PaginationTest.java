/**
 * 
 */
package com.shutterfly.missioncontrol.ui;

import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import com.shutterfly.missioncontrol.common.LoginToApplication;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CustomWebDriverWait;

/**
 * @author Diptman Gupta
 *
 */
public class PaginationTest extends ConfigLoader {

	@Test
	public void login() {
		LoginToApplication login = new LoginToApplication();
		login.loginToUHCFulfillmentHub();

		WebDriverWait wait = new CustomWebDriverWait().universalWait();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("smc_search"))).click();
	}

}
