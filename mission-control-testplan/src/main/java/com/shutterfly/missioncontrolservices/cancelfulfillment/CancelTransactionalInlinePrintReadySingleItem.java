/**
 *
 */
package com.shutterfly.missioncontrolservices.cancelfulfillment;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrolservices.common.ValidationUtilConfig;
import com.shutterfly.missioncontrolservices.config.ConfigLoader;
import com.shutterfly.missioncontrolservices.config.CsvReaderWriter;
import com.shutterfly.missioncontrolservices.util.AppConstants;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

/**
 * @author dgupta
 */
public class CancelTransactionalInlinePrintReadySingleItem extends ConfigLoader {

  /**
   *
   */
  private String uri = "";
  private String payload = "";
  private String record = "";

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionCancelFulfillment");
    return uri;

  }

  private String buildPayload() throws IOException {
    URL file = Resources
        .getResource(
            "XMLPayload/CancelFulfillment/CancelTransactionalInlinePrintReadySingleItem.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    record = cwr.getRequestIdByKeys("TIPRSI");

    return payload = payload.replaceAll("REQUEST_101", record);

  }

  CsvReaderWriter cwr = new CsvReaderWriter();

  @Test(groups = "Cancel_TIPRSI_Response", dependsOnGroups = {"PostForArchive_TIPRSI_DB"})
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


  @Test(groups = "Cancel_TIPRSI_DB", dependsOnGroups = {"Cancel_TIPRSI_Response"})
  private void validateRecordsInDatabase() throws Exception {

    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.CANCEL);
  }
}