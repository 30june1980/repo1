package com.shutterflly.missioncontrolapi;

import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class ConstantsControllerTest extends ConfigLoader {

    private AccessToken accessToken;
    private String token;

    @BeforeClass
    public void setup() {
        accessToken = new AccessToken();
        token = accessToken.getAccessToken();
    }

    @Test
    public void portalConfigurationTests() {
        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/portal/configurations");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

}
