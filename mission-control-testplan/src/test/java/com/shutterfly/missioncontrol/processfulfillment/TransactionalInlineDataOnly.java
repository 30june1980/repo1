/**
 * 
 */
package com.shutterfly.missioncontrol.processfulfillment;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.testng.annotations.Test;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * @author dgupta
 *
 */
public class TransactionalInlineDataOnly extends ConfigLoader {
	/**
	 * 
	 */
	private String uri = "";
	long millis = System.currentTimeMillis();
	String record = "Test_qa_" + millis;

	private String getProperties() {
		basicConfigNonWeb();
		uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessFulfillment");
		return uri;
	}

	private String buildPayload() throws IOException {
		URL file = Resources.getResource("XMLPayload/ProcessFulfillment/TransactionalInlineDataOnly.xml");
		String payload = Resources.toString(file, StandardCharsets.UTF_8);
		return payload = payload.replaceAll("REQUEST_101", record);
	}

	@Test(groups = "Test_TIDO_XML")
	private void getResponse() throws IOException {
		basicConfigNonWeb();
		Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log().all()
				.contentType("application/xml").body(this.buildPayload()).when().post(this.getProperties());
		assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
		response.then().body(
				"ackacknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
				equalTo("Accepted"));
		CsvReaderWriter cwr = new CsvReaderWriter();
		cwr.writeToCsv("TIDO", record);

	}

	@Test(groups = "database_TIDO", dependsOnGroups = { "Test_TIDO_XML" })
	private void validateRecordsInDatabase() throws IOException, InterruptedException {
		DatabaseValidationUtil databaseValidationUtil = new DatabaseValidationUtil();
		databaseValidationUtil.validateRecordsAvailabilityAndStatusCheck(record);
	}
}