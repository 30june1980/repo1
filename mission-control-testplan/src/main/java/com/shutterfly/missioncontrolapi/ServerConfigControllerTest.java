package com.shutterfly.missioncontrolapi;

import com.shutterfly.missioncontrolservices.accesstoken.AccessToken;
import com.shutterfly.missioncontrolservices.config.ConfigLoader;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class ServerConfigControllerTest extends ConfigLoader {

	private AccessToken accessToken;
	private String token;

	@BeforeClass
	public void setup() {
		accessToken = new AccessToken();
		token = accessToken.getAccessToken();
	}

	@Test
	public void findAllServerConfigurations() {

		Response response = given().header("Accept", "application/json").header("Authorization", token)
				.queryParam("pageNumber", "1").queryParam("pageSize", "10").log().all().contentType(ContentType.JSON)
				.when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/server");

		Assert.assertEquals(response.getStatusCode(), 200);
	}
	
	@Test
	public void findByUuid() {

		Response response = given().header("Accept", "application/json").header("Authorization", token)
				.pathParam("uuid", "fca7800c-12ae-4ef5-8318-dd537aa5a224").log().all().contentType(ContentType.JSON)
				.when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/server/{uuid}");

		Assert.assertEquals(response.getStatusCode(), 200);
	}
	
	
	
}
