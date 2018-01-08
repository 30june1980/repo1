/**
 *
 */
package com.shutterfly.missioncontrol.postfulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import com.shutterfly.missioncontrol.common.AppConstants;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.testng.annotations.Test;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;

import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * @author dgupta
 */
public class PostTransactionalInlinePrintReadySingleItem extends ConfigLoader {

  /**
   *
   */
  private String uri = "";
  private String payload = "";
  private String record = "";

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

  CsvReaderWriter cwr = new CsvReaderWriter();

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
  private void validateRecordsInDatabase() throws Exception {
    DatabaseValidationUtil databaseValidationUtil = new DatabaseValidationUtil();
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.REQUEST_UPDATED_TO_DB,
            AppConstants.POST_STATUS);
  }
}