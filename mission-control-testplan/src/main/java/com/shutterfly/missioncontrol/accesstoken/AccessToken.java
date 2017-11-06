package com.shutterfly.missioncontrol.accesstoken;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertNotNull;

import com.shutterfly.missioncontrol.config.ConfigLoader;

import io.restassured.response.Response;

public class AccessToken extends ConfigLoader {

	public String getAccessToken() {
		/*
		 * Retrieving Access Token using API
		 */

		basicConfigNonWeb();
		Response response = given().contentType("application/x-www-form-urlencoded")
				.header("saml", config.getProperty("SamlValue")).formParam("userName", "DEV_OPS")
				.formParam("password", config.getProperty("DevOpsPassword")).when()
				.post("http://tsbsapp31-lv.internal.shutterfly.com:8090/login/authentication");
		assertNotNull(response.getCookie("ACCESS_TOKEN"));

		return response.getCookie("ACCESS_TOKEN");
	}
}
