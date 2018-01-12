/**
 *
 */
package com.shutterfly.missioncontrol.postfulfillment;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.AppConstants;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
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
  DatabaseValidationUtil databaseValidationUtil = new DatabaseValidationUtil();
  CsvReaderWriter cwr = new CsvReaderWriter();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment");
    return uri;

  }

  private String buildPayload() throws IOException {
    URL file = Resources
        .getResource("XMLPayload/PostFulfillment/PostTransactionalInlineDataOnly.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    record = cwr.getRequestIdByKeys("TIDO");

    return payload = payload.replaceAll("REQUEST_101", record);

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


  @Test(groups = "Post_TIDO_DB", dependsOnGroups = {"Post_TIDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_REQUESTOR,
            AppConstants.POST_STATUS);
  }

  @Test(groups = "Post_TIDO_RequestHistory", dependsOnGroups = {"Post_TIDO_Response"})
  private void validateRequestHistoryInDatabase() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record);
    List<Document> eventHistory = (ArrayList<Document>) fulfillmentTrackingRecordDoc
        .get("eventHistory");
    assertEquals(eventHistory.get(1).get("eventType").toString(), "Received");
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
    assertNull(databaseValidationUtil.getTrackingRecord(requestId));
  }
}