package com.shutterfly.missioncontrol.postfulfillment;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import com.shutterfly.missioncontrol.util.AppConstants;
import com.shutterfly.missioncontrol.util.TrackingRecordValidationUtil;
import com.shutterfly.missioncontrol.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.bson.Document;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

public class PostTransactionalInlineDataOnly extends ConfigLoader {

  private String uri = "";
  private String payload = "";
  private String record = "";
  private String record1 = Utils.getQARandomId();
  CsvReaderWriter cwr = new CsvReaderWriter();
  DatabaseValidationUtil databaseValidationUtil = ValidationUtilConfig.getInstances();

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

  private String buildPayloadForProcess() throws IOException {
    URL file = Resources
        .getResource("XMLPayload/ProcessFulfillment/TransactionalInlineDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);
    return payload.replaceAll("REQUEST_101", record1);
  }

  @Test(groups = "Process_TIDO_Response_ForPOst")
  private void getResponseForProcess() throws IOException {
    basicConfigNonWeb();
    //remove charset from content type using encoder config, build the payload
    EncoderConfig encoderconfig = new EncoderConfig();
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(this.buildPayloadForProcess()).when()
        .post(config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessFulfillment"));

    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
  }

  @Test(groups = "Post_TIDO_Response", dependsOnGroups = {"Process_TIDO_Valid_Request_Validation"})
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

  @Test(groups = "Post_TIDO_DB_Fields", dependsOnGroups = {"Post_TIDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record);
    TrackingRecordValidationUtil
        .validatePostRequestFields(this.buildPayload(), fulfillmentTrackingRecordDoc);
  }

  @Test(groups = "Post_TIDO_Response_GENERATED", dependsOnGroups = {
      "Process_TIDO_Response_ForPOst"})
  private void getResponseForGenerated() throws IOException {
    this.getPayloadFromFile();
    String innerPayload = this
        .buildPayloadWithEventType(AppConstants.RECEIVED, AppConstants.GENERATED);
    String innerPayload1 = innerPayload.replaceAll("\\b" + "REQUEST_101" + "\\b", record1);
    basicConfigNonWeb();
    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml")
        .body(innerPayload1).when()
        .post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
  }

  @Test(groups = "Post_TIDO_Response_Fulfilled", dependsOnGroups = {"Post_TIDO_DB_GENERATED"})
  private void getResponseForFulFilled() throws IOException {
    this.getPayloadFromFile();
    String innerPayload = this
        .buildPayloadWithEventType(AppConstants.RECEIVED, AppConstants.FULFILLED);
    String innerPayload1 = innerPayload.replaceAll("\\bREQUEST_101\\b", record1);
    basicConfigNonWeb();
    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml")
        .body(innerPayload1).when()
        .post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));

  }


  @Test(groups = "Post_TIDO_DB_GENERATED", dependsOnGroups = {"Post_TIDO_Response_GENERATED"})
  private void validateRecordsInDatabaseGenerated() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record1);
    String currentFulfillmentStatus = (String) fulfillmentTrackingRecordDoc
        .get("currentFulfillmentStatus");
    assertEquals(currentFulfillmentStatus, AppConstants.IN_PROCESS);
  }


  @Test(groups = "Post_TIDO_DB_FulFilled", dependsOnGroups = {"Post_TIDO_Response_Fulfilled"})
  private void validateRecordsInDatabaseFulfilled() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record1);
    String currentFulfillmentStatus = (String) fulfillmentTrackingRecordDoc
        .get("currentFulfillmentStatus");
    assertEquals(currentFulfillmentStatus, "FULFILLED");
  }

  @Test(groups = "Post_TIDO_DB", dependsOnGroups = {"Post_TIDO_Response"})
  private void validateAcceptanceByRequestor() throws Exception {
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.NO_REQUESTOR_NOTIFICATION_REQUIRED,
            AppConstants.POST_STATUS);
  }

  @Test(groups = "Post_TIDO_RequestHistory", dependsOnGroups = {"Post_TIDO_Response"})
  private void validateRequestHistoryInDatabase() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record);
    List<Document> eventHistory = (ArrayList<Document>) fulfillmentTrackingRecordDoc
        .get("eventHistory");
    assertEquals(eventHistory.get(1).get("eventType").toString(), AppConstants.RECEIVED);
    assertNotNull(eventHistory.get(1).get("recipientId"));
    assertNotNull(eventHistory.get(1).get("deliveryMethodCd"));
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
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionLevelErrors.transactionError.errorCode.code",
        equalTo("18420"));
    assertNull(databaseValidationUtil.getTrackingRecord(requestId));
  }

  @Test(groups = "Post_TIDO_postItemStatusBulkDetail", dependsOnGroups = {"Post_TIDO_DB"})
  private void validatePostItemStatusBulkDetailInDB() throws Exception {
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(record );
    ArrayList postFulfillmentStatusList = (ArrayList<Document>) trackingRecord.get("postFulfillmentStatus");
    Document postFulfillmentStatus = (Document) postFulfillmentStatusList.get(0);
    assertNull(postFulfillmentStatus.get("postItemStatusBulkDetails"));
  }
}