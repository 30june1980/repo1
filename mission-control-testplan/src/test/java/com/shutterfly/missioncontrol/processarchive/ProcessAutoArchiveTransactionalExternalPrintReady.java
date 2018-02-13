package com.shutterfly.missioncontrol.processarchive;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.common.EcgFileSafeUtil;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;
import com.shutterfly.missioncontrol.util.AppConstants;
import com.shutterfly.missioncontrol.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.bson.Document;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ProcessAutoArchiveTransactionalExternalPrintReady extends ConfigLoader {

    private String uri = "";
    String record = Utils.getQARandomId();
    DatabaseValidationUtil databaseValidationUtil = ValidationUtilConfig.getInstances();
    CsvReaderWriter cwr = new CsvReaderWriter();

    private String getProperties() {
        basicConfigNonWeb();
        uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessFulfillment");
        return uri;
    }

    private String buildPayload() throws IOException {
        URL file = Resources
                .getResource("XMLPayload/ProcessFulfillment/TransactionalExternalPrintReadyAutoArchive.xml");
        String payload = Resources.toString(file, StandardCharsets.UTF_8);

        return payload.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml",
                (record + ".xml"));

    }

    private String buildPayloadForPost() throws IOException {
        URL file = Resources
                .getResource("XMLPayload/PostFulfillment/PostTransactionalExternalPrintReadyInArchive.xml");
        String payload = Resources.toString(file, StandardCharsets.UTF_8);
        return  payload.replaceAll("REQUEST_101", record);
    }

    private String buildPayloadForPostArchive() throws IOException {
        URL file = Resources
                .getResource("XMLPayload/PostFulfillment/PostTransactionalExternalPrintReadyInAutoArchive.xml");
        String payload = Resources.toString(file, StandardCharsets.UTF_8);
        return payload.replaceAll("REQUEST_101", record);
    }

    @Test(groups = "Process_TEPR_Response_Auto")
    private void getResponse() throws IOException {
        basicConfigNonWeb();
        String payload = this.buildPayload();

        EcgFileSafeUtil.putFileAtSourceLocation(EcgFileSafeUtil.buildInboundFilePath(payload),
                record, AppConstants.BULK_FILE);

        EncoderConfig encoderconfig = new EncoderConfig();
        Response response = given()
                .config(RestAssured.config()
                        .encoderConfig(
                                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
                .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
                .body(payload).when().post(this.getProperties());
        assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
        response.then().body(
                "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
                equalTo("Accepted"));

    }

    @Test(groups = "Process_TEPR_DB_AutoArchive1", dependsOnGroups = {"Process_TEPR_Response_Auto"})
    private void validateAutoArchiveInDatabase() throws Exception {
        databaseValidationUtil
                .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_ARCHIVAL_SYSTEM,
                        AppConstants.ARCHIVE);

    }


    @Test(groups = "Post_TEPR_Response_auto1", dependsOnGroups = {"Process_TEPR_DB_AutoArchive1"})
    private void getResponseForPostReceived() throws IOException {
        basicConfigNonWeb();
        Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
                .all()
                .contentType("application/xml").body(this.buildPayloadForPost()).when().post(config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment"));
        assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
        response.then().body(
                "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
                equalTo("Accepted"));

    }

    @Test(groups = "Post_TEPR_Response_auto2", dependsOnGroups = {"Post_TEPR_Response_auto1"})
    private void getResponseForPostRecieved() throws IOException {
        basicConfigNonWeb();
        Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
                .all()
                .contentType("application/xml").body(Utils.replaceExactMatch(this.buildPayloadForPost(),"Generated","Received")).when().post(config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment"));
        assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
        response.then().body(
                "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
                equalTo("Accepted"));

    }

    @Test(groups = "Post_TEPR_Response_auto3", dependsOnGroups = {"Post_TEPR_Response_auto2"})
    private void getResponseForPostFulfilled() throws IOException {
        basicConfigNonWeb();
        Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
                .all()
                .contentType("application/xml").body(Utils.replaceExactMatch(this.buildPayloadForPost(),"Generated","Fulfilled")).when().post(config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment"));
        assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
        response.then().body(
                "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
                equalTo("Accepted"));

    }

    @Test(groups = "Post_TEPR_DB", dependsOnGroups = {"Post_TEPR_Response_auto3"})
    private void validateRecordsInDatabaseForPost() throws Exception {
        ValidationUtilConfig.getInstances()
                .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_REQUESTOR,
                        AppConstants.POST_STATUS);
    }

    @Test(groups = "PostForArchive_TEPR_Response", dependsOnGroups = {"Post_TEPR_DB"})
    private void getResponseForPostArchive() throws IOException {
        basicConfigNonWeb();
        Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
                .all()
                .contentType("application/xml").body(this.buildPayloadForPostArchive()).when().post(config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment"));
        assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
        response.then().body(
                "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
                equalTo("Accepted"));

    }

    @Test(groups = "PostForArchive_TEPR_DB", dependsOnGroups = {"PostForArchive_TEPR_Response"})
    private void validateRecordsInDatabaseForPostArchive() throws Exception {
        ValidationUtilConfig.getInstances()
                .validateRecordsAvailabilityAndStatusCheck(record, AppConstants.ACCEPTED_BY_REQUESTOR,
                        AppConstants.POST_STATUS);
    }

    @Test(groups = "PostForArchive_TEPR_DB1", dependsOnGroups = {"PostForArchive_TEPR_DB"})
    private void validateRecordsInDatabaseAfterPostArchive() throws Exception {
        Document fulfillmentTrackingRecord=ValidationUtilConfig.getInstances().getTrackingRecord(record);
        assertNotNull(fulfillmentTrackingRecord);
        ArrayList eventHistoryList = (ArrayList<Document>) fulfillmentTrackingRecord.get("eventHistory");


        assertEquals( eventHistoryList.stream().anyMatch(x->((Document)x).get("eventType").toString().equals("ReceivedPending")), true);
        assertEquals( eventHistoryList.stream().anyMatch(x->((Document)x).get("eventType").toString().equals("ArchivePending")), true);
        assertEquals( eventHistoryList.stream().anyMatch(x->((Document)x).get("eventType").toString().equals("Generated")), true);
        assertEquals( eventHistoryList.stream().anyMatch(x->((Document)x).get("eventType").toString().equals("Received")), true);
        assertEquals( eventHistoryList.stream().anyMatch(x->((Document)x).get("eventType").toString().equals("Fulfilled")), true);
        assertEquals( eventHistoryList.stream().anyMatch(x->((Document)x).get("eventType").toString().equals("ArchiveConfirmed")), true);
        assertEquals( fulfillmentTrackingRecord.get("currentFulfillmentStatus").toString(), "FULFILLED");
        assertEquals( fulfillmentTrackingRecord.get("currentArchivalStatus").toString(), "ARCHIVE_CONFIRMED");
        assertNotNull(fulfillmentTrackingRecord.get("fulfillmentRequest"));
        assertNotNull(fulfillmentTrackingRecord.get("archiveRequest"));
        assertNotNull(fulfillmentTrackingRecord.get("postFulfillmentStatus"));


    }

}
