/**
 *
 */
package com.shutterfly.missioncontrol.processfulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

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
import org.bson.Document;
import org.testng.annotations.Test;

/**
 * @author dgupta
 */
public class TransactionalInlineDataOnly extends ConfigLoader {

  private String uri = "";
  UUID uuid = UUID.randomUUID();
  String record = "Test_qa_" + uuid.toString();

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
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(this.buildPayload()).when().post(this.getProperties());

    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
    CsvReaderWriter cwr = new CsvReaderWriter();
    cwr.writeToCsv("TIDO", record);

  }

  @Test(groups = "Process_TIDO_DB", dependsOnGroups = {"Process_TIDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.PROCESS);
  }

  @Test(groups = "Process_TIDO_Valid_Request_Validation", dependsOnGroups = {
      "Process_TIDO_Response"})
  private void validateRecordFieldsInDbForValidRequest() throws Exception {
    Document fulfillmentTrackingRecordDoc = ValidationUtilConfig.getInstances()
        .getTrackingRecord(record);
    TrackingRecordValidationUtil
        .validateTrackingRecordForProcessRequest(fulfillmentTrackingRecordDoc, record,
            AppConstants.ACCEPTED);
    assertEquals(fulfillmentTrackingRecordDoc.get("currentFulfillmentStatus"), "SENT_TO_SUPPLIER");
    Document fulfillmentRequest = (Document) fulfillmentTrackingRecordDoc.get("fulfillmentRequest");
    Document requestDetail = (Document) fulfillmentRequest.get("requestDetail");
    assertNotNull(requestDetail.get("transactionalRequestDetail"));
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

    Document fulfillmentTrackingRecordDoc = ValidationUtilConfig.getInstances()
        .getTrackingRecord(requestId);
    assertNotNull(fulfillmentTrackingRecordDoc.get("_id"));
    assertNotNull(fulfillmentTrackingRecordDoc.get("_class"));
    assertNotNull(fulfillmentTrackingRecordDoc.get("currentFulfillmentStatus"));
    assertNotNull(fulfillmentTrackingRecordDoc.get("requestId"));
    assertNotNull(fulfillmentTrackingRecordDoc.get("auditHistory"));
    assertNotNull(fulfillmentTrackingRecordDoc.get("eventHistory"));
    assertNotNull(fulfillmentTrackingRecordDoc.get("fulfillmentMetaData"));
    Document fulfillmentRequest = (Document) fulfillmentTrackingRecordDoc.get("fulfillmentRequest");
    assertNotNull(fulfillmentRequest);
    assertNotNull(fulfillmentRequest.get("requestHeader"));
    assertNotNull(fulfillmentRequest.get("requestDetail"));
    assertNotNull(fulfillmentRequest.get("requestTrailer"));
  }
}