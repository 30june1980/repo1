/**
 * 
 */
package com.shutterfly.missioncontrol.common;

import com.shutterfly.missioncontrol.accesstoken.AccessToken;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.testng.Assert.assertNotNull;

/**
 * @author Diptman Gupta
 *
 */
public class FileTransferDetailsUtil extends ConfigLoader {

	public String getFileTransferPathForProcessRequest(String requestType, String requestCategory, String direction,
			String requestorParticipantId, String sourceParticipantId, String targetParticipantId,
			String materialType) {
		AccessToken accessToken = new AccessToken();
		final String uri = "http://tsbsapp32-lv.internal.shutterfly.com:8085";
		Response response = given().contentType("application/json")
				.header("Authorization", accessToken.getAccessToken()).param("requestType", requestType)
				.param("requestCategory", requestCategory).param("direction", direction)
				.param("requestorParticipantId", requestorParticipantId)
				.param("sourceParticipantId", sourceParticipantId).param("targetParticipantId", targetParticipantId)
				.param("materialType", materialType).when().get(uri + "/api/services/v1/filetransferdetails/active")
				.then().extract().response();
		String responsebody = response.asString();
		JsonPath jsonPath = new JsonPath(responsebody);
		String fileTransferPathForProcessRequest = jsonPath.getString("fileTransferDetailsDto.sourceFilePath");
		assertNotNull(fileTransferPathForProcessRequest);
		return fileTransferPathForProcessRequest;

	}
}