/**
 * 
 */
package com.shutterfly.missioncontrol.postfulfillment;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.testng.annotations.Test;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.common.EcgFileSafeUtil;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * @author dgupta
 *
 */
public class PostBulkPrintReady extends ConfigLoader {
	/**
	 * 
	 */
	private String uri = "";
	private String payload = "";
	private String record = "";

	private String getProperties() {
		basicConfigNonWeb();
		uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment");
		return uri;

	}

	private String buildPayload() throws IOException {
		URL file = Resources.getResource("XMLPayload/PostFulfillment/PostBulkPrintReady.xml");
		payload = Resources.toString(file, StandardCharsets.UTF_8);
		record = cwr.getRequestIdByKeys("BPR");

		return payload = payload.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml",
				(record + "_Post.xml"));

	}

	CsvReaderWriter cwr = new CsvReaderWriter();

	@Test(groups = "Test_PBPR_XML")
	private void getResponse() throws IOException {
		basicConfigNonWeb();
		String payload = this.buildPayload();
		record = record + "_Post";

		EcgFileSafeUtil.putFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload), record,
				"bulkfile_all_valid.xml");
		Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log().all()
				.contentType("application/xml").body(this.buildPayload()).when().post(this.getProperties());
		assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
		response.then().body(
				"ackacknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
				equalTo("Accepted"));

	}

	@Test(groups = "database", dependsOnGroups = { "Test_PBPR_XML" })
	private void validateRecordsInDatabase() throws Exception {
		record = record.replace("_Post", "");
		DatabaseValidationUtil databaseValidationUtil = new DatabaseValidationUtil();
		databaseValidationUtil.validateRecordsAvailabilityAndStatusCheck(record, "AcceptedByRequestor", null);
	}
}