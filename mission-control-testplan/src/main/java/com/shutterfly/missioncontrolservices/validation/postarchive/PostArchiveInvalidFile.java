package com.shutterfly.missioncontrolservices.validation.postarchive;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrolservices.common.EcgFileSafeUtil;
import com.shutterfly.missioncontrolservices.common.ValidationUtilConfig;
import com.shutterfly.missioncontrolservices.config.ConfigLoader;
import com.shutterfly.missioncontrolservices.util.AppConstants;
import com.shutterfly.missioncontrolservices.util.Util;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

public class PostArchiveInvalidFile extends ConfigLoader {


  private String uri = "";
  private String payloadForProcess = "";
  private String payloadForPost="";
  private String payloadForProcessArchive="";
  private String payloadForPostArchive="";
  private String record = Util.getQARandomId();

  private String getProperties(String uri) {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty(uri);
    return uri;
  }

  private String buildPayloadForPost() throws IOException {
    URL file = Resources.getResource("XMLPayload/PostFulfillment/PostBulkDataOnly.xml");
    this.payloadForPost = Resources.toString(file, StandardCharsets.UTF_8);
    return payloadForPost = payloadForPost.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml",
        (record + AppConstants.POST_SUFFIX + ".xml"));
  }

  private String buildPayloadForProcess() throws IOException {
    URL file = Resources.getResource("XMLPayload/ProcessFulfillment/BulkDataOnly.xml");
    this.payloadForProcess = Resources.toString(file, StandardCharsets.UTF_8);
    return payloadForProcess = payloadForProcess.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml",
        (record + AppConstants.PROCESS + ".xml"));
  }

  private String buildPayloadForProcessArchive() throws IOException {
    URL file = Resources.getResource("XMLPayload/ProcessArchive/ProcessArchiveBulkDataOnly.xml");
    String payloadForProcessArchive = Resources.toString(file, StandardCharsets.UTF_8);
    return payloadForProcessArchive.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml",
        (record + AppConstants.ARCHIVE_SUFFIX + ".xml"));
  }

  private String buildPayloadForPostArchive() throws IOException {
    URL file = Resources.getResource("XMLPayload/PostArchive/PostArchiveBulkDataOnly.xml");
    String payloadForPostArchive = Resources.toString(file, StandardCharsets.UTF_8);
    return payloadForPostArchive = payloadForPostArchive.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml",
        (record + "_PostArchive.xml"));

  }


  @Test(groups = "Process_BDO_Response_Val")
  private void getResponseForProcess() throws IOException, InterruptedException {
    basicConfigNonWeb();
    String payload = this.buildPayloadForProcess();
    EcgFileSafeUtil.putFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload), record+ AppConstants.PROCESS,
        AppConstants.BULK_FILE);

    EncoderConfig encoderconfig = new EncoderConfig();
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(this.buildPayloadForProcess()).when().post(this.getProperties("UrlExtensionProcessFulfillment"));

    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
  }

  @Test(groups = "Process_BDO_DB_val", dependsOnGroups = {"Process_BDO_Response_Val"})
  private void validateRecordsInDatabaseForprocess() throws Exception {
    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.PROCESS);
  }

  @Test(groups = "Post_BDO_Response_val", dependsOnGroups = {"Process_BDO_DB_val"})
  private void getResponseForPost() throws IOException {
    basicConfigNonWeb();
    String payload = this.buildPayloadForPost();

    EcgFileSafeUtil
        .updateAndPutFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload), record,
            AppConstants.BULK_FILE_INVALID, AppConstants.POST_SUFFIX);
    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(this.buildPayloadForPost()).when().post(this.getProperties("UrlExtensionPostFulfillment"));
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
  }


  @Test(groups = "Post_BDO_DB_val", dependsOnGroups = {"Post_BDO_Response_val"})
  private void validateRecordsInDatabaseForPost() throws Exception {
    record = record.replace(AppConstants.POST_SUFFIX, "");
    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_REQUESTOR,
            AppConstants.POST_STATUS);
  }


  @Test(groups = "Archive_BDO_Response_val", dependsOnGroups = {"Post_BDO_DB_val"})
  private void getResponseForProcessArchive() throws IOException {
    basicConfigNonWeb();
    String payload = this.buildPayloadForProcessArchive();
    EcgFileSafeUtil.putFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload),
        record+ AppConstants.ARCHIVE_SUFFIX , AppConstants.BULK_FILE);
    EncoderConfig encoderconfig = new EncoderConfig();
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(this.buildPayloadForProcessArchive()).when().post(this.getProperties("UrlExtensionProcessArchive"));
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));

  }

  @Test(groups = "Archive_BDO_DB_val", dependsOnGroups = {"Archive_BDO_Response_val"})
  private void validateRecordsInDatabase() throws Exception {
    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_ARCHIVAL_SYSTEM,
            AppConstants.ARCHIVE);
  }
}
