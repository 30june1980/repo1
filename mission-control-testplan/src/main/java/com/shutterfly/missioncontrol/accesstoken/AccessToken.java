package com.shutterfly.missioncontrol.accesstoken;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertNotNull;

import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.util.Encryption;

import io.restassured.response.Response;

public class AccessToken extends ConfigLoader {

	Logger logger = LoggerFactory.getLogger(AccessToken.class);

	public String getAccessToken() {
		/*
		 * Retrieving Access Token using API
		 */

		basicConfigNonWeb();
		String password = null;
		try {
			SecretKey secretKey = Encryption.keyGenerator();
			password = Encryption.decrypt(config.getProperty("DevOpsPassword"), secretKey);
		} catch (Exception e) {
			logger.error("Failed to decrypt devops password", e);
			throw new RuntimeException("Failed to decrypt devops password");
		}

		Response response = given().contentType("application/x-www-form-urlencoded")
				.header("saml", config.getProperty("SamlValue")).formParam("userName", "DEV_OPS")
				.formParam("password", password).when()
				.post("http://missioncontrolportal-qa.internal.shutterfly.com/login/authentication");
		assertNotNull(response.getCookie("ACCESS_TOKEN"));

		return response.getCookie("ACCESS_TOKEN");
	}
}
