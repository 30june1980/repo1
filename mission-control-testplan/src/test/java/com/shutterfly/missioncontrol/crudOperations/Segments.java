/**
 * 
 */
package com.shutterfly.missioncontrol.crudOperations;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import io.restassured.response.Response;

/**
 * @author dgupta
 *
 */
public class Segments {

	@Test
	private void getBusinessSegment() {

		Response response = when()
				.get("http://missioncontrolapi-qa.internal.shutterfly.com/api/services/v1/customer/1/segment/E&I");
		assertEquals(200, response.getStatusCode());
		response.then().body("segmentId", equalTo("E&I"));
	}

	@Test
	private void getMarketSegment() {
		Response response = when().get(
				"http://missioncontrolapi-qa.internal.shutterfly.com/api/services/v1/customer/1/segment/E&I/child/OXF");
		assertEquals(200, response.getStatusCode());
		response.then().body("segmentId", equalTo("OXF"));
		response.then().body("segmentType", equalTo("MARKET_SEGMENT"));
	}

	
}
