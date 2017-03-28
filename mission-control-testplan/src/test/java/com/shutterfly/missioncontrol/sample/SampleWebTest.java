package com.shutterfly.missioncontrol.sample;

import org.testng.annotations.Test;

import com.shutterfly.missioncontrol.config.ConfigLoader;

/**
 * @author Diptman Gupta
 *
 */
public class SampleWebTest extends ConfigLoader {

	@Test
	public void getApplicationUrlTest() {

		basicConfigWeb();
		driver.get("http://www.google.com");
		driver.quit();

	}
}
