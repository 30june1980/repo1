package com.shutterfly.missioncontrol.fulfillmenthub.test;

import com.jayway.restassured.response.Response;
import com.shutterfly.missioncontrol.fulfillmenthub.util.FileDataProvider;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import static com.jayway.restassured.RestAssured.given;

/**
 * Created by kedar on 23-09-2016.
 */
public class RESTRequestTest {
    private FileDataProvider filedp = new FileDataProvider();

    //Response from a REST service to a GET request
    Response extractableresponse = given()
                                   .contentType("application/json")
                                   .when()
                                   .get(filedp.getProperty("RESTURL")+filedp.getProperty("RESTZIP"))
                                   .then()
                                   .extract().response();
    @Test(priority = 2)
    //Asserting for Response code
    public void checkGetReqResponseCode() {
        try {
            String rescode = String.valueOf(extractableresponse.getStatusCode());
            Reporter.log("Actual service Response is " + rescode);
            Assert.assertEquals(rescode, "200");
        }catch(Exception e){
            System.out.println(e);
        }
    }
    @Test(priority = 3)
    //Asserting for Content
    public void checkGetReqResponseContent(){
        try {
            String content = extractableresponse.asString();
            Reporter.log(content);
            String city = filedp.getProperty("RESTvalidatecity");
            Assert.assertTrue(content.contains(city));
        }catch(Exception e){
            System.out.println(e);
        }
    }
    @Test(priority = 1)
    /*Test data provider*/
    public void checkDataProviderSendsCorrectData(){
        String u1 = "RESTURL";
        String u2 = "RESTZIP";
        String u3 = "RESTvalidatequery";
        String query = filedp.getProperty(u1)+filedp.getProperty(u2);
        Assert.assertEquals(query,filedp.getProperty(u3));
        Reporter.log(query);
    }



}
