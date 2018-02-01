package com.shutterfly.missioncontrol.postforarchive;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import com.shutterfly.missioncontrol.util.AppConstants;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

/**
 * @author dgupta
 */
public class PostArchiveTransactionalInlineDataOnly extends ConfigLoader {

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
    URL file = Resources
        .getResource("XMLPayload/PostArchive/PostArchiveTransactionalInlineDataOnly.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    record = cwr.getRequestIdByKeys("TIDO");

    return payload = payload.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml",
        (record + ".xml"));

  }


  @Test(groups = "PostForArchive_TIDO_Response", dependsOnGroups = {"Archive_TIDO_DB"})
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


  @Test(groups = "PostForArchive_TIDO_DB", dependsOnGroups = {"PostForArchive_TIDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_REQUESTOR,
            AppConstants.POST_STATUS);
  }

  @Test(groups = "PostForArchive_TIDO_DB_eventHistory", dependsOnGroups = {
      "PostForArchive_TIDO_DB"})
  private void validateEventHistoryInDatabase() throws Exception {
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(record);
    ArrayList eventHistoryList = (ArrayList<Document>) trackingRecord.get("eventHistory");

    int maxRetry = 10;
    for (int retry = 0; retry <= maxRetry; retry++) {
      try {
        if (eventHistoryList.size() >= 5) {
          Document eventHistory = (Document) eventHistoryList.get(4);
          assertEquals(eventHistory.get("eventType"), "ArchiveConfirmed");
          assertEquals(eventHistory.get("statusCode"), AppConstants.ACCEPTED);
          break;
        } else {
          throw new Exception("eventHistoryList size is less than 5 : " + eventHistoryList.size());
        }
      } catch (Exception ex) {
        if (retry >= maxRetry) {
          throw new Exception("ArchiveConfirmed event history doesn't exist");
        } else {
          TimeUnit.SECONDS.sleep(10);
        }
      }
    }
  }

}