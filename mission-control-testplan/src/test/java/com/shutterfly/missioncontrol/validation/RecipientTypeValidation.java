/**
 * 
 */
package com.shutterfly.missioncontrol.validation;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.testng.annotations.Test;
import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.config.ConfigLoader;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * @author dgupta
 *
 */
public class RecipientTypeValidation extends ConfigLoader {
	/**
	 * 
	 */
	private String uri = "";
	UUID uuid = UUID.randomUUID();
	String record = "Test_qa_" + uuid.toString();
	
	private String getProperties() {
		basicConfigNonWeb();
		uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessFulfillment");
		return uri;
	}

	private String buildPayload() throws IOException {
		URL file = Resources.getResource("XMLPayload/Validation/CommonValidationRules.xml");		
		String payload = Resources.toString(file, StandardCharsets.UTF_8);
		return payload = payload.replaceAll("REQUEST_101", record).replaceAll("MemberType", "");
		
	}

	@Test
	private void getResponse() throws IOException {
		basicConfigNonWeb();

		/*
		 * remove charset from content type using encoder config build the payload
		 */

		EncoderConfig encoderconfig = new EncoderConfig();
		Response response = given()
				.config(RestAssured.config()
						.encoderConfig(encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
				.header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
				.body(this.buildPayload()).when().post(this.getProperties());

		assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
		System.out.println(response.getBody().asString());
		response.then().body(
				"ackacknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
				equalTo("Rejected"));
		response.then().body(
				"ackacknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionLevelErrors.transactionError.errorCode.code",
				equalTo("18034"));

		response.then().body(
				"ackacknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionLevelErrors.transactionError.errorCode.desc",
				equalTo("Recipient type is mandatory."));

	}
}