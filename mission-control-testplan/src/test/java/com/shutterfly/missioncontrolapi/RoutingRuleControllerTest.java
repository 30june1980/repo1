package com.shutterfly.missioncontrolapi;

import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class RoutingRuleControllerTest extends ConfigLoader {

    private AccessToken accessToken;
    private String token;

    @BeforeClass
    public void setup() {
        accessToken = new AccessToken();
        token = accessToken.getAccessToken();
    }

    @Test
    public void uniqueRoutingRules() {
        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/routingrule/details/unique");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void findAllRoutingRule() {
        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .queryParam("pageNumber", "1").queryParam("pageSize", "1").contentType(ContentType.JSON)
                .when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/routingrule");
        Assert.assertEquals(response.getStatusCode(), 200);
    }




}
