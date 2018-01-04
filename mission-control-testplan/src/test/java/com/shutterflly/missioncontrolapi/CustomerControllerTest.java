package com.shutterflly.missioncontrolapi;

import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class CustomerControllerTest extends ConfigLoader {

    private AccessToken accessToken;
    private String token;
    private String segmentId = "EI";
    private String childSegmentId = "SMB";
    private String customerId = "1";

    @BeforeClass
    public void setup() {
        accessToken = new AccessToken();
        token = accessToken.getAccessToken();
    }

    @Test
    public void getAllActiveAndPendingSegments() {
        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/customer/segments/active-pending");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void getSegment() {
        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).pathParam("customerId", customerId).pathParam("segmentId", segmentId)
                .when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/customer/{customerId}/segment/{segmentId}");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void getSegmentsForCustomer() {
        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).pathParam("customerId", customerId)
                .when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/customer/{customerId}/segments");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void getChildSegment() {
        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).pathParam("customerId", customerId).pathParam("segmentId", segmentId)
                .pathParam("childSegmentId", childSegmentId).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/customer/{customerId}/segment/{segmentId}/child/{childSegmentId}");
        Assert.assertEquals(response.getStatusCode(), 200);
    }


}
