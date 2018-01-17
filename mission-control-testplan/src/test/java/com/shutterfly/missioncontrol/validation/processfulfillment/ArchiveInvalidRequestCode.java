package com.shutterfly.missioncontrol.validation.processfulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.utils.Utils;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.testng.annotations.Test;

public class ArchiveInvalidRequestCode extends ConfigLoader {

  private String uri = "";
  private String payload = "";
  String record = Utils.getQARandomId();

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessArchive");
    return uri;

  }

  private String buildPayload() throws IOException {
    URL file = Resources
        .getResource("XMLPayload/ProcessArchive/ProcessArchiveTransactionalInlineDataOnly.xml");
    payload = Resources.toString(file, StandardCharsets.UTF_8);
    return payload = payload.replaceAll("REQUEST_101", record)
        .replaceAll("bulkfile_all_valid.xml", (record + ".xml"));
  }

  @Test()
  private void validRequestCategories() throws IOException {
    basicConfigNonWeb();
    EncoderConfig encoderconfig = new EncoderConfig();
    String payload = this.buildPayload();
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(Utils.replaceExactMatch(this.buildPayload(),"TransactionalInlineDataOnly","InvalidRequestCategory")).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionLevelErrors.transactionError.errorCode.code",
        equalTo("18005"));

  }

  @Test()
  private void InvalidRequestDetails() throws IOException {
    basicConfigNonWeb();
    EncoderConfig encoderconfig = new EncoderConfig();

    String toBeReplacedWith="<sch:bulkRequestDetail>\n"
        + " \n"
        + "            <!--Optional:-->\n"
        + " \n"
        + "            <sch:filePath>/MissionControl/bulkFiles/BRMS</sch:filePath>\n"
        + " \n"
        + "            <sch:fileName>bulkfile_all_valid.xml</sch:fileName>\n"
        + " \n"
        + "            <sch:fileSize>4096</sch:fileSize>\n"
        + " \n"
        + "            <!--Optional:-->\n"
        + " \n"
        + "            <sch:ecgDetail>text</sch:ecgDetail>\n"
        + " \n"
        + "            <!--Optional:-->\n"
        + " \n"
        + "            <sch:sourceDetail>text</sch:sourceDetail>\n"
        + " \n"
        + "          </sch:bulkRequestDetail>";
    String payload=Utils.relaceInStringFromTill(this.buildPayload(),"<sch:archiveTransactionalDetail>","</sch:archiveTransactionalDetail>",toBeReplacedWith);
     Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(payload).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionLevelErrors.transactionError.errorCode.code",
        equalTo("18409"));

  }



}
