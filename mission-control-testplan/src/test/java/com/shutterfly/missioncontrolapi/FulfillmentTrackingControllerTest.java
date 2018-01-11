package com.shutterfly.missioncontrolapi;

import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;

public class FulfillmentTrackingControllerTest extends ConfigLoader {

    private AccessToken accessToken;
    private String token;
    private String batchRequestId = "MC_BATCH_REQUEST_6d126016-4fb6-4dd0-9861-16131acc0df6";

    @BeforeClass
    public void setup() {
        accessToken = new AccessToken();
        token = accessToken.getAccessToken();
    }

    @Test
    public void findFulfillmentDocAndCountByRuleID() {
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .queryParam("ruleId", "Test1").log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/fulfillmenttracking/batch");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void getAllQueuedCancellationRequestsForBulkRequest() {
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .pathParam("bulkRequestId", "1").log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/fulfillmenttracking/bulk/{bulkRequestId}/queued-cancel-requests");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void findMultiItemsByMultItemRequestHeaderId() {
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .pathParam("multItemRequestHeaderId", "1").log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/fulfillmenttracking/multiitems/{multItemRequestHeaderId}");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void getFulfillmentDocsForRequestIds() {
        String[] requestIds = new String[]{batchRequestId};
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .queryParam("requestIds", (Object[]) requestIds).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/fulfillmenttracking/requests/");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void getBatchedTransactionalRecords() {
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .pathParam("bulkRequestId", batchRequestId).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/fulfillmenttracking/{bulkRequestId}/batcheditems");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void findFulfillmentDocByID() {
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .pathParam("requestID", batchRequestId).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/fulfillmenttracking/{requestID}");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void findCancelRequestByID() {
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .pathParam("requestID", batchRequestId).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/fulfillmenttracking/{requestID}/cancel");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void findFulfillmentDocMetaDataByID() {
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .pathParam("requestID", batchRequestId).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/fulfillmenttracking/{requestID}/metadata");
        Assert.assertEquals(response.getStatusCode(), 200);
    }
}
