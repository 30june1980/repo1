package com.shutterfly.missioncontrol.redelivery;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.testng.Assert.assertEquals;

public class RedeliveryTransactionalExternalPrintReady extends ConfigLoader {

    private AccessToken accessToken;
    private String uri = "";

    @BeforeClass
    public void setup() {
        accessToken = new AccessToken();
        String token = accessToken.getAccessToken();
    }

    private String getProperties() {
        basicConfigNonWeb();
        uri = config.getProperty("BaseUrl") + config.getProperty("UrlExtensionFindFulfillmentHistory");
        return uri;
    }

    private String buildPayload() throws IOException {
        URL file = Resources.getResource("JSONPayload/FindFulfillmentHistoryRedelivery.json");
        return Resources.toString(file, StandardCharsets.UTF_8);
    }

    @Test(dependsOnGroups = {"Process_EUTEPR_DB", "Post_EUTEPR_DB"})
    public void redeliveryTest() throws IOException {
        String payload = buildPayload();
        Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
                .all().contentType("application/json").accept("application/json")
                .body(payload).when().post(this.getProperties());
        assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    }

}
