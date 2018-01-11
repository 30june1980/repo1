package com.shutterfly.missioncontrolapi;

import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class ParticipantControllerTest extends ConfigLoader {

    private AccessToken accessToken;
    private String token;

    @BeforeClass
    public void setup() {
        accessToken = new AccessToken();
        token = accessToken.getAccessToken();
    }

    @Test
    public void getAllUniqueLatestParticipantsLatestSortedByStatus() {
        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/participants/");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void getParticipantTypes() {
        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/participants/types");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void getParticipantDetail() {
        Response res = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/participants/");

        String participantId = res.getBody().path("participantId[1]");
        String participantType = res.getBody().path("participantType[1].code");

        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).pathParam("participantType", participantType)
                .pathParam("participantId", participantId).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/participant/history/{participantType}/{participantId}");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void findParticipantByIDAndType() {
        Response res = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/participants/");

        String participantId = res.getBody().path("participantId[1]");
        String participantType = res.getBody().path("participantType[1].code");

        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).pathParam("participantType", participantType)
                .pathParam("participantId", participantId).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/participant/{participantId}/{participantType}");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void getByStatus() {
        Response res = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/participants/");

        String participantType = res.getBody().path("participantType[1].code");

        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).pathParam("participantTypeCode", participantType)
                .queryParam("status", "Retired").when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/participants/{participantTypeCode}/bystatus");
        Assert.assertEquals(response.getStatusCode(), 200);
    }
}
