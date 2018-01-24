/**
 *
 */
package com.shutterfly.missioncontrol.cancelfulfillment;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertNotNull;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.RequestUtil;
import com.shutterfly.missioncontrol.common.AppConstants;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.codec.binary.StringUtils;
import org.bson.Document;
import org.testng.annotations.Test;

/**
 * @author dgupta
 */
public class CancelTransactionalInlineDataOnly extends ConfigLoader {

  private String uri = "";
  private String payload = "";
  private String record = "";
  CsvReaderWriter cwr = new CsvReaderWriter();
  DatabaseValidationUtil databaseValidationUtil = ValidationUtilConfig.getInstances();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionCancelFulfillment");
    return uri;
  }

  private String buildPayload() throws IOException {
    URL file = Resources
        .getResource("XMLPayload/CancelFulfillment/CancelTransactionalInlineDataOnly.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    record = cwr.getRequestIdByKeys("TIDO");
    return payload = payload.replaceAll("REQUEST_101", record);
  }

  @Test(groups = "Cancel_TIDO_Response", dependsOnGroups = {"PostForArchive_BDO_DB_eventHistory"})
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


  @Test(groups = "Cancel_TIDO_DB", dependsOnGroups = {"Cancel_TIDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.CANCEL);
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(record);
    ArrayList eventHistoryList = (ArrayList<Document>) trackingRecord.get("eventHistory");
    //Document eventHistory = (Document) eventHistoryList.get();
    boolean cancelPending = eventHistoryList.stream()
        .anyMatch(eventHistory -> evenHistoryIsForCancelPending((Document) eventHistory));
    assertTrue(cancelPending);
  }


  private boolean evenHistoryIsForCancelPending(Document eventHistory) {
    return StringUtils.equals(eventHistory.get("eventType").toString(), "CancelPending")
        && StringUtils.equals(eventHistory.get("statusCode").toString(), AppConstants.ACCEPTED);
  }


  @Test(groups = "Cancel_TIDO_Duplicate_1")
  private void validationForDuplicateCancel_1() throws Exception {
    String requestId = "Test_qa_" + UUID.randomUUID().toString();
    RequestUtil.sendProcess(requestId);
    //send cancel request
    URL cancelFile = Resources
        .getResource("XMLPayload/CancelFulfillment/CancelTransactionalInlineDataOnly.xml");
    payload = Resources.toString(cancelFile, StandardCharsets.UTF_8);
    payload = payload.replaceAll("REQUEST_101", requestId);

    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(payload).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));

    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(requestId, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.CANCEL);

    //send cancel again
    response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(payload).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));

    //post request should be generated
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(requestId, AppConstants.ACCEPTED_BY_REQUESTOR,
            AppConstants.POST_STATUS);
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(requestId);
    assertNotNull(trackingRecord.get("postFulfillmentStatus"));
    ArrayList eventHistoryList = (ArrayList<Document>) trackingRecord.get("eventHistory");
    Document eventHistory = (Document) eventHistoryList.get(2);
    assertEquals(eventHistory.get("eventType"), "Cancelled");
    assertEquals(eventHistory.get("statusCode"), "Rejected");
    ArrayList exceptionDetailList = (ArrayList<Document>) eventHistory.get("exceptionDetailList");
    Document exceptionDetail = (Document) exceptionDetailList.get(0);
    assertEquals(exceptionDetail.get("errorCode"), "18041");
  }

  @Test(groups = "Cancel_TIDO_Duplicate_2")
  private void validationForDuplicateCancel_2() throws Exception {
    String requestId = "Test_qa_" + UUID.randomUUID().toString();
    RequestUtil.sendProcess(requestId);
    //send invalid cancel request
    URL cancelFile = Resources
        .getResource("XMLPayload/CancelFulfillment/CancelTransactionalInlineDataOnly.xml");
    String invalidCancelPayload = Resources.toString(cancelFile, StandardCharsets.UTF_8);
    invalidCancelPayload = invalidCancelPayload.replaceAll("REQUEST_101", requestId)
        .replace("ACET", "");

    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(invalidCancelPayload).when()
        .post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Rejected"));

    //send cancel again
    sendCancel(requestId);

    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(requestId, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.CANCEL);

    //both invalid and cancel request should be saved in array
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(requestId);
    ArrayList cancelFulfillment = (ArrayList<Document>) trackingRecord.get("cancelFulfillment");
    assertEquals(cancelFulfillment.size(), 2);
    assertNull(trackingRecord.get("postFulfillmentStatus"));
  }

  private void sendCancel(String requestId) throws IOException {
    Response response;
    URL cancelFile = Resources
        .getResource("XMLPayload/CancelFulfillment/CancelTransactionalInlineDataOnly.xml");
    String payload = Resources.toString(cancelFile, StandardCharsets.UTF_8);
    payload = payload.replaceAll("REQUEST_101", requestId);
    response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(payload).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
  }


  @Test(groups = "Cancel_TIDO_For_No_Process")
  private void validateCancelForNoProcess() throws Exception {
    String requestId = "Test_qa_" + UUID.randomUUID().toString();

    URL file = Resources
        .getResource("XMLPayload/CancelFulfillment/CancelTransactionalInlineDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);
    payload = payload.replaceAll("REQUEST_101", requestId);

    basicConfigNonWeb();
    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(payload).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(requestId);
    assertNotNull(trackingRecord.get("postFulfillmentStatus"));
   /*ArrayList eventHistoryList = (ArrayList<Document>) trackingRecord.get("eventHistory");
    Document eventHistory = (Document) eventHistoryList.get(2);
    assertEquals(eventHistory.get("eventType"), "Cancelled");
    assertEquals(eventHistory.get("statusCode"), "Rejected");
    ArrayList exceptionDetailList = (ArrayList<Document>) eventHistory.get("exceptionDetailList");
    Document exceptionDetail = (Document) exceptionDetailList.get(0);
    assertEquals(exceptionDetail.get("errorCode"), "18420");*/
  }

  @Test(groups = "Cancel_TIDO_For_No_Process")
  private void validationWhenProcessIsAlreadyRejected() throws Exception {
    String requestId = "Test_qa_" + UUID.randomUUID().toString();

    RequestUtil.sendProcess(requestId);
    sendPostWithRejectedEvent(requestId);
    sendCancel(requestId);
    int maxRetry = 10;
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(requestId);
    assertNotNull(trackingRecord.get("postFulfillmentStatus"));
    for (int retry = 0; retry <= maxRetry; retry++) {
      try {
        trackingRecord = databaseValidationUtil.getTrackingRecord(requestId);
        ArrayList eventHistoryList = (ArrayList<Document>) trackingRecord.get("eventHistory");
        if (eventHistoryList.size() >= 3) {
          Document eventHistory = (Document) eventHistoryList.get(2);
          assertEquals(eventHistory.get("eventType"), "Cancelled");
          assertEquals(eventHistory.get("statusCode"), "Rejected");
          ArrayList exceptionDetailList = (ArrayList<Document>) eventHistory
              .get("exceptionDetailList");
          Document exceptionDetail = (Document) exceptionDetailList.get(0);
          assertEquals(exceptionDetail.get("errorCode"), "18044");
          break;
        } else {
          throw new Exception("eventHistoryList size is less than 3 : " + eventHistoryList.size());
        }
      } catch (Exception ex) {
        if (retry >= maxRetry) {
          throw new Exception(ex.getMessage());
        } else {
          TimeUnit.SECONDS.sleep(10);
        }
      }
    }
  }

  @Test(groups = "Cancel_TIDO_For_No_Process")
  private void validationWhenProcessIsAlreadyFulfilled() throws Exception {
    String requestId = "Test_qa_" + UUID.randomUUID().toString();
    RequestUtil.sendProcess(requestId);
    sendPostWithFulfilledEvent(requestId);
    sendCancel(requestId);

    int maxRetry = 10;
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(requestId);
    assertNotNull(trackingRecord.get("postFulfillmentStatus"));
    for (int retry = 0; retry <= maxRetry; retry++) {
      try {
        trackingRecord = databaseValidationUtil.getTrackingRecord(requestId);
        ArrayList eventHistoryList = (ArrayList<Document>) trackingRecord.get("eventHistory");
        if (eventHistoryList.size() >= 3) {
          Document eventHistory = (Document) eventHistoryList.get(2);
          assertEquals(eventHistory.get("eventType"), "Cancelled");
          assertEquals(eventHistory.get("statusCode"), "Rejected");
          ArrayList exceptionDetailList = (ArrayList<Document>) eventHistory
              .get("exceptionDetailList");
          Document exceptionDetail = (Document) exceptionDetailList.get(0);
          assertEquals(exceptionDetail.get("errorCode"), "18042");
          break;
        } else {
          throw new Exception("eventHistoryList size is less than 3 : " + eventHistoryList.size());
        }
      } catch (Exception ex) {
        if (retry >= maxRetry) {
          throw new Exception(ex.getMessage());
        } else {
          TimeUnit.SECONDS.sleep(10);
        }
      }
    }
  }

  private void sendPostWithRejectedEvent(String requestId) throws Exception {
    basicConfigNonWeb();
    String uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment");

    URL file = Resources
        .getResource("XMLPayload/PostFulfillment/PostTransactionalInlineDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);

    payload = payload.replaceAll("REQUEST_101", requestId)
        .replace(AppConstants.ACCEPTED, AppConstants.REJECTED);

    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all().contentType("application/xml").body(payload).when().post(uri);
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
  }

  private void sendPostWithFulfilledEvent(String requestId) throws Exception {
    basicConfigNonWeb();
    String uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment");

    URL file = Resources
        .getResource("XMLPayload/PostFulfillment/PostTransactionalInlineDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);

    payload = payload.replaceAll("REQUEST_101", requestId)
        .replace(AppConstants.RECEIVED, AppConstants.FULFILLED);

    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all().contentType("application/xml").body(payload).when().post(uri);
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
  }
}