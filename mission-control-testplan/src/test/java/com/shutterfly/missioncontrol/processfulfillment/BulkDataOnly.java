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
import com.shutterfly.missioncontrol.common.EcgFileSafeUtil;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * @author dgupta
 *
 */
public class BulkDataOnly extends ConfigLoader {
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
		URL file = Resources.getResource("XMLPayload/ProcessFulfillment/BulkDataOnly.xml");
		String payload = Resources.toString(file, StandardCharsets.UTF_8);

		return payload = payload.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml", (record + ".xml"));

	}

	CsvReaderWriter cwr = new CsvReaderWriter();

	@Test(groups = "Test_BDO_XML")
	private void getResponse() throws IOException, InterruptedException {
		basicConfigNonWeb();
		String payload = this.buildPayload();
		EcgFileSafeUtil.putFileAtSourceLocation(EcgFileSafeUtil.buildSourceFilePath(payload),
				EcgFileSafeUtil.buildTargetFilePath(payload), record, "bulkfile_all_valid.xml");
		Response response = RestAssured.given().header("Accept", "application/xml")
				.header("saml", config.getProperty("SamlValue"))
				.header("Authorization",
						"eyJjdHkiOiJhcHBsaWNhdGlvblwvSlNPTiIsImFsZyI6IlJTMjU2In0.eyJ0b2tlbiI6ImVKSHNlbEEwK3h2NnRyZ1Q5dWgrZW1RblJkZ2pPUENnYmx6dElaTnBYOUV5MmpVVFBpN0huclUyZWc3bXRvYzV6Wko2eVMzZ1Y2elZpLzUwNEdabG9nSmt4TFEwRUUyemtFdmlEZEhmN2dGZHhRNnF4blp1cVowNDVYcDZjUGQxZEZ6Zlc3TW8rZXBBSEEzdlJEemNKUlgvaGJvb3packVZUjFtditrdGF5Q2VKT0hlVHlYM2VmSU92UFViMk9UaSIsICJpdiI6InVWL3ArelFHY0dJckhYM3hqakFlWlE9PSIsICJtYXhFeHBpcmF0aW9uVGltZSI6IjE1MDI5NjcxMTUwNTMifQ.DDRLXXaZapebTv4vIPsR0rXds3UAU2rtwkasfXxDMOb6-olBitu3DbOdOTkJUd6L0jq4rL4lgN6WRJbXVPi31L9daPwV81LFKEsQKGjWWP2_hCm36DaxM4pVVVLVTBwOdi9b86YX6tWvCFOduDB-TbVtgViqEh6g0Z2aVsdYmGpyj-0dpC9z4z_fJoNt_uCzthLKNb39JEY1bDNO-loX_fz1e114_LFnexUwQcGJ28g3_2AE1DQY68-axaurru9G1Chk9xXTEDBKDAyQyFxnLhk-edeMOxqA64ZJj2rS4t5MrfOOwFAUxd65okW_tU4WFX8JhlGD2HRUk8yvTQWhmA")
				.log().all().contentType("application/xml").body(this.buildPayload()).when().post(this.getProperties());
		assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
		response.then().body(
				"ackacknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
				equalTo("Accepted"));
		cwr.writeToCsv("BDO", record);

	}

	@Test(groups = "database", dependsOnGroups = { "Test_BDO_XML" })
	private void validateRecordsInDatabase() throws IOException, InterruptedException {
		DatabaseValidationUtil databaseValidationUtil = new DatabaseValidationUtil();
		databaseValidationUtil.validateRecordsAvailabilityAndStatusCheck(record);
	};
}