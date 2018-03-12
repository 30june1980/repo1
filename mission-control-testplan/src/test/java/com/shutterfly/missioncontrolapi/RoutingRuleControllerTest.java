package com.shutterfly.missioncontrolapi;

import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertEquals;

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
            assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void findAllRoutingRule() {
        Response response = given().header("Accept", "application/json").header("Authorization", token).log().all()
                .queryParam("pageNumber", "1").queryParam("pageSize", "1").contentType(ContentType.JSON)
                .when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/routingrule");
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void verifyIfRoutingRulesAreRetroActive() {
       Response response=  given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/routingrule/details/unique")
                .then()
                .extract()
                .response();
       List<String> list1= response.jsonPath().getList("effectiveInterval.startDate");
       List<String> list2= response.jsonPath().getList("effectiveInterval.endDate");
       List<String> status= response.jsonPath().getList("status");
       for(int i=0;i<list1.size();i++){
           LocalDate localDate = LocalDate.parse(list2.get(i), DateTimeFormatter.ISO_LOCAL_DATE);
           LocalDate date = LocalDate.now();
           assertEquals(localDate.isBefore(date),"Retired".equals(status.get(i)));
       }
    }


    @Test
    public void verifyIfRoutingRulesAreRetroActive1() {
        Response response=  given().header("Accept", "application/json").header("Authorization", token).log().all()
                .contentType(ContentType.JSON).when().get(config.getProperty("BaseApiUrl") + "/api/services/v1/routingrule/details/unique")
                .then()
                .extract()
                .response();
        List<String> list1= response.jsonPath().getList("effectiveInterval.startDate");
        List<String> list2= response.jsonPath().getList("effectiveInterval.endDate");
        List<String> status= response.jsonPath().getList("status");
        for(int i=0;i<list1.size();i++){
            LocalDate localDate = LocalDate.parse(list2.get(i), DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDate date = LocalDate.now();
            assertEquals(localDate.isAfter(date),"Active".equals(status.get(i)));
        }
    }
}
