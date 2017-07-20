package com.shutterfly.missioncontrol.accessToken;

import org.openqa.selenium.By;

import com.shutterfly.missioncontrol.config.ConfigLoader;

public class AccessToken extends ConfigLoader {


	public  String getAccessToken() {
		basicConfigWeb();
		driver.get("http://missioncontrolportal-qa.internal.shutterfly.com/");
		driver.findElement(By.xpath("//input[@type='text'][@placeholder='Username']")).sendKeys("a");
		driver.findElement(By.xpath("//input[@type='password'][@placeholder='Password']")).sendKeys("password");
		driver.findElement(By.xpath("//button[contains(text(),'Login')]")).submit();
		String accessToken = driver.manage().getCookieNamed("ACCESS_TOKEN").getValue();
		driver.close();
		return accessToken;

	}
}
