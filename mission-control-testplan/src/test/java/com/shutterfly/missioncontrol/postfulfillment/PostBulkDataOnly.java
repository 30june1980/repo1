/**
 *
 */
package com.shutterfly.missioncontrol.postfulfillment;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.AppConstants;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.common.EcgFileSafeUtil;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import org.bson.Document;
import org.testng.annotations.Test;

/**
 * @author dgupta
 */
public class PostBulkDataOnly extends ConfigLoader {

  private String uri = "";
  private String payload = "";
  private String record = "";
  CsvReaderWriter cwr = new CsvReaderWriter();

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

  @Test(groups = "Post_BDO_Response", dependsOnGroups = {"Process_BDO_DB"})
  private void getResponse() throws IOException {
    basicConfigNonWeb();
    String payload = this.buildPayload();
    record = record + AppConstants.POST_SUFFIX;

    EcgFileSafeUtil
        .updateAndPutFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload), record,
            AppConstants.BULK_FILE);
    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(this.buildPayload()).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
  }

  @Test(groups = "Post_BDO_DB", dependsOnGroups = {"Post_BDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    record = record.replace(AppConstants.POST_SUFFIX, "");
    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_REQUESTOR,
            AppConstants.POST_STATUS);
  }

  @Test(groups = "Post_BDO_DB_Items", dependsOnGroups = {"Post_BDO_Response"})
  private void validateBulkItemsEventHistoryInDatabase() throws Exception {
    DatabaseValidationUtil databaseValidationUtil = ValidationUtilConfig.getInstances();
    Document trackingRecord = databaseValidationUtil
        .getTrackingRecord(record + AppConstants.POST_SUFFIX + "_1");
    assertNotNull(trackingRecord);
    ArrayList eventHistoryList = (ArrayList<Document>) trackingRecord.get("eventHistory");
    Document eventHistory = (Document) eventHistoryList.get(0);
    assertEquals(eventHistory.get("eventType"), AppConstants.RECEIVED);
  }
}