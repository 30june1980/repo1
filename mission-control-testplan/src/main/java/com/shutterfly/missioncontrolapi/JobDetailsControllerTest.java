package com.shutterfly.missioncontrolapi;

import com.shutterfly.missioncontrolservices.accesstoken.AccessToken;
import com.shutterfly.missioncontrolservices.config.ConfigLoader;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class JobDetailsControllerTest extends ConfigLoader {

    private AccessToken accessToken;
    private String token;

    @BeforeClass
    public void setup() {
        accessToken = new AccessToken();
        token = accessToken.getAccessToken();
    }

    @Test
    public void getAllJobDetails() {
        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/quartz-details");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

}
