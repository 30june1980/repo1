/**
 * 
 */
package com.shutterfly.missioncontrol.crudoperations;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.testng.annotations.Test;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.config.ConfigLoader;

import io.restassured.response.Response;

/**
 * @author dgupta
 *
 */
public class Segments extends ConfigLoader {
	String uri = null;
	String myJson = null;
	long millis = System.currentTimeMillis();
	String record = "Test_qa_" + millis;

	private String getProperties() {
		basicConfigNonWeb();
		uri = config.getProperty("BaseApiUrl") + "api/services/v1/customer/1/segment";
		return uri;
	}

	private String buildJson() throws IOException {
		URL file = Resources.getResource("RestAPI/Segments/Segments.json");
		myJson = Resources.toString(file, StandardCharsets.UTF_8);

		return myJson = myJson.replaceAll("REQUEST_101", record);

	}

	@Test
	private void createBusinessSegment() throws IOException {
		System.out.println("Record is : " + record);
		Response response = given().contentType("application/json").body(this.buildJson()).when()
				.post(this.getProperties());
		assertEquals(200, response.getStatusCode());
		response.then().body("segmentId", equalTo(record));

	}

}
