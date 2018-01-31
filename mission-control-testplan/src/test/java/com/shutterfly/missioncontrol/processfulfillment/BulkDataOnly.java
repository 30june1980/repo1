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
import org.bson.Document;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertNotNull;

/**
 * @author dgupta
 */
public class BulkDataOnly extends ConfigLoader {

  private String uri = "";
  UUID uuid = UUID.randomUUID();
  String record = "Test_qa_" + uuid.toString();
  CsvReaderWriter cwr = new CsvReaderWriter();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessFulfillment");
    return uri;
  }

  private String buildPayload() throws IOException {
    URL file = Resources.getResource("XMLPayload/ProcessFulfillment/BulkDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);

    return payload.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml",
        (record + ".xml"));
  }

  @Test(groups = "Process_BDO_Response")
  private void getResponse() throws IOException, InterruptedException {
    basicConfigNonWeb();
    String payload = this.buildPayload();
    EcgFileSafeUtil.putFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload), record,
        AppConstants.BULK_FILE);

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
    cwr.writeToCsv("BDO", record);
  }

  @Test(groups = "Process_BDO_DB", dependsOnGroups = {"Process_BDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.PROCESS);
  }

  @Test(groups = "Process_BDO_Valid_Request_Validation", dependsOnGroups = {
      "Process_BDO_Response"})
  private void validateRecordFieldsInDbForValidBDORequest() throws Exception {
    Document fulfillmentTrackingRecordDoc = ValidationUtilConfig.getInstances()
        .getTrackingRecord(record);
    TrackingRecordValidationUtil
        .validateTrackingRecordForProcessRequest(fulfillmentTrackingRecordDoc, record,
            AppConstants.ACCEPTED);
    Document fulfillmentRequest = (Document) fulfillmentTrackingRecordDoc.get("fulfillmentRequest");
    Document requestDetail = (Document) fulfillmentRequest.get("requestDetail");
    assertNotNull(requestDetail.get("bulkRequestDetail"));
  }

  @Test(groups = "Process_BDO_Valid_Request_Validation")
  private void validateRecordFieldsInDbForInValidBDORequest() throws Exception {
    String requestId = "Test_qa" + UUID.randomUUID().toString();

    //send invalid process request
    basicConfigNonWeb();
    URL file = Resources.getResource("XMLPayload/ProcessFulfillment/BulkDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);

    payload = payload.replaceAll("REQUEST_101", requestId)
        .replaceAll("bulkfile_all_valid.xml", (requestId + ".xml")).replace("BRMS", "");

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

    //validate db fields
    Document fulfillmentTrackingRecordDoc = ValidationUtilConfig.getInstances()
        .getTrackingRecord(requestId);
    TrackingRecordValidationUtil
        .validateTrackingRecordForProcessRequest(fulfillmentTrackingRecordDoc, requestId,
            AppConstants.REJECTED);

    Document fulfillmentRequest = (Document) fulfillmentTrackingRecordDoc.get("fulfillmentRequest");
    Document requestDetail = (Document) fulfillmentRequest.get("requestDetail");
    assertNotNull(requestDetail.get("bulkRequestDetail"));
  }

  @Test(groups = "Process_BDO_Response_2")
  private void getResponse2() throws Exception {
    basicConfigNonWeb();
    String requestId = "Test_qa" + UUID.randomUUID().toString();

    URL file = Resources.getResource("XMLPayload/ProcessFulfillment/BulkDataOnly_2.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);

    payload = payload.replaceAll("REQUEST_101", requestId).replaceAll("bulkfile_all_valid.xml",
        (requestId + ".xml"));
    EcgFileSafeUtil.putFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload), requestId,
        AppConstants.BULK_FILE);

    EncoderConfig encoderconfig = new EncoderConfig();
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(payload).when().post(this.getProperties());

    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(requestId, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.PROCESS);
  }

}