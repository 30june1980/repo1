package com.shutterfly.missioncontrol;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.common.DatabaseValidationUtil;
import com.shutterfly.missioncontrol.common.ValidationUtilConfig;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.util.AppConstants;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by Shweta on 19-01-2018.
 */
public class RequestUtil extends ConfigLoader {

  private static DatabaseValidationUtil databaseValidationUtil = ValidationUtilConfig.getInstances();

  //send transactional inline process request
  public static void sendProcess(String requestId) throws Exception {
    basicConfigNonWeb();
    //send process request
    URL file = Resources
        .getResource("XMLPayload/ProcessFulfillment/TransactionalInlineDataOnly.xml");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);
    payload = payload.replaceAll("REQUEST_101", requestId);

    //remove charset from content type using encoder config, build the payload
    EncoderConfig encoderconfig = new EncoderConfig();
    Response response = given()
        .config(RestAssured.config()
            .encoderConfig(
                encoderconfig.appendDefaultContentCharsetToContentTypeIfUndefined(false)))
        .header("saml", config.getProperty("SamlValue")).contentType(ContentType.XML).log().all()
        .body(payload).when()
        .post(config.getProperty("BaseUrl") + config.getProperty("UrlExtensionProcessFulfillment"));

    response.then().body(
        "acknowledgeMsg.acknowledge.validationResults.transactionLevelAck.transaction.transactionStatus",
        equalTo("Accepted"));

    //validate process is sent to supplier
    databaseValidationUtil
        .validateRecordsAvailabilityAndStatusCheck(requestId, AppConstants.ACCEPTED_BY_SUPPLIER,
            AppConstants.PROCESS);
  }

}
