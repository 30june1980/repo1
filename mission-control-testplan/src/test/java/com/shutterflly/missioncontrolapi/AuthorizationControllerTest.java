package com.shutterflly.missioncontrolapi;

import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class AuthorizationControllerTest {

    private AccessToken accessToken;
    private String token;

    @BeforeClass
    public void setup() {
        accessToken = new AccessToken();
        token = accessToken.getAccessToken();
    }

    @Test
    public void getAuthorizationDetails() {

        Response response1 = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get("http://missioncontrolapi-qa.internal.shutterfly.com/api/services/v1/authorization");
        Assert.assertEquals(response1.getStatusCode(), 200);
    }
}
