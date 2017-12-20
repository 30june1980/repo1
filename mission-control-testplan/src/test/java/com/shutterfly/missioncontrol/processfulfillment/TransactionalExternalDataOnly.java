/**
 * 
 */
package com.shutterfly.missioncontrol.processfulfillment;

import static org.hamcrest.Matchers.equalTo;
import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.testng.annotations.Test;

import com.google.common.io.Resources;
import com.mongodb.MongoClient;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.common.EcgFileSafeUtil;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.ConnectToDatabase;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * @author dgupta
 *
 */
public class TransactionalExternalDataOnly extends ConfigLoader {
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
		URL file = Resources.getResource("XMLPayload/ProcessFulfillment/TransactionalExternalDataOnly.xml");
		String payload = Resources.toString(file, StandardCharsets.UTF_8);

		return payload = payload.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml", (record + ".xml"));

	}

	CsvReaderWriter cwr = new CsvReaderWriter();

	@Test(groups = "Test_TIPRM_XML")
	private void getResponse() throws IOException, InterruptedException {
		basicConfigNonWeb();
		String payload = this.buildPayload();
		EcgFileSafeUtil.putFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload),
				record, "bulkfile_all_valid.xml");
		 EncoderConfig encoderconfig = new EncoderConfig();
		Response response = given()
				.config(RestAssured.config()
						.encoderConfig(encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
				.header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
				.body(this.buildPayload()).when().post(this.getProperties());
				
		response.then().body(
				"ackacknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
				equalTo("Accepted"));
		cwr.writeToCsv("TEDO", record);

	}

	ConnectToDatabase connectToDatabase = new ConnectToDatabase();
	MongoClient client;

	@Test(groups = "database", dependsOnGroups = { "Test_TIPRM_XML" })
	private void validateRecordsInDatabase() throws Exception {
		DatabaseValidationUtil databaseValidationUtil = new DatabaseValidationUtil();
		databaseValidationUtil.validateRecordsAvailabilityAndStatusCheck(record , "AcceptedBySupplier", "Process");
	}
}