package com.shutterfly.missioncontrol.validation.processfulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.testng.Assert.assertEquals;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.AppConstants;
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
  private void nameValuePairMismatch() throws IOException {
    basicConfigNonWeb();
    EncoderConfig encoderconfig = new EncoderConfig();
    String payload = Utils.replaceInStringFromTill(this.buildPayload(),"<v7:name>","</v7:name>","");
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(payload).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionLevelErrors.transactionError.errorCode.code",
        equalTo("18062"));

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
    String payload=Utils.replaceInStringFromTill(this.buildPayload(),"<sch:archiveTransactionalDetail>","</sch:archiveTransactionalDetail>",toBeReplacedWith);
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


  private String buildPayloadForArchive() throws IOException {
    URL file = Resources.getResource("XMLPayload/ProcessArchive/ProcessArchiveBulkDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);
    return payload.replaceAll("REQUEST_101", record).replaceAll("bulkfile_all_valid.xml",
        (record + AppConstants.ARCHIVE_SUFFIX + ".xml"));
  }

  @Test()
  private void InvalidRequestDetails_1() throws IOException {
    basicConfigNonWeb();
    EncoderConfig encoderconfig = new EncoderConfig();

    String toBeReplacedWith=" <sch:archiveTransactionalDetail>\n"
        + "                        <sch:recipient>\n"
        + "                            <sch:recipientId>111223333</sch:recipientId>\n"
        + "                            <!--Optional:-->\n"
        + "                            <sch:idQualifier>SocialSecIDType</sch:idQualifier>\n"
        + "                            <!--Optional:-->\n"
        + "                            <sch:deliveryMethod1>Mailed</sch:deliveryMethod1>\n"
        + "                            <!--Optional:-->\n"
        + "                            <sch:deliveryMethod2>Emailed</sch:deliveryMethod2>\n"
        + "                            <!--Optional:-->\n"
        + "                            <sch:recipientType>MemberType</sch:recipientType>\n"
        + "                            <sch:person>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:prefix>Mr.</sch:prefix>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:firstName>SampleFirstName</sch:firstName>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:middleName>String</sch:middleName>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:lastName>SampleLastName</sch:lastName>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:suffix>Jr</sch:suffix>\n"
        + "                            </sch:person>\n"
        + "                            <!--You have a CHOICE of the next 2 items at this level-->\n"
        + "                            <!--Optional:-->\n"
        + "                            <sch:MailToAddress>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:Address1>1001 Main Street</sch:Address1>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:Address2>String</sch:Address2>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:Address3>String</sch:Address3>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:City>Main City</sch:City>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:State>CA</sch:State>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:Zip>999999</sch:Zip>\n"
        + "                            </sch:MailToAddress>\n"
        + "                            <sch:ReturnToAddressInternational>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:Address1>1A Some Street Name</sch:Address1>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:Address2>String</sch:Address2>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:Address3>String</sch:Address3>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:City>String?</sch:City>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:Province>SomeProvinceName</sch:Province>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:PostalCD>String</sch:PostalCD>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:Country>999</sch:Country>\n"
        + "                            </sch:ReturnToAddressInternational>\n"
        + "                            <!--Optional:-->\n"
        + "                            <sch:faxNumber>String</sch:faxNumber>\n"
        + "                            <!--Optional:-->\n"
        + "                            <sch:emailAddress>SomeEmailAddress.com</sch:emailAddress>\n"
        + "                            <!--Optional:-->\n"
        + "                            <sch:carbonCopyInd>false</sch:carbonCopyInd>\n"
        + "                        </sch:recipient>\n"
        + "                        <sch:data>\n"
        + "                            <!--Optional:-->\n"
        + "                            <sch:externalFileType>\n"
        + "\t\t\t\t\t\t<!--Optional: -->\n"
        + "\t\t\t\t\t\t<sch:filePath>/MissionControl/RRD</sch:filePath>\n"
        + "\t\t\t\t\t\t<sch:fileName>bulkfile_all_valid.xml</sch:fileName>\n"
        + "\t\t\t\t\t\t<sch:fileSize>9999</sch:fileSize>\n"
        + "\t\t\t\t\t</sch:externalFileType>\n"
        + "                            <sch:contentFormatType>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:contentStream>Binary content stream of document</sch:contentStream>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:documentType>DOC_CLASS_123</sch:documentType>\n"
        + "                                <!--Optional:-->\n"
        + "                                <sch:mimeType>String</sch:mimeType>\n"
        + "                                <!--0 to 20 repetitions:-->\n"
        + "                                <sch:documentMetadata>\n"
        + "                                    <!--Optional:-->\n"
        + "                                    <v7:name>postingDate</v7:name>\n"
        + "                                    <!--Optional:-->\n"
        + "                                    <v7:value>mm/dd/yyyy</v7:value>\n"
        + "                                </sch:documentMetadata>\n"
        + "                                <sch:documentMetadata>\n"
        + "                                    <!--Optional:-->\n"
        + "                                    <v7:name>memberID</v7:name>\n"
        + "                                    <!--Optional:-->\n"
        + "                                    <v7:value>Some member ID value</v7:value>\n"
        + "                                </sch:documentMetadata>\n"
        + "                            </sch:contentFormatType>\n"
        + "                        </sch:data>\n"
        + "                    </sch:archiveTransactionalDetail>";
    String payload=Utils.replaceInStringFromTill(this.buildPayloadForArchive(),"<sch:archiveBulkDetail>","</sch:archiveBulkDetail>",toBeReplacedWith);
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(payload).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionLevelErrors.transactionError.errorCode.code",
        equalTo("18408"));

  }


}
