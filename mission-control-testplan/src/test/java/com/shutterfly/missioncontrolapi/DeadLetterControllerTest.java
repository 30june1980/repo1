package com.shutterfly.missioncontrolapi;

import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;

public class DeadLetterControllerTest extends ConfigLoader {

    private AccessToken accessToken;
    private String token;
    List<Object> messageIdentifier1;

    @BeforeClass
    public void setup() {
        accessToken = new AccessToken();
        token = accessToken.getAccessToken();
    }

    @Test
    public void deadLetterCount() {
        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/dead-letter/count");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void getAllValidRecords() {
        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .queryParam("pageNumber", "1").queryParam("pageSize", 1).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/dead-letter");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void getDetailsOfDeadLetterRecord() {

        String transactionId = given().header("Accept", "application/json").header("Authorization", token)
                .queryParam("pageNumber", "1").queryParam("pageSize", 1).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/dead-letter").then().body("isEmpty()", Matchers.is(false))
                .extract().path("content[0].transactionId");

        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .pathParam("transactionId", transactionId).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/dead-letter/details/{transactionId}");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void getDeadLetterRecordsByMessageIdentifier() {

        String messageIdentifier = given().header("Accept", "application/json").header("Authorization", token)
                .queryParam("pageNumber", "1").queryParam("pageSize", 1).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/dead-letter").then().body("isEmpty()", Matchers.is(false))
                .extract().path("content[0].messageIdentifier");

        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .pathParam("messageIdentifier", messageIdentifier).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/dead-letter/{messageIdentifier}");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test(groups = "GET_DEADLETTER_BY_RECORDS_ID")
    public void getDeadLetterRecordsByMessageIdentifier1() {

        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .queryParam("pageNumber", "1").queryParam("pageSize", 20).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/dead-letter").then().body("isEmpty()", Matchers.is(false))
                .extract().response();

        List<Object> messageIdentifier = response.jsonPath().getList("content.messageIdentifier").stream().filter(x->!x.equals("")).collect(Collectors.toList());
        messageIdentifier1= messageIdentifier.subList(0,10);
    }

    @Test(groups = "VALIDTAE_1",dependsOnGroups = "GET_DEADLETTER_BY_RECORDS_ID")
    public void validate(){
        messageIdentifier1.forEach(x->{
            Response res=given().header("Accept", "application/json").header("Authorization", token)
                    .pathParam("messageIdentifier", String.valueOf(x)).log().all()
                    .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                            + "/api/services/v1/dead-letter/{messageIdentifier}");
            Assert.assertEquals(res.getStatusCode(), 200);
            Assert.assertEquals(!res.getBody().as(List.class).isEmpty(), true);
        });
    }
}
