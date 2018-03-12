package com.shutterfly.missioncontrolapi;

import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;

public class FileTransferDetailsControllerTest extends ConfigLoader {

    private AccessToken accessToken;
    private String token;
    List<Object> fileTransferDetailsId;
    @BeforeClass
    public void setup() {
        accessToken = new AccessToken();
        token = accessToken.getAccessToken();
    }

    @Test
    public void findAllUniqueFileTransferDetails() {
        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/filetransferdetails");
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test(groups = "GET_FILE_TRANSFER")
    public void getFileTransferDetails() {

        Response response = given().header("Accept", "application/json").header("Authorization", token)
                .queryParam("pageNumber", "1").queryParam("pageSize", 20).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl")
                        + "/api/services/v1/filetransferdetails").then().body("isEmpty()", Matchers.is(false))
                .extract().response();

        List<Object> messageIdentifier = response.jsonPath().getList("fileTransferDetailsId")
                .stream()
                .filter(x->!x.equals(""))
                .collect(Collectors.toList());
       fileTransferDetailsId = messageIdentifier.subList(0,10);
   }

    @Test(groups = "" ,dependsOnGroups = "GET_FILE_TRANSFER")
    public void verifyFileTransferIdNoTRetorActive(){
        fileTransferDetailsId.forEach(x->{
            Response res=given()
                    .header("Accept", "application/json")
                    .header("Authorization", token)
                    .pathParam("filetransferdetailsid", String.valueOf(x)).log().all()
                    .contentType(ContentType.JSON)
                    .when()
                    .get(config.getProperty("BaseApiUrl")+ "/api/services/v1/filetransferdetails/{filetransferdetailsid}");
            List<String> startDate= res.jsonPath().getList("effectiveInterval.startDate");
            List<String> endDate= res.jsonPath().get("effectiveInterval.endDate");

            LocalDate endDate1 = LocalDate.parse(endDate.get(0), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate date = LocalDate.now();
            assertEquals(endDate1.isAfter(date),true);

        });
    }
}
