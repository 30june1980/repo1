package com.shutterfly.missioncontrol.postforarchive;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.AppConstants;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.common.EcgFileSafeUtil;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.bson.Document;
import org.testng.annotations.Test;

/**
 * @author dgupta
 */
public class PostArchiveBulkDataOnly extends ConfigLoader {

  private String uri = "";
  private String record = "";
  DatabaseValidationUtil databaseValidationUtil = ValidationUtilConfig.getInstances();
  CsvReaderWriter cwr = new CsvReaderWriter();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment");
    return uri;
  }

  private String buildPayload() throws IOException {
    URL file = Resources.getResource("XMLPayload/PostArchive/PostArchiveBulkDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);
    record = cwr.getRequestIdByKeys("BDO");
    return payload.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml",
        (record + AppConstants.POST_FOR_ARCHIVE_SUFFIX + ".xml"));
  }

  @Test(groups = "PostForArchive_BDO_Response", dependsOnGroups = {"Archive_BDO_DB"})
  private void getResponse() throws IOException {
    basicConfigNonWeb();
    String payload = this.buildPayload();
    EcgFileSafeUtil.updateAndPutFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload),
        record, AppConstants.BULK_FILE_FOR_ARCHIVE, AppConstants.POST_FOR_ARCHIVE_SUFFIX);
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

  @Test(groups = "PostForArchive_BDO_DB", dependsOnGroups = {"PostForArchive_BDO_Response"})
  private void validateRecordsInDatabase() throws Exception {
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_REQUESTOR,
            AppConstants.POST_STATUS);
  }

  @Test(groups = "PostForArchive_BDO_DB_eventHistory", dependsOnGroups = {"PostForArchive_BDO_DB"})
  private void validateEventHistoryInDatabase() throws Exception {
    Document trackingRecord = databaseValidationUtil.getTrackingRecord(record);
    ArrayList eventHistoryList = (ArrayList<Document>) trackingRecord.get("eventHistory");

    int maxRetry = 6;
    for (int retry = 0; retry <= maxRetry; retry++) {
      try {
        if (eventHistoryList.size()>=4) {
          break;
        } else {
          throw new Exception("eventHistoryList size is less than 4" + record);
        }
      } catch (Exception ex) {
        if (retry >= maxRetry) {
          ex.printStackTrace();
        } else {
          TimeUnit.SECONDS.sleep(10);
        }
      }
    }

    Document eventHistory = (Document) eventHistoryList.get(3);
    assertEquals(eventHistory.get("eventType"), "ArchiveConfirmed");
    assertEquals(eventHistory.get("statusCode"), AppConstants.ACCEPTED);
    ArrayList postFeedbackList = (ArrayList<Document>) eventHistory.get("postFeedbackList");
    Document postFeedback1 = (Document)postFeedbackList.get(0);
    assertEquals(postFeedback1.get("name"),"documentID");
    assertEquals(postFeedback1.get("value"),"Letter31020_04");
    Document postFeedback2 = (Document)postFeedbackList.get(1);
    assertEquals(postFeedback2.get("name"),"documentType");
    assertEquals(postFeedback2.get("value"),"pdfDocument");
    Document postFeedback3 = (Document)postFeedbackList.get(2);
    assertEquals(postFeedback3.get("name"),"repositoryName");
    assertEquals(postFeedback3.get("value"),"Letter052017");
  }

}