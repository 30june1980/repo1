/**
 * 
 */
package com.shutterfly.missioncontrolapi;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * @author Diptman Gupta
 *
 */
public class SendingRuleControllerTest extends ConfigLoader {
	private AccessToken accessToken;
	private String token;

	@BeforeClass
	public void setup() {
		accessToken = new AccessToken();
		token = accessToken.getAccessToken();
	}

	@Test
	public void findAllSendingRules() {

		Response response = given().header("Accept", "application/json").header("Authorization", token)
				.queryParam("pageNumber", "1").queryParam("pageSize", "10").log().all().contentType(ContentType.JSON)
				.when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/sendingrules");

		Assert.assertEquals(response.getStatusCode(), 200);

	}

	@Test
	public void findAllActivePendingSendingRule() {

		Response response = given().header("Accept", "application/json").header("Authorization", token)
				.queryParam("pageNumber", "1").queryParam("pageSize", "10").log().all().contentType(ContentType.JSON)
				.when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/sendingrules");
		assertEquals(response.getStatusCode(), 200);

	}

	@Test
	public void findSendingRuleBySendingRuleId() {

		int size = given().header("Accept", "application/json").header("Authorization", token)
				.queryParam("pageNumber", "1").queryParam("pageSize", "10").contentType(ContentType.JSON).when()
				.get(config.getProperty("BaseApiUrl") + "/api/services/v1/sendingrules").path("content.size()");

		if (size > 0) {

			String sendingRuleId = given().header("Accept", "application/json").header("Authorization", token)
					.queryParam("pageNumber", "1").queryParam("pageSize", "10").contentType(ContentType.JSON).when()
					.get(config.getProperty("BaseApiUrl") + "/api/services/v1/sendingrules").then()
					.contentType(ContentType.JSON).extract().path("content[0].sendingRuleDto.sendingRuleId");
			assertNotNull(sendingRuleId);

			Response response = given().header("Accept", "application/json").header("Authorization", token)
					.queryParam("pageNumber", "1").queryParam("pageSize", "10").pathParam("sendingRuleId", sendingRuleId).contentType(ContentType.JSON).when()
					.get(config.getProperty("BaseApiUrl") + "/api/services/v1/sendingrules/{sendingRuleId}");
			assertEquals(response.getStatusCode(), 200);
			;

		} else {
			System.out.println("API is working, but there is no sending rule retrived Please add atleast one!");
		}

	}

}
