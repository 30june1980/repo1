/**
 * 
 */
package com.shutterfly.missioncontrol.postarchive;

import static org.hamcrest.Matchers.equalTo;
import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;

import com.shutterfly.missioncontrol.common.EcgFileSafeUtil;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.testng.annotations.Test;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * @author dgupta
 *
 */
public class PostArchiveBulkDataOnly extends ConfigLoader {
	/**
	 * 
	 */
	private String uri = "";

	private String record = "";

	private String getProperties() {
		basicConfigNonWeb();
		uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment");
		return uri;

	}


	private String buildPayload() throws IOException {
		URL file = Resources.getResource("XMLPayload/PostArchive/PostArchiveBulkDataOnly.xml");
		String payload = Resources.toString(file, StandardCharsets.UTF_8);
		record = cwr.getRequestIdByKeys("BDO");

		return payload = payload.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml",
				(record + "_PostArchive.xml"));

	}

	CsvReaderWriter cwr = new CsvReaderWriter();

	@Test(groups = "Test_POABDO_XML", dependsOnGroups = { "Test_PABDO_XML" })
	private void getResponse() throws IOException {
		basicConfigNonWeb();
		String payload = this.buildPayload();
		record = record + "_PostArchive";
		EcgFileSafeUtil.putFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload),
				record, "bulkfile_all_valid.xml");
		EncoderConfig encoderconfig = new EncoderConfig();
		Response response = given()
				.config(RestAssured.config()
						.encoderConfig(encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
				.header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
				.body(this.buildPayload()).when().post(this.getProperties());
		assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
		response.then().body(
				"ackacknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
				equalTo("Accepted"));

	}

	@Test(groups = "database", dependsOnGroups = { "Test_POABDO_XML" })
	private void validateRecordsInDatabase() throws Exception {
		DatabaseValidationUtil databaseValidationUtil = new DatabaseValidationUtil();
		databaseValidationUtil.validateRecordsAvailabilityAndStatusCheck(record, "AcceptedByRequestor", "PostStatus");
	}
}