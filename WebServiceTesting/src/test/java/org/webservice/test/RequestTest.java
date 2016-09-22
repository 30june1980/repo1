package org.webservice.test;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.response.Response;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.jayway.restassured.path.xml.XmlPath.with;

/**
 * Created by kedar on 19-09-2016.
 */
public class RequestTest {
    // http://services.aonaware.com/DictService/DictService.asmx?op=Define
    // http://services.aonaware.com/DictService/DictService.asmx?wsdl
    @Test(priority = 2)
    public void getSOAPResponseFromDictionary() {
        RestAssured.baseURI = "http://services.aonaware.com";
        RestAssured.port = 80;
        String word = "ink";

        String myEnvelope = "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">" +
                "<soap12:Body>" +
                "<Define xmlns=\"http://services.aonaware.com/webservices/\">" +
                "<word>" + word + "</word>" +
                "</Define>" +
                "</soap12:Body>" +
                "</soap12:Envelope>";

        Map<String, String> authhdrs = new HashMap<String, String>();
        authhdrs.put("SOAPAction", "Define");
        Response extractableResponse1;
        extractableResponse1 = RestAssured.given().request().headers(authhdrs)
                .contentType("application/soap+xml; charset=UTF-8;").body(myEnvelope)
                .when().post("/DictService/DictService.asmx").then().extract().response();

        try {
            String response1 = String.valueOf(extractableResponse1.getStatusCode());
            Reporter.log("Actual service response is " + response1);
            // 1. Assert Response code SOAP response
            Assert.assertEquals(response1, "200");
        } catch (Exception e) {
            System.out.println(e);
        }
        try {
            String xml = extractableResponse1.andReturn().asString();
            String prettyXML = with(xml).prettyPrint();
            //2. Assert Content of the response has expected value
            Assert.assertTrue(prettyXML.contains("THE DEVIL'S DICTIONARY"));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // http://services.aonaware.com/CountCheatService/CountCheatService.asmx
    // http://services.aonaware.com/CountCheatService/CountCheatService.asmx?WSDL
    @Test(priority = 1)
    public void getSOAPResponseFromCountCheatService() {
        RestAssured.baseURI = "http://services.aonaware.com";
        RestAssured.port = 80;
        String word = "link";

        String myEnvelope = "<soap12:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap12=\"http://www.w3.org/2003/05/soap-envelope\">" +
                "<soap12:Body>" +
                "<LetterSolutions xmlns=\"http://services.aonaware.com/webservices/\">" +
                "<anagram>" + word + "</anagram>" +
                "</LetterSolutions>" +
                "</soap12:Body>" +
                "</soap12:Envelope>";

        Map<String, String> authdrs = new HashMap<String, String>();
        authdrs.put("SOAPAction", "LetterSolutions");
        Response extractableResponse2;
        extractableResponse2 = RestAssured.given().request().headers(authdrs)
                .contentType("application/soap+xml; charset=UTF-8;").body(myEnvelope)
                .when().post("CountCheatService/CountCheatService.asmx").then().extract().response();

        try {
            String response2 = String.valueOf(extractableResponse2.getStatusCode());
            Reporter.log("Actual service Response is " + response2);
            //1. Assert Response Code for SOAP response
            Assert.assertEquals(response2, "200");
        } catch (Exception e) {
            System.out.println(e);
        }
        try {
            String xml = extractableResponse2.andReturn().asString();
            String prettyXML = with(xml).prettyPrint();
            //2. Assert Response Content has expected values
            Assert.assertTrue(prettyXML.contains("kiln"));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
