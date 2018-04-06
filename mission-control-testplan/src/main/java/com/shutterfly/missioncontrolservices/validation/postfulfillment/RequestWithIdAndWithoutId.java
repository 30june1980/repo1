package com.shutterfly.missioncontrolservices.validation.postfulfillment;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrolservices.config.ConfigLoader;
import com.shutterfly.missioncontrolservices.config.CsvReaderWriter;
import com.shutterfly.missioncontrolservices.util.AppConstants;
import com.shutterfly.missioncontrolservices.util.Util;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

public class RequestWithIdAndWithoutId  extends ConfigLoader{

  private String uri = "";
  private String payload = "";
  private String record = "";
  CsvReaderWriter cwr = new CsvReaderWriter();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionPostFulfillment");
    return uri;
  }

  private String buildPayloadForWithoutId() throws IOException {
    URL file = Resources
        .getResource("XMLPayload/PostFulfillment/PostTransactionalInlineDataOnly.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    return payload = payload.replaceAll("REQUEST_101","");
  }

  private String buildPayLoadWithNewRequestId() throws IOException{
    URL file = Resources
        .getResource("XMLPayload/PostFulfillment/PostTransactionalInlineDataOnly.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    return payload = payload.replaceAll("REQUEST_101", Util.getQARandomId());
  }


  @Test()
  private void getResponseWithoutId() throws IOException {
    basicConfigNonWeb();
    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(this.buildPayloadForWithoutId()).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo(AppConstants.REJECTED));

    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionLevelErrors.transactionError.errorCode.code",
        equalTo("18000"));

  }

  @Test()
  private void getResponseWithId() throws IOException {
    basicConfigNonWeb();
    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all()
        .contentType("application/xml").body(this.buildPayLoadWithNewRequestId()).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo(AppConstants.REJECTED));

    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionLevelErrors.transactionError.errorCode.code",
        equalTo("18420"));

  }

}
