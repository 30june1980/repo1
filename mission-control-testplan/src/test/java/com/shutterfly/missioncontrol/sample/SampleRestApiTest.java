/**
 * 
 */
package com.shutterfly.missioncontrol.sample;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.jayway.restassured.response.Response;
import static com.jayway.restassured.RestAssured.given;

/**
 * @author dgupta
 *
 */
public class SampleRestApiTest {

	@Test
	public void fulfillmentStatusTracking() {
		final String URI = "http://dsbsapp14-lv.internal.shutterfly.com:8085";
		Response extractableresponse = given().contentType("application/json").when()
				.get(URI + "/api/services/v1/statustracking/Test_qa_401").then().extract().response();
		String responseCode = String.valueOf(extractableresponse.getStatusCode());
		Assert.assertEquals(responseCode, "200");

	}
}
