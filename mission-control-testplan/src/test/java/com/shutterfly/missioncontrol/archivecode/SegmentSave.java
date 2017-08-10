/**
 * 
 */
package com.shutterfly.missioncontrol.archivecode;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.testng.annotations.Test;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.accessToken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;

import io.restassured.response.Response;

/**
 * @author dgupta
 *
 */
public class SegmentSave extends ConfigLoader {
	String uri = null;
	String myJson = null;
	long millis = System.currentTimeMillis();
	String record = "Test_qa_" + millis;
	private String accesstoken = new AccessToken().getAccessToken();

	private String getSaveSegmentProperties() {
		basicConfigNonWeb();
		uri = config.getProperty("BaseApiUrl") + "api/services/v1/customer/1/segment";
		return uri;
	}

	private String buildPayload() throws IOException {
		URL file = Resources.getResource("RestAPI/Segments/SaveSegments.json");
		myJson = Resources.toString(file, StandardCharsets.UTF_8);
		return myJson = myJson.replaceAll("REQUEST_101", record);

	}

	@Test
	private void createBusinessSegment() throws IOException {

		Response response = given().contentType("application/json").header("Authorization", accesstoken)
				.body(this.buildPayload()).when().post(this.getSaveSegmentProperties());
		assertEquals(200, response.getStatusCode());
		response.then().body("segmentId", equalTo(record));

	}
}
