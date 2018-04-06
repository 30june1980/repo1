package com.shutterfly.missioncontrolservices.postfulfillment;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrolservices.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrolservices.common.ValidationUtilConfig;
import com.shutterfly.missioncontrolservices.config.ConfigLoader;
import com.shutterfly.missioncontrolservices.config.CsvReaderWriter;
import com.shutterfly.missioncontrolservices.util.AppConstants;
import com.shutterfly.missioncontrolservices.util.TrackingRecordValidationUtil;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.bson.Document;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

/**
 * @author dgupta
 */
public class PostTransactionalInlinePrintReadySingleItem extends ConfigLoader {

  private String uri = "";
  private String payload = "";
  private String record = "";
  DatabaseValidationUtil databaseValidationUtil = ValidationUtilConfig.getInstances();
  CsvReaderWriter cwr = new CsvReaderWriter();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment");
    return uri;
  }

  private String buildPayload() throws IOException {
    URL file = Resources
        .getResource("XMLPayload/PostFulfillment/PostTransactionalInlinePrintReadySingleItem.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    record = cwr.getRequestIdByKeys("TIPRSI");

    return payload = payload.replaceAll("REQUEST_101", record);
  }


  @Test(groups = "Post_TIPRSI_Response", dependsOnGroups = {"Process_TIPRSI_DB"})
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

  }

  @Test(groups = "Post_TIPRSI_DB", dependsOnGroups = {"Post_TIPRSI_Response"})
  private void validateRequestStatus() throws Exception {
    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.REQUEST_UPDATED_TO_DB,
            AppConstants.POST_STATUS);
  }

  @Test(groups = "Post_TIPRSI_DB_Fields", dependsOnGroups = {"Post_TIPRSI_DB"})
  private void validateRecordsInDatabase() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record);
    TrackingRecordValidationUtil
        .validatePostRequestFields(this.buildPayload(), fulfillmentTrackingRecordDoc);
  }
}