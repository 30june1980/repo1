package com.shutterfly.missioncontrolservices.redelivery;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrolservices.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrolservices.common.ValidationUtilConfig;
import com.shutterfly.missioncontrolservices.config.ConfigLoader;
import com.shutterfly.missioncontrolservices.config.CsvReaderWriter;
import com.shutterfly.missioncontrolservices.util.AppConstants;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.bson.Document;
import org.testng.annotations.Test;

public class PostTransactionalExternalPrintReady_Fulfilled extends ConfigLoader {

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
        .getResource("XMLPayload/Redelivery/PostTransactionalExternalPrintReady_Fulfilled.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    record = cwr.getRequestIdByKeys("REDELIVER");
    return payload = payload.replaceAll("REQUEST_101", record);
  }

  @Test(groups = "Post_EUTEPR_Response_Fulfilled", dependsOnGroups = {"Post_EUTEPR_DB_Status_Generated"})
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

  @Test(groups = "Post_EUTEPR_DB_Fulfilled", dependsOnGroups = {"Post_EUTEPR_Response_Fulfilled"})
  private void validateRecordsInDatabase() throws Exception {
    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_REQUESTOR,
            AppConstants.POST_STATUS);
  }

  @Test(groups = "Post_EUTEPR_DB_Status_Fulfilled", dependsOnGroups = {"Post_EUTEPR_DB_Fulfilled"})
  private void validateCurrentFulfillmentStatusFulfilled() throws Exception {
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(record);
    String currentFulfillmentStatus = (String) trackingRecord.get("currentFulfillmentStatus");
    assertEquals(currentFulfillmentStatus,"FULFILLED");
  }
}