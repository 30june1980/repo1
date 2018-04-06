package com.shutterfly.missioncontrolapi;

import com.shutterfly.missioncontrolservices.accesstoken.AccessToken;
import com.shutterfly.missioncontrolservices.config.ConfigLoader;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class AsyncFileDownloadController extends ConfigLoader {

    private AccessToken accessToken;
    private String token;
    private String userIdentifier = "testMsid";

    @BeforeClass
    public void setup() {
        accessToken = new AccessToken();
        token = accessToken.getAccessToken();
    }

    @Test
    public void asyncFileDownloadsCount() {
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .pathParam("userIdentifier", userIdentifier).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/search/async/exports/download/count/{userIdentifier}");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void asyncFileDownloads() {
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .pathParam("userIdentifier", userIdentifier).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/search/async/exports/{userIdentifier}");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void asyncFileUploads() {
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .queryParam("query", "query").pathParam("userIdentifier", userIdentifier).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/search/async/exports/{userIdentifier}/upload");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

}
