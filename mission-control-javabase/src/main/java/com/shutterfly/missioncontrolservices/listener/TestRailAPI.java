package com.shutterfly.missioncontrolservices.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shutterfly.missioncontrolservices.config.ConfigLoaderWeb;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.testng.Reporter;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

public class TestRailAPI extends ConfigLoaderWeb {
    private static RequestSpecification testRailSpec;
    private static ResponseSpecification responseSpec;

    private static void buildResponse() {
        String authCookie = (config.getProperty("TestrailUserName") + ":" + config.getProperty("TestrailApiKey"));
        String authCookieEncoded = new String(Base64.encodeBase64(authCookie.getBytes()));
        testRailSpec = new RequestSpecBuilder().setBaseUri(config.getProperty("TestrailUrl")).addHeader("Authorization", "Basic " + authCookieEncoded).build();
        ResponseSpecBuilder resBuilder = new ResponseSpecBuilder();
        resBuilder.expectStatusCode(200);
        responseSpec = resBuilder.build();
    }

    @SuppressWarnings("unchecked")
    public static void testRailAddResultAPI(int statusId, String comment, String runId, String caseId) {
        try {
            buildResponse();

            JSONObject json = new JSONObject();
            json.put("status_id", statusId);
            json.put("comment", comment);
            given()
                    .spec(testRailSpec)
                    .contentType("application/json")
                    .request()
                    .body(json)
                    .pathParam("runId", runId)
                    .pathParam("caseId", caseId)
                    .when()
                    .post("/index.php?/api/v2/add_result_for_case/{runId}/{caseId}")
                    .then()
                    .log()
                    .ifError()
                    .spec(responseSpec)
            ;
        } catch (AssertionError | Exception exception) {
            Reporter.log("Issue with Test rail Integration , PFB for error message : " + exception.getMessage());
            System.out.println("Issue with Test rail Integration. Please check the RunId and CaseId , Please find above for error message : " + exception.getMessage());
        }
    }

    private static String requestBuilder(List<String> caseIdList, int statusId, String comment) throws JsonProcessingException {
        String jsonString;
        List<Result> resultList = new ArrayList<>();
        for (String temp : caseIdList) {
            int caseid = (int) Double.parseDouble(temp);
            resultList.add(new Result(caseid, statusId, comment));
        }
        TestRailPOJO testRailPOJO = new TestRailPOJO(resultList);

        ObjectMapper mapper = new ObjectMapper();
        jsonString = mapper.writeValueAsString(testRailPOJO);
        return jsonString;
    }

    @SuppressWarnings("unchecked")
    public static void testRailMultipleCaseAPI(int statusid, String comment, String runId, List<String> caseId) {
        try {
            buildResponse();
            String json = requestBuilder(caseId, statusid, comment);
            given()
                    .spec(testRailSpec)
                    .contentType("application/json")
                    .request()
                    .body(json)
                    .pathParam("runId", runId)
                    .when()
                    .post("/index.php?/api/v2/add_results_for_cases/{runId}")
                    .then()
                    .log()
                    .ifError()
                    .spec(responseSpec)
            ;
        } catch (AssertionError | Exception exception) {
            Reporter.log("Issue with Test rail Integration , PFB for error message : " + exception.getMessage());
            System.out.println("Issue with Test rail Integration. Please check the RunId and CaseId , Please find above for error message : " + exception.getMessage());
        }
    }
}