package com.shutterfly.missioncontrolservices.redelivery;

import com.google.common.io.Resources;
import com.shutterfly.missioncontrolservices.accesstoken.AccessToken;
import com.shutterfly.missioncontrolservices.config.ConfigLoader;
import com.shutterfly.missioncontrolportal.Utils.XmlUtils;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

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
        Map<String, String> postMap = XmlUtils.readXml(Resources.getResource("XMLPayload/Redelivery/PostTransactionalExternalPrintReady.xml").getPath());
        Map<String, String> processMap = XmlUtils.readXml(Resources.getResource("XMLPayload/Redelivery/TransactionalExternalPrintReady.xml").getPath());

        URL file = Resources.getResource("JSONPayload/FindFulfillmentHistoryRedelivery.json");
        String payload = Resources.toString(file, StandardCharsets.UTF_8);
        JSONObject redeliveryObject = new JSONObject(payload);
        redeliveryObject.put("fulfillmentType", postMap.get("sch:fulfillmentType"));
        redeliveryObject.put("requestID", "");
        redeliveryObject.put("searchSourceID", postMap.get("sch:sourceID"));
        redeliveryObject.put("requestorRefNo", postMap.get("sch:requestorRefNo"));
        redeliveryObject.put("supplierRefNo", postMap.get("sch:supplierRefNo"));
        redeliveryObject.put("recipientID", postMap.get("sch:recipientId"));
        redeliveryObject.put("eventDispatchedDate", formatDate(postMap.get("sch:dispatchedDate"), 1));
        redeliveryObject.put("deliveryMethodCd", postMap.get("sch:deliveryMethodCd"));
        redeliveryObject.put("idQualifier", processMap.get("ns2:idQualifier"));
        redeliveryObject.put("recipientType", processMap.get("ns2:recipientType"));
        redeliveryObject.put("rangeStartEventReceivedDate", formatDate(postMap.get("sch:receivedDate"), -20));
        redeliveryObject.put("rangeEndEventReceivedDate", formatDate(postMap.get("sch:receivedDate"), 1));
        return redeliveryObject.toString();
    }

    private String formatDate(@Nonnull String utcDate, int offset) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.parse(utcDate), ZoneOffset.UTC);
        LocalDate localDate;
        localDate = localDateTime.toLocalDate().plusDays(offset);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return localDate.format(formatter);
    }

    @Test()
    public void redeliveryTest() throws Exception {
        String payload = buildPayload();
        Response response = RestAssured.given().header("saml", config.getProperty("SamlValue")).log()
                .all().contentType("application/json").accept("application/json")
                .body(payload).when().post(this.getProperties());
        assertEquals(response.getStatusCode(), 200, "Assertion for Response code!");
    }

}
