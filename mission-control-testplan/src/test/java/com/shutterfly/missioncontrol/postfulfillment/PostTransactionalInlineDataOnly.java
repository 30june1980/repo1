/**
 *
 */
package com.shutterfly.missioncontrol.postfulfillment;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.AppConstants;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bson.Document;
import org.testng.annotations.Test;

/**
 * @author dgupta
 */
public class PostTransactionalInlineDataOnly extends ConfigLoader {

  private String uri = "";
  private String payload = "";
  private String record = "";
  CsvReaderWriter cwr = new CsvReaderWriter();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment");
    return uri;

  }

  private String buildPayload() throws IOException {
    getPayloadFromFile();
    record = cwr.getRequestIdByKeys("TIDO");
    return payload = payload.replaceAll("REQUEST_101", record);

  }

  private void getPayloadFromFile() throws IOException {
    URL file = Resources
        .getResource("XMLPayload/PostFulfillment/PostTransactionalInlineDataOnly.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
  }

  private String buildPayloadWithEventType(String replaceString, String eventType)
      throws IOException {
    return payload = payload.replaceAll("\\b" + replaceString + "\\b", eventType);
  }


  @Test(groups = "Post_TIDO_Response", dependsOnGroups = {"Process_TIDO_DB"})
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

 /* @Test(groups = "Post_TIDO_Response_GENERATED", dependsOnGroups = {"Process_TIDO_DB"})
  private void getResponseForGenerated() throws IOException {
    basicConfigNonWeb();
    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml")
        .body(this.buildPayloadWithEventType(AppConstants.RECEIVED, AppConstants.GENERATED)).when()
        .post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));

  }

  @Test(groups = "Post_TIDO_Response_Fulfilled", dependsOnGroups = {"Post_TIDO_DB_GENERATED"})
  private void getResponseForFulFilled() throws IOException {
    basicConfigNonWeb();
    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml")
        .body(this.buildPayloadWithEventType(AppConstants.GENERATED, AppConstants.FULFILLED)).when()
        .post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));

  }


  @Test(groups = "Post_TIDO_DB_GENERATED", dependsOnGroups = {"Post_TIDO_Response_GENERATED"})
  private void validateRecordsInDatabaseGenerated() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record);
    String currentFulfillmentStatus = (String) fulfillmentTrackingRecordDoc
        .get("currentFulfillmentStatus");
    assertEquals(currentFulfillmentStatus, AppConstants.IN_PROCESS);
  }


  @Test(groups = "Post_TIDO_DB_FulFilled", dependsOnGroups = {"Post_TIDO_DB_GENERATED"})
  private void validateRecordsInDatabaseFulfilled() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record);
    String currentFulfillmentStatus = (String) fulfillmentTrackingRecordDoc
        .get("currentFulfillmentStatus");
    assertEquals(currentFulfillmentStatus, AppConstants.COMPLETE);
  }*/

  @Test(groups = "Post_TIDO_DB", dependsOnGroups = {"Post_TIDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_REQUESTOR,
            AppConstants.POST_STATUS);
  }

  @Test(groups = "Post_TIDO_RequestHistory", dependsOnGroups = {"Post_TIDO_Response"})
  private void validateRequestHistoryInDatabase() throws Exception {
    Document fulfillmentTrackingRecordDoc = ValidationUtilConfig.getInstances()
        .getTrackingRecord(record);
    List<Document> eventHistory = (ArrayList<Document>) fulfillmentTrackingRecordDoc
        .get("eventHistory");
    assertEquals(eventHistory.get(1).get("eventType").toString(), AppConstants.RECEIVED);
  }

  @Test(groups = "Post_TIDO_For_No_Process")
  private void validatePostForNoProcess() throws Exception {
    basicConfigNonWeb();
    URL file = Resources
        .getResource("XMLPayload/PostFulfillment/PostTransactionalInlineDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);
    String requestId = "Test_qa_" + UUID.randomUUID().toString();
    payload = payload.replaceAll("REQUEST_101", requestId);
    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(payload).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Rejected"));
    assertNull(ValidationUtilConfig.getInstances().getTrackingRecord(requestId));
  }
}