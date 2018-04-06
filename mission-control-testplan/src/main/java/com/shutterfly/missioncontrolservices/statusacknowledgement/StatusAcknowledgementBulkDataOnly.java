/**
 *
 */
package com.shutterfly.missioncontrolservices.statusacknowledgement;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrolservices.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrolservices.common.EcgFileSafeUtil;
import com.shutterfly.missioncontrolservices.common.ValidationUtilConfig;
import com.shutterfly.missioncontrolservices.config.ConfigLoader;
import com.shutterfly.missioncontrolservices.config.CsvReaderWriter;
import com.shutterfly.missioncontrolservices.util.AppConstants;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.bson.Document;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author dgupta
 */
public class StatusAcknowledgementBulkDataOnly extends ConfigLoader {

  private String uri = "";
  private String record = "";
  DatabaseValidationUtil databaseValidationUtil = ValidationUtilConfig.getInstances();
  CsvReaderWriter cwr = new CsvReaderWriter();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment");
    return uri;

  }

  private String buildPayload() throws IOException {
    URL file = Resources.getResource("XMLPayload/PostFulfillment/PostBulkDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);
    record = cwr.getRequestIdByKeys("BDO_SA");

    return payload.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml",
        (record + "_StatusAck.xml"));

  }

  @Test(groups = "Post_StatusAck_BDO_Response", dependsOnGroups = {"Process_InvalidFile_BDO_DB"})
  private void getResponse() throws IOException {
    basicConfigNonWeb();
    String payload = this.buildPayload();
    EcgFileSafeUtil.updateAndPutFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload),
        record, AppConstants.BULK_FILE_INVALID, "_StatusAck");

    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(this.buildPayload()).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));

  }

  @Test(groups = "Post_StatusAck_BDO_DB", dependsOnGroups = {"Post_StatusAck_BDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_REQUESTOR,
            AppConstants.POST_STATUS);
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.STATUS_ACK);
  }

  @Test(groups = "Post_StatusAck_BDO_DB_Items", dependsOnGroups = {"Post_StatusAck_BDO_DB"})
  private void validateItemRecordsInDB() throws Exception {
    Document trackingRecord_1 = databaseValidationUtil.getTrackingRecord(record + "_1");
    Document trackingRecord_2 = databaseValidationUtil.getTrackingRecord(record + "_2");
    assertNotNull(trackingRecord_1);
    assertNotNull(trackingRecord_2);
  }

  @Test(groups = "Post_StatusAck_BDO_DB_Items", dependsOnGroups = {"Post_StatusAck_BDO_DB"})
  private void validateItemStatusInDB() throws Exception {
    Document trackingRecord_1 = databaseValidationUtil.getStatusTrackingRecord(record + "_1");
    Document trackingRecord_2 = databaseValidationUtil.getStatusTrackingRecord(record + "_2");
    assertNotNull(trackingRecord_1);
    databaseValidationUtil
        .validateRecordStatus(trackingRecord_1, record + "_1", "ItemValidationFailure",
            AppConstants.POST_STATUS);
    assertNotNull(trackingRecord_2);
    databaseValidationUtil.validateRecordStatus(trackingRecord_2, record + "_2", "RequestSavedToDB",
        AppConstants.POST_STATUS);
  }
}