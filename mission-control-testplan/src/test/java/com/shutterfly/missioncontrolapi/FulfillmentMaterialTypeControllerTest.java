package com.shutterfly.missioncontrolapi;

import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class FulfillmentMaterialTypeControllerTest extends ConfigLoader {

    private AccessToken accessToken;
    private String token;

    @BeforeClass
    public void setup() {
        accessToken = new AccessToken();
        token = accessToken.getAccessToken();
    }

    @Test
    public void findAllMaterialTypes() {
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .queryParam("pageNumber", "1").queryParam("pageSize", 1).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/fulfillmentmaterialtypes");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void findByMaterialId() {
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .pathParam("materialId", 11900).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/fulfillmentmaterialtypes/{materialId}");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void findMaterialTypesByStatus() {
        String[] statusArray = new String[]{"Active"};
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .queryParam("status", (Object[]) statusArray).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/fulfillmentmaterialtypes/bystatus");
        Assert.assertEquals(response.getStatusCode(), 200);
    }
}
