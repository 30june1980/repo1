package com.shutterfly.missioncontrol.cancelfulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.AppConstants;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.testng.annotations.Test;

/**
 * @author dgupta
 */
public class CancelBulkDataOnly extends ConfigLoader {

  private String uri = "";
  private String payload = "";
  private String record = "";
  CsvReaderWriter cwr = new CsvReaderWriter();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionCancelFulfillment");
    return uri;
  }

  private String buildPayload() throws IOException {
    URL file = Resources.getResource("XMLPayload/CancelFulfillment/CancelBulkDataOnly.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    record = cwr.getRequestIdByKeys("BDO");
    return payload = payload.replaceAll("REQUEST_101", record);
  }

  @Test(groups = "Cancel_BDO_Response", dependsOnGroups = {"PostForArchive_BDO_DB"})
  private void getResponse() throws IOException {
    basicConfigNonWeb();
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
  }

  @Test(groups = "Cancel_BDO_DB", dependsOnGroups = {"Cancel_BDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    ValidationUtilConfig
        .getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.CANCEL);
  }


  @Test(groups = "Cancel_BDO_Invalid_Request")
  private void getResponseForInvalidCancel() throws IOException {
    basicConfigNonWeb();
    String requestId = "Test_qa_" + UUID.randomUUID().toString();
    URL file = Resources.getResource("XMLPayload/CancelFulfillment/CancelBulkDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);
    payload = payload.replaceAll("REQUEST_101", requestId).replace("ACET", "");
    EncoderConfig encoderconfig = new EncoderConfig();
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(payload).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Rejected"));
  }
}