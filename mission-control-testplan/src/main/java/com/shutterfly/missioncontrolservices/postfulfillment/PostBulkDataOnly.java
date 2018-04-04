package com.shutterfly.missioncontrolservices.postfulfillment;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrolservices.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrolservices.common.EcgFileSafeUtil;
import com.shutterfly.missioncontrolservices.common.ValidationUtilConfig;
import com.shutterfly.missioncontrolservices.config.ConfigLoader;
import com.shutterfly.missioncontrolservices.config.CsvReaderWriter;
import com.shutterfly.missioncontrolservices.util.AppConstants;
import com.shutterfly.missioncontrolservices.util.TrackingRecordValidationUtil;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.bson.Document;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author dgupta
 */
public class PostBulkDataOnly extends ConfigLoader {

  private String uri = "";
  private String payload = "";
  private String record = "";
  CsvReaderWriter cwr = new CsvReaderWriter();
  DatabaseValidationUtil databaseValidationUtil = ValidationUtilConfig.getInstances();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment");
    return uri;
  }

  private String buildPayload() throws IOException {
    URL file = Resources.getResource("XMLPayload/PostFulfillment/PostBulkDataOnly.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    record = cwr.getRequestIdByKeys("BDO");

    return payload = payload.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml",
        (record + AppConstants.POST_SUFFIX + ".xml"));
  }

  @Test(groups = "Post_BDO_Response", dependsOnGroups = {"Process_BDO_DB_Fields"})
  private void getResponse() throws IOException {
    basicConfigNonWeb();
    String payload = this.buildPayload();
    EcgFileSafeUtil
        .updateAndPutFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload), record,
            AppConstants.BULK_FILE, AppConstants.POST_SUFFIX);
    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(this.buildPayload()).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
  }

  @Test(groups = "Post_BDO_DB", dependsOnGroups = {"Post_BDO_Response"})
  private void validateAcceptanceByRequestor() throws Exception {
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_REQUESTOR,
            AppConstants.POST_STATUS);
  }

  @Test(groups = "Post_BDO_DB_Fields", dependsOnGroups = {"Post_BDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record);
    TrackingRecordValidationUtil
        .validateBulkPostRequestFields(this.buildPayload(), fulfillmentTrackingRecordDoc);
  }

  @Test(groups = "Post_BDO_DB_Items", dependsOnGroups = {"Post_BDO_DB"})
  private void validateBulkItemsEventHistoryInDatabase() throws Exception {
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(record + "_1");
    assertNotNull(trackingRecord);
    ArrayList eventHistoryList = (ArrayList<Document>) trackingRecord.get("eventHistory");
    Document eventHistory = (Document) eventHistoryList.get(0);
    assertEquals(eventHistory.get("eventType"), AppConstants.RECEIVED);
    assertNotNull(eventHistory.get("recipientId"));
    assertNotNull(eventHistory.get("deliveryMethodCd"));
  }

  @Test(groups = "Post_BDO_postItemStatusBulkDetail", dependsOnGroups = {"Post_BDO_DB"})
  private void validatePostItemStatusBulkDetailInDB() throws Exception {
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(record);
    ArrayList postFulfillmentStatusList = (ArrayList<Document>) trackingRecord
        .get("postFulfillmentStatus");
    Document postFulfillmentStatus = (Document) postFulfillmentStatusList.get(0);
    assertNotNull(postFulfillmentStatus.get("postItemStatusBulkDetails"));
  }
}