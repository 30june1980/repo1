/**
 *
 */
package com.shutterfly.missioncontrolservices.common;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertNotNull;

import com.shutterfly.missioncontrolservices.accesstoken.AccessToken;
import com.shutterfly.missioncontrolservices.config.ConfigLoader;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

/**
 * @author Diptman Gupta
 *
 */
public class FileTransferDetailsUtil extends ConfigLoader {

	public String getFileTransferPathForProcessRequest(String requestType, String requestCategory,
													   String direction,
													   String requestorParticipantId, String sourceParticipantId, String targetParticipantId,
													   String materialType) {
		AccessToken accessToken = new AccessToken();
		final String uri = config.getProperty("BaseApiUrl");
		Response response = given().contentType("application/json")
				.header("Authorization", accessToken.getAccessToken()).param("requestType", requestType)
				.param("requestCategory", requestCategory).param("direction", direction)
				.param("requestorParticipantId", requestorParticipantId)
				.param("sourceParticipantId", sourceParticipantId)
				.param("targetParticipantId", targetParticipantId)
				.param("materialType", materialType).when()
				.get(uri + "/api/services/v1/filetransferdetails/active")
				.then().extract().response();
		String responsebody = response.asString();
		JsonPath jsonPath = new JsonPath(responsebody);
		String fileTransferPathForProcessRequest = jsonPath
				.getString("fileTransferDetailsDto.sourceFilePath");
		assertNotNull(fileTransferPathForProcessRequest);
		return fileTransferPathForProcessRequest;

	}
}