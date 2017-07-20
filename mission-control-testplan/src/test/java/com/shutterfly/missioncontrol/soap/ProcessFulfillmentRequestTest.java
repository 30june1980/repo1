/**
 * 
 */
package com.shutterfly.missioncontrol.soap;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.shutterfly.missioncontrol.common.BuildAndValidateSoapRequest;
import com.shutterfly.missioncontrol.config.CsvReaderWriter;

/**
 * @author Diptman Gupta
 *
 */

public class ProcessFulfillmentRequestTest extends BuildAndValidateSoapRequest {

	@Test
	@Parameters({ "BS", "MS", "EOB" })

	public void validateSoapResponseTest(String BS, String MS, String EOB) throws Exception {

		basicConfigNonWeb();
		String strEndpoint = config.getProperty("Endpoint");
		Map<String, String> updateMap = new HashMap<>();
		long millis = System.currentTimeMillis();
		String requestId = "Test_qa_automation_" + millis;
		CsvReaderWriter wr = new CsvReaderWriter();
		wr.writeToCsv("TIR_SOAP",requestId);
		updateMap.put("sch:requestID", requestId);
		updateMap.put("sch:businessSegmentID", BS);
		updateMap.put("sch:marketSegmentCd", MS);
		updateMap.put("sch:fulfillmentType", EOB);
		Map<String, String> validateMap = new HashMap<>();
		validateMap.put("ns2:ackReportingLevel", "Transaction");
		validateSoapResponse(getSOAPResponse(readXml(config.getProperty("SoapProcessFulfillmentRequestXml"), updateMap),
				strEndpoint), validateMap);

	}

}
