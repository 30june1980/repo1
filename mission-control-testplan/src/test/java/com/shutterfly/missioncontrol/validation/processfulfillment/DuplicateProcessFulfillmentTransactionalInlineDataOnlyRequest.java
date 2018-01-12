package com.shutterfly.missioncontrol.validation.processfulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.AppConstants;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.testng.annotations.Test;

public class DuplicateProcessFulfillmentTransactionalInlineDataOnlyRequest extends ConfigLoader {


  private String uri = "";
  String record ;
  CsvReaderWriter cwr = new CsvReaderWriter();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessFulfillment");
    return uri;
  }

  private String buildPayload() throws IOException {
    URL file = Resources
        .getResource("XMLPayload/ProcessFulfillment/TransactionalInlineDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);
    record=cwr.getRequestIdByKeys("TIDO");
    return payload.replaceAll("REQUEST_101", record);
  }

  @Test(groups = "Process_TIDO_Response_duplicate",dependsOnGroups = {"Process_TIDO_DB"})
  private void getResponse() throws IOException {
    basicConfigNonWeb();
    /*
     * remove charset from content type using encoder config
     *
     * build the payload
     */

    EncoderConfig encoderconfig = new EncoderConfig();
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(this.buildPayload()).when().post(this.getProperties());

    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionLevelErrors.transactionError.errorCode.desc",
        equalTo(AppConstants.DUPLICATE_PROCESSFULFILLMENT_REQUEST));
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionLevelErrors.transactionError.errorCode.code",
        equalTo("18419"));
  }

  @Test(groups = "Process_TIDO_DB_duplicate", dependsOnGroups = {"Process_TIDO_Response_duplicate"})
  private void validateRecordsInDatabase() throws Exception {
    DatabaseValidationUtil databaseValidationUtil = new DatabaseValidationUtil();
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.VALIDATION_FAILURE,
            AppConstants.PROCESS);
  }
}
