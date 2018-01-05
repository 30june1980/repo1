package com.shutterfly.missioncontrol.findfulfillmenthistory;

import static org.hamcrest.Matchers.anything;
import static org.testng.Assert.assertEquals;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Created by Shweta on 05-01-2018.
 */
public class FindFulfillmentHistory extends ConfigLoader {

  private AccessToken accessToken;
  private String token;
  private String uri = "";

  @BeforeClass
  public void setup() {
    accessToken = new AccessToken();
    token = accessToken.getAccessToken();
  }

  private String getProperties() {
    basicConfigNonWeb();
    uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionFindFulfillmentHistory");
    return uri;
  }

  private String buildPayload() throws IOException {
    URL file = Resources.getResource("JSONPayload/FindFulfillmentHistory.json");
    String payload = Resources.toString(file, StandardCharsets.UTF_8);
    return payload;
  }

  @Test
  public void findFulfillmentHistory() throws IOException {
    Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
        .all().contentType("application/json").accept("application/json")
        .body(this.buildPayload()).when().post(this.getProperties());
    assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    response.then().body(anything("Non empty response"));
  }

}
