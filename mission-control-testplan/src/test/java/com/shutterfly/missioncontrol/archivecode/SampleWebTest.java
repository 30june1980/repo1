package com.shutterfly.missioncontrol.archivecode;

import com.shutterfly.missioncontrol.config.ConfigLoaderWeb;
import org.testng.annotations.Test;

import com.shutterfly.missioncontrol.config.ConfigLoader;

/**
 * @author Diptman Gupta
 *
 */
public class SampleWebTest extends ConfigLoaderWeb {

	@Test
	public void getApplicationUrlTest() {

		driver.get(config.getProperty("ApplicationUrl"));
		driver.quit();

	}
}
