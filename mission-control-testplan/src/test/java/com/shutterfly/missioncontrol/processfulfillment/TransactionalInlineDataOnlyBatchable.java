package com.shutterfly.missioncontrol.processfulfillment;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import com.shutterfly.missioncontrol.util.AppConstants;
import com.shutterfly.missioncontrol.util.TrackingRecordValidationUtil;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.bson.Document;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author dgupta
 */
public class TransactionalInlineDataOnlyBatchable extends ConfigLoader {

  private String uri = "";
  UUID uuid = UUID.randomUUID();
  String record = "Test_qa_" + uuid.toString();
  DatabaseValidationUtil databaseValidationUtil = ValidationUtilConfig.getInstances();


  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessFulfillment");
    return uri;
  }

  private String buildPayload() throws IOException {
    URL file = Resources
        .getResource("XMLPayload/ProcessFulfillment/TransactionalInlineDataOnlyBatchable.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);
    return payload.replaceAll("REQUEST_101", record);
  }

  @Test(groups = "Process_TIDO_Batchable_Response")
  private void getResponse() throws IOException {
    basicConfigNonWeb();
    EncoderConfig encoderconfig = new EncoderConfig();
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(this.buildPayload()).when().post(this.getProperties());

    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));
    CsvReaderWriter cwr = new CsvReaderWriter();
    cwr.writeToCsv("TIDOB", record);

  }

  @Test(groups = "Process_TIDO_Batchable_DB", dependsOnGroups = {"Process_TIDO_Batchable_Response"})
  private void validateRequestStatus() throws Exception {
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.REQUEST_BATCHED,
            AppConstants.PROCESS);
  }

  @Test(groups = "Process_TIDO_Batchable_DB_Fields", dependsOnGroups = {
      "Process_TIDO_Batchable_DB"})
  private void validateRecordInDatabase() throws Exception {
    Document fulfillmentTrackingRecordDoc = databaseValidationUtil.getTrackingRecord(record);
    TrackingRecordValidationUtil
        .validateTransactionalProcessRequestFields(this.buildPayload(), fulfillmentTrackingRecordDoc);
  }
}