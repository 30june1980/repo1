package com.shutterfly.missioncontrol.archivecode;

import org.testng.annotations.Test;

import com.shutterfly.missioncontrol.config.ConfigLoader;

/**
 * @author Diptman Gupta
 *
 */
public class SampleWebTest extends ConfigLoader {

	@Test
	public void getApplicationUrlTest() {

		basicConfigLoaderWeb();
		driver.get(config.getProperty("ApplicationUrl"));
		driver.quit();

	}
}
