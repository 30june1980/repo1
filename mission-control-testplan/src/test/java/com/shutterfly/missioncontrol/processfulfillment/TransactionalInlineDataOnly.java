package com.shutterfly.missioncontrol.processfulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import com.shutterfly.missioncontrol.util.AppConstants;
import com.shutterfly.missioncontrol.util.TrackingRecordValidationUtil;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import org.bson.Document;
import org.testng.annotations.Test;

/**
 * @author dgupta
 */
public class TransactionalInlineDataOnly extends ConfigLoader {

  private String uri = "";
  UUID uuid = UUID.randomUUID();
  String record = "Test_qa_" + uuid.toString();
  DatabaseValidationUtil databaseValidationUtil = ValidationUtilConfig.getInstances();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessFulfillment");
    return uri;
  }

  private String buildPayload() throws IOException {
    URL file = Resources
        .getResource("XMLPayload/ProcessFulfillment/TransactionalInlineDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);
    return payload.replaceAll("REQUEST_101", record);
  }

  @Test(groups = "Process_TIDO_Response")
  private void getResponse() throws IOException {
    basicConfigNonWeb();
    //remove charset from content type using encoder config, build the payload
    EncoderConfig encoderconfig = new EncoderConfig();
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).header("callbackuri", "xyz")
        .contentType(ContentType.XML).log().all()
        .body(this.buildPayload()).when().post(this.getProperties());

    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
    CsvReaderWriter cwr = new CsvReaderWriter();
    cwr.writeToCsv("TIDO", record);
  }

  @Test(groups = "Process_TIDO_DB", dependsOnGroups = {"Process_TIDO_Response"})
  private void validateAcceptanceBySupplier() throws Exception {
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.PROCESS);
  }

  @Test(groups = "Process_TIDO_DB_Fields", dependsOnGroups = {"Process_TIDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record);
    TrackingRecordValidationUtil
        .validateProcessRequestFields(this.buildPayload(), fulfillmentTrackingRecordDoc);
  }

  @Test(groups = "Process_TIDO_DB_Rule", dependsOnGroups = {"Process_TIDO_DB_Fields"})
  private void validateRuleInDatabase() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record);
    assertNotNull(fulfillmentTrackingRecordDoc.get("ruleId"));
  }

  @Test(groups = "Process_TIDO_DB_CallBackUri", dependsOnGroups = {"Process_TIDO_DB"})
  private void validateCallBackUriInDatabase() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record);
    ArrayList fulfillmentMetaDataList = (ArrayList<Document>) fulfillmentTrackingRecordDoc
        .get("fulfillmentMetaData");

    Document fulfillmentMetaData = (Document) fulfillmentMetaDataList.get(1);
    if (fulfillmentMetaData.get("name").equals("callbackuri")) {
      assertEquals(fulfillmentMetaData.get("value"), "xyz");
    }

  }

  @Test(groups = "Process_TIDO_Valid_Request_Validation", dependsOnGroups = {
      "Process_TIDO_DB"})
  private void validateRecordFieldsInDbForValidRequest() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record);
    TrackingRecordValidationUtil
        .validateTrackingRecordForProcessRequest(fulfillmentTrackingRecordDoc, record,
            AppConstants.ACCEPTED);
    assertEquals(fulfillmentTrackingRecordDoc.get("currentFulfillmentStatus"), "SENT_TO_SUPPLIER");
    Document fulfillmentRequest = (Document) fulfillmentTrackingRecordDoc.get("fulfillmentRequest");
    Document requestDetail = (Document) fulfillmentRequest.get("requestDetail");
    assertNotNull(requestDetail.get("transactionalRequestDetail"));
  }

  @Test(groups = "Process_TIDO_Event_History", dependsOnGroups = {
      "Process_TIDO_DB"})
  private void validateEventHistoryInDb() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record);
    ArrayList eventHistoryList = (ArrayList<Document>) fulfillmentTrackingRecordDoc
        .get("eventHistory");
    Document eventHistory = (Document) eventHistoryList.get(0);
    assertNotNull(eventHistory.get("recipientId"));
    assertNotNull(eventHistory.get("deliveryMethodCd"));
  }


  @Test(groups = "Process_TIDO_InValid_Request_Validation")
  private void validateRecordFieldsInDbForInValidRequest() throws Exception {
    basicConfigNonWeb();
    URL file = Resources
        .getResource("XMLPayload/ProcessFulfillment/TransactionalInlineDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);
    String requestId = "Test_qa_" + UUID.randomUUID().toString();
    //set sourceId null
    payload = payload.replaceAll("REQUEST_101", requestId)
        .replace("CIRRUS", "");
    //remove charset from content type using encoder config, build the payload
    EncoderConfig encoderconfig = new EncoderConfig();
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(payload).when().post(this.getProperties());

    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Rejected"));
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(requestId);
    TrackingRecordValidationUtil
        .validateTrackingRecordForProcessRequest(fulfillmentTrackingRecordDoc, requestId,
            AppConstants.REJECTED);
  }
}