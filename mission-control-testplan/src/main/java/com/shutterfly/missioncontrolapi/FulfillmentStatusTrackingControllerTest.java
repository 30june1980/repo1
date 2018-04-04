package com.shutterfly.missioncontrolapi;

import com.shutterfly.missioncontrolservices.accesstoken.AccessToken;
import com.shutterfly.missioncontrolservices.config.ConfigLoader;
import com.shutterfly.missioncontrolservices.config.CsvReaderWriter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class FulfillmentStatusTrackingControllerTest extends ConfigLoader {

  private AccessToken accessToken;
  private String token;
  CsvReaderWriter cwr = new CsvReaderWriter();
  private String requestId;

  @BeforeClass
  public void setup() throws IOException {
    accessToken = new AccessToken();
    token = accessToken.getAccessToken();
    requestId = cwr.getRequestIdByKeys("TIDO");
  }

  @Test
  public void findStatusTrackingById() {
    Response response = given().header("Accept", "application/json").header("Authorization", token)
        .pathParam("requestID", requestId).log().all()
        .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
            + "/api/services/v1/statustracking/{requestID}");
    Assert.assertEquals(response.getStatusCode(), 200);
  }

}
