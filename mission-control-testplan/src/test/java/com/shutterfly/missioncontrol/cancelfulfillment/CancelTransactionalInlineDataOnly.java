/**
 *
 */
package com.shutterfly.missioncontrol.cancelfulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertNotNull;

import com.shutterfly.missioncontrol.common.AppConstants;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.UUID;
import org.bson.Document;
import org.testng.annotations.Test;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;

import io.restassured.RestAssured;
import io.restassured.response.Response;

/**
 * @author dgupta
 */
public class CancelTransactionalInlineDataOnly extends ConfigLoader {

  private String uri = "";
  private String payload = "";
  private String record = "";
  CsvReaderWriter cwr = new CsvReaderWriter();
  DatabaseValidationUtil databaseValidationUtil = ValidationUtilConfig.getInstances();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionCancelFulfillment");
    return uri;
  }

  private String buildPayload() throws IOException {
    URL file = Resources
        .getResource("XMLPayload/CancelFulfillment/CancelTransactionalInlineDataOnly.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    record = cwr.getRequestIdByKeys("TIDO");
    return payload = payload.replaceAll("REQUEST_101", record);
  }

  @Test(groups = "Cancel_TIDO_Response", dependsOnGroups = {"PostForArchive_TIDO_DB"})
  private void getResponse() throws IOException {
    basicConfigNonWeb();
    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(this.buildPayload()).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
  }


  @Test(groups = "Cancel_TIDO_DB", dependsOnGroups = {"Cancel_TIDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.CANCEL);
  }


  @Test(groups = "Cancel_TIDO_Duplicate_1")
  private void validationForDuplicateCancel_1() throws Exception {
    String requestId = "Test_qa_" + UUID.randomUUID().toString();
    sendProcess(requestId);
    //send cancel request
    URL cancelFile = Resources
        .getResource("XMLPayload/CancelFulfillment/CancelTransactionalInlineDataOnly.xml");
    payload = Resources.toString(cancelFile, StandardCharsets.UTF_8);
    payload = payload.replaceAll("REQUEST_101", requestId);

    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(payload).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));

    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(requestId, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.CANCEL);

    //send cancel again
    response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(payload).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));

    //post request should be generated
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(requestId, AppConstants.ACCEPTED_BY_REQUESTOR,
            AppConstants.POST_STATUS);
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(requestId);
    assertNotNull(trackingRecord.get("postFulfillmentStatus"));
  }

  @Test(groups = "Cancel_TIDO_Duplicate_2")
  private void validationForDuplicateCancel_2() throws Exception {
    String requestId = "Test_qa_" + UUID.randomUUID().toString();
    sendProcess(requestId);
    //send invalid cancel request
    URL cancelFile = Resources
        .getResource("XMLPayload/CancelFulfillment/CancelTransactionalInlineDataOnly.xml");
    String invalidCancelPayload = Resources.toString(cancelFile, StandardCharsets.UTF_8);
    invalidCancelPayload = invalidCancelPayload.replaceAll("REQUEST_101", requestId)
        .replace("ACET", "");

    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(invalidCancelPayload).when()
        .post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Rejected"));

    //send cancel again
    String payload = Resources.toString(cancelFile, StandardCharsets.UTF_8);
    payload = payload.replaceAll("REQUEST_101", requestId);
    response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(payload).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));

    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(requestId, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.CANCEL);

    //both invalid and cancel request should be saved in array
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(requestId);
    ArrayList cancelFulfillment = (ArrayList<Document>) trackingRecord.get("cancelFulfillment");
    assertEquals(cancelFulfillment.size(), 2);
    assertNull(trackingRecord.get("postFulfillmentStatus"));
  }

  private void sendProcess(String requestId) throws Exception {
    basicConfigNonWeb();
    //send process request
    URL file = Resources
        .getResource("XMLPayload/ProcessFulfillment/TransactionalInlineDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);
    payload = payload.replaceAll("REQUEST_101", requestId);

    //remove charset from content type using encoder config, build the payload
    EncoderConfig encoderconfig = new EncoderConfig();
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(payload).when()
        .post(config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessFulfillment"));

    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));

    //validate process is sent to supplier
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(requestId, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.PROCESS);
  }
}