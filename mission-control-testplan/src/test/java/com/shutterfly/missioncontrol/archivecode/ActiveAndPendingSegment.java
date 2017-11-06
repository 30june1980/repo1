/**
 * 
 */
package com.shutterfly.missioncontrol.archivecode;

import static io.restassured.RestAssured.given;

import java.io.IOException;

import org.testng.annotations.Test;

import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;

/**
 * @author dgupta
 *
 */
public class ActiveAndPendingSegment extends ConfigLoader {
	String uri = null;
	String myJson = null;
	long millis = System.currentTimeMillis();
	String record = "Test_qa_" + millis;
	private String accesstoken = new AccessToken().getAccessToken();

	private String getSaveSegmentProperties() {
		basicConfigNonWeb();
		uri = config.getProperty("BaseApiUrl") + "api/services/v1/customer/segments/active-pending";
		return uri;
	}

	@Test
	private void createBusinessSegment() throws IOException {
		given().header("Authorization", accesstoken).get(this.getSaveSegmentProperties()).then().assertThat()
				.statusCode(200);
	}
	
	
}
