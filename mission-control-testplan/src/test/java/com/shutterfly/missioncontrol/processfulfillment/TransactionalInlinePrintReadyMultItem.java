package com.shutterfly.missioncontrol.processfulfillment;

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
import org.bson.Document;
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
public class TransactionalInlinePrintReadyMultItem extends ConfigLoader {

  private String uri = "";
  private String payload = "";
  CsvReaderWriter cwr = new CsvReaderWriter();
  UUID uuid = UUID.randomUUID();
  String record = "Test_qa_" + uuid.toString();
  DatabaseValidationUtil databaseValidationUtil = ValidationUtilConfig.getInstances();
  String singleItemRequestId = record + "_1";

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessFulfillment");
    return uri;
  }

  private String buildPayload() throws IOException {
    URL file = Resources
        .getResource("XMLPayload/ProcessFulfillment/TransactionalInlinePrintReadyMultItem.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);

    return payload = payload.replaceAll("REQUEST_101", record);

  }

  @Test(groups = "Process_TIPRMI_Response")
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
    cwr.writeToCsv("TIPRMI", record);

  }


  @Test(groups = "Process_TIPRMI_DB", dependsOnGroups = {"Process_TIPRMI_Response"})
  private void validateRequestStatus() throws Exception {
    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, "PutToRequestGeneratorTopic",
            AppConstants.PROCESS);
  }

  @Test(groups = "Process_TIPRMI_DB_Fields", dependsOnGroups = {"Process_TIPRMI_DB"})
  private void validateRecordInDatabase() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record);
    TrackingRecordValidationUtil
        .validateTransactionalProcessRequestFields(this.buildPayload(),
            fulfillmentTrackingRecordDoc);
  }

  @Test(groups = "Process_TIPRMI_ChildItems_Status", dependsOnGroups = {"Process_TIPRMI_DB"})
  private void validateSingleItemRecordsInDatabase() throws Exception {
    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(singleItemRequestId,
            AppConstants.REQUEST_BATCHED,
            AppConstants.PROCESS);
  }

  @Test(groups = "Process_TIPRMI_ChildItems_DB", dependsOnGroups = {
      "Process_TIPRMI_ChildItems_Status"})
  private void validateChildItemInDatabase() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil
        .getTrackingRecord(singleItemRequestId);
    String payload = this.buildPayload();
    payload = payload.replace(record, singleItemRequestId)
        .replace("TransactionalInlinePrintReadyMultItem",
            "TransactionalInlinePrintReadySingleItem");
    TrackingRecordValidationUtil.validateChildItemFields(payload, fulfillmentTrackingRecordDoc);
  }

}