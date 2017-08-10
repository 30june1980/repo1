/**
 * 
 */
package com.shutterfly.missioncontrol.archivecode;

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

public class ProcessFulfillmentRequestBulkTest extends BuildAndValidateSoapRequest {

	@Test
	@Parameters({ "BS", "MS", "FulfillmentType" })

	public void validateSoapResponseTest(String BS, String MS, String FulfillmentType ) throws Exception {

		basicConfigNonWeb();
		String strEndpoint = config.getProperty("Endpoint");
		Map<String, String> updateMap = new HashMap<>();
		long millis = System.currentTimeMillis();
		String requestId = "Test_qa_automation_" + millis;
		CsvReaderWriter wr = new CsvReaderWriter();
		wr.writeToCsv("BDO_SOAP",requestId);
		updateMap.put("sch:requestID", requestId);
		updateMap.put("sch:businessSegmentID", BS);
		updateMap.put("sch:marketSegmentCd", MS);
		updateMap.put("sch:fulfillmentType", FulfillmentType);
		Map<String, String> validateMap = new HashMap<>();
		validateMap.put("ns2:transactionStatus", "Accepted");
		validateMap.put("ns2:ackReportingLevel", "Transaction");

		validateSoapResponse(getSOAPResponse(readXml(config.getProperty("SoapProcessFulfillmentRequestBulkXml"), updateMap),
				strEndpoint), validateMap);
	}

}
