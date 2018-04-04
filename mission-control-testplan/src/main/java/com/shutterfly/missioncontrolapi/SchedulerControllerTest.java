package com.shutterfly.missioncontrolapi;

import com.shutterfly.missioncontrolservices.accesstoken.AccessToken;
import com.shutterfly.missioncontrolservices.config.ConfigLoader;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;

/**
 * Created by Ruksad siddiqui on 9/3/18
 */
public class SchedulerControllerTest extends ConfigLoader {

    private AccessToken accessToken;
    private String token;
    private String uri = "";

    @BeforeClass
    public void setup() {
        accessToken = new AccessToken();
        token = accessToken.getAccessToken();
    }


    @Test
    public void getSchedules() {
        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/jobs");
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void getSchedulesNonDeleted() {
        Response response = given()
                .header("Accept", "application/json")
                .header("Authorization", token)
                .log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/jobs")
                .then()
                .extract().response();

        List<Object> schedules = response.getBody().as(List.class);
        assertEquals(response.getStatusCode(), 200);
        assertEquals(!schedules.isEmpty(), true);
    }
}
