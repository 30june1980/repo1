/**
 *
 */
package com.shutterfly.missioncontrol.postforcancel;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.AppConstants;
import com.shutterfly.missioncontrol.common.EcgFileSafeUtil;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.testng.annotations.Test;

/**
 * @author dgupta
 */
public class PostBulkPrintReadyForCancel extends ConfigLoader {

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
        .getResource("XMLPayload/PostForCancel/PostBulkPrintReadyForCancel.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    record = cwr.getRequestIdByKeys("BPR");

    return payload = payload.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml",
        (record + AppConstants.POST_FOR_CANCEL_SUFFIX + ".xml"));

  }

  CsvReaderWriter cwr = new CsvReaderWriter();

  @Test(groups = "PostForCancel_BPR_Response", dependsOnGroups = {"Cancel_BPR_DB"})
  private void getResponse() throws IOException {
    basicConfigNonWeb();
    String payload = this.buildPayload();
    record = record + AppConstants.POST_FOR_CANCEL_SUFFIX;

    EcgFileSafeUtil.putFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload), record,
        AppConstants.BULK_FILE_FOR_CANCEL);
    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(this.buildPayload()).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));

  }


  @Test(groups = "PostForCancel_BPR_DB", dependsOnGroups = {"PostForCancel_BPR_Response"})
  private void validateRecordsInDatabase() throws Exception {

    record = record.replace(AppConstants.POST_FOR_CANCEL_SUFFIX, "");
    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_REQUESTOR,
            AppConstants.POST_STATUS);
  }
}