/**
 *
 */
package com.shutterfly.missioncontrol.processfulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.AppConstants;
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
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bson.Document;
import org.testng.annotations.Test;

/**
 * @author dgupta
 */
public class TransactionalInlinePrintReadyMultItem_NonBatchableSingleItems extends ConfigLoader {

  /**
   *
   */
  private String uri = "";
  private String payload = "";

  UUID uuid = UUID.randomUUID();
  String record = "Test_qa_" + uuid.toString();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessFulfillment");
    return uri;
  }

  private String buildPayload() throws IOException {
    URL file = Resources.getResource(
        "XMLPayload/ProcessFulfillment/TransactionalInlinePrintReadyMultItem_NonBatchableSingleItems.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);

    return payload = payload.replaceAll("REQUEST_101", record);

  }

  CsvReaderWriter cwr = new CsvReaderWriter();

  @Test(groups = "Process_TIPRMI_NBSI_Response")
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
    cwr.writeToCsv("TIPRMI_NBSI", record);

  }


  @Test(groups = "Process_TIPRMI_NBSI_DB", dependsOnGroups = {"Process_TIPRMI_NBSI_Response"})
  private void validateRecordsInDatabase() throws Exception {

    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record, "PutToRequestGeneratorTopic",
            AppConstants.PROCESS);
  }

  @Test(dependsOnGroups = {"Process_TIPRMI_NBSI_DB"})
  private void validateSingleItemRecordsInDatabase() throws Exception {

    ValidationUtilConfig.getInstances()
        .validateRecordsAvailabilityAndStatusCheck(record + "_2", AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.PROCESS);
  }

  @Test(dependsOnGroups = {"Process_TIPRMI_NBSI_DB"})
  private void validateSingleItemRecordsInDatabaseForEventHistory() throws Exception {

    Document fulfillmentTrackingRecordDoc=ValidationUtilConfig.getInstances()
        .getTrackingRecord(record );
    List<Document> document= (List<Document>) fulfillmentTrackingRecordDoc.get("eventHistory");
    List<String> lisrOfEventHistory=new ArrayList<>(10);
    document.forEach(x-> {
      lisrOfEventHistory.add(x.get("eventType").toString());
    });
    assertEquals((lisrOfEventHistory.contains("ReceivedPending")||lisrOfEventHistory.contains("cancelPending")),true);
  }

  @Test(dependsOnGroups = {"Process_TIPRMI_NBSI_DB"})
  private void validateSingleItemRecordsInDatabaseForChildRequest() throws Exception {

    Document fulfillmentTrackingRecordDoc=ValidationUtilConfig.getInstances()
        .getTrackingRecord(record +"_1");
    Document fulfillmentTrackingRecordDoc1=ValidationUtilConfig.getInstances()
        .getTrackingRecord(record +"_2");

   assertEquals((Objects.nonNull(fulfillmentTrackingRecordDoc) && Objects.nonNull(fulfillmentTrackingRecordDoc1)),true);
  }
}