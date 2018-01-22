/**
 *
 */
package com.shutterfly.missioncontrol.processarchive;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.RequestUtil;
import com.shutterfly.missioncontrol.common.AppConstants;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.common.EcgFileSafeUtil;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import com.shutterfly.missioncontrol.utils.Utils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * @author dgupta
 */
public class ProcessArchiveTransactionInlineDataOnly extends ConfigLoader {

  private String uri = "";
  private String payload = "";
  private String record = "";
  DatabaseValidationUtil databaseValidationUtil = ValidationUtilConfig.getInstances();
  CsvReaderWriter cwr = new CsvReaderWriter();
  Logger logger = LoggerFactory.getLogger(ProcessArchiveTransactionInlineDataOnly.class);
  String requestIdForInvalidRequest = "Test_qa_" + UUID.randomUUID().toString();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessArchive");
    return uri;
  }

  private String buildPayload() throws IOException {
    URL file = Resources
        .getResource("XMLPayload/ProcessArchive/ProcessArchiveTransactionalInlineDataOnly.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    record = cwr.getRequestIdByKeys("TIDO");
    return payload = payload.replaceAll("REQUEST_101", record)
        .replaceAll("bulkfile_all_valid.xml", (record + ".xml"));
  }

 @Test(groups = "Archive_TIDO_Response", dependsOnGroups = {"Post_TIDO_DB"})
  private void getResponse() throws IOException {
    basicConfigNonWeb();
    EncoderConfig encoderconfig = new EncoderConfig();
    String payload = this.buildPayload();
    EcgFileSafeUtil.putFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload), record,
        AppConstants.BULK_FILE);
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


  @Test()
  private void getResponseForInvalidRequestType() throws IOException {
    basicConfigNonWeb();
    EncoderConfig encoderconfig = new EncoderConfig();
    String payload = Utils.replaceExactMatch(this.buildPayload(), "Archive", "Anything");
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(payload).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionLevelErrors.transactionError.errorCode.code",
        equalTo("18011"));
  }

  @Test(groups = "Archive_TIDO_DB", dependsOnGroups = {"Archive_TIDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_ARCHIVAL_SYSTEM,
            AppConstants.ARCHIVE);
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(record);
    ArrayList archiveRequestList = (ArrayList<Document>) trackingRecord.get("archiveRequest");
    Object archiveRequest = archiveRequestList.get(0);
    assertNotNull(archiveRequest);
    ArrayList eventHistoryList = (ArrayList<Document>) trackingRecord.get("eventHistory");
    Document eventHistory = (Document) eventHistoryList.get(3);
    assertEquals(eventHistory.get("eventType"), "ArchivePending");
    assertEquals(eventHistory.get("statusCode"), AppConstants.ACCEPTED);
  }

  @Test(groups = "Archive_TIDO_DB_eventHistory", dependsOnGroups = {"Archive_TIDO_DB"})
  private void validateDestinationIdInDB() throws Exception {
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(record);
    ArrayList fulfillmentMetaDataList = (ArrayList<Document>) trackingRecord
        .get("fulfillmentMetaData");
    Document metaData1 = (Document) fulfillmentMetaDataList.get(2);
    assertEquals(metaData1.get("name"),"archiveDestinationId");
    assertEquals(metaData1.get("value"),"EDMS");
  }


  private String buildPayloadForInvalidArchive() throws IOException {
    URL file = Resources
        .getResource(
            "XMLPayload/Validation/ProcessArchiveTransactionalInlineDataOnlyInvalidArchiveDetails.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    return payload = payload.replaceAll("REQUEST_101", requestIdForInvalidRequest)
        .replaceAll("bulkfile_all_valid.xml", (requestIdForInvalidRequest + ".xml"));
  }

  @Test(groups = "Archive_TIDO_Invalid_Response")
  private void getResponseForMissingRequestDetails() throws Exception {
    basicConfigNonWeb();
    EncoderConfig encoderconfig = new EncoderConfig();
    Response response = null;
    RequestUtil.sendProcess(requestIdForInvalidRequest);
    try {
      response = given()
          .config(RestAssured.config()
              .encoderConfig(
                  encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
          .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
          .body(this.buildPayloadForInvalidArchive()).when().post(this.getProperties());
    } catch (Exception e) {
      logger.info("Could not make rest assured call {}", e.getMessage());
    }
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionLevelErrors.transactionError.errorCode.code",
        equalTo("18073"));
  }

  @Test(groups = "Archive_TIDO_Invalid_DB", dependsOnGroups = {"Archive_TIDO_Invalid_Response"})
  private void validateDatabaseForInvalidArchive() throws Exception {
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(requestIdForInvalidRequest);
    ArrayList archiveRequestList = (ArrayList<Document>) trackingRecord.get("archiveRequest");
    assertNull(archiveRequestList);
  }
}