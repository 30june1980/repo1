/**
 *
 */
package com.shutterfly.missioncontrol.processfulfillment;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.EcgFileSafeUtil;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import com.shutterfly.missioncontrol.util.AppConstants;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

/**
 * @author dgupta
 */
public class BulkPrintReady extends ConfigLoader {

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
    URL file = Resources.getResource("XMLPayload/ProcessFulfillment/BulkPrintReady.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);

    return payload.replaceAll("REQUEST_101", record)
        .replaceAll("bulkfile_all_valid.xml", (record + ".xml"));

  }

  CsvReaderWriter cwr = new CsvReaderWriter();

  @Test(groups = "Process_BPR_Response")
  private void getResponse() throws IOException, InterruptedException {
    basicConfigNonWeb();

    String payload = this.buildPayload();

    EcgFileSafeUtil.putFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload),
        record, AppConstants.BULK_FILE);

    EncoderConfig encoderconfig = new EncoderConfig();
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(this.buildPayload()).when().post(this.getProperties());

    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
    cwr.writeToCsv("BPR", record);

  }

  @Test(groups = "Process_BPR_DB", dependsOnGroups = {"Process_BPR_Response"})
  private void validateRecordsInDatabase() throws Exception {

    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.PROCESS);
  }
}