package com.shutterfly.missioncontrol.common;

import static org.testng.Assert.assertNotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.shutterfly.missioncontrol.config.ConfigLoader;

import io.restassured.path.xml.XmlPath;

public class EcgFileSafeUtil extends ConfigLoader {
	private static final Logger logger = LoggerFactory.getLogger(EcgFileSafeUtil.class);

	public static String buildInboundFilePath(String payload) {

		/*
		 * Get Source Id Build Inbound ECG file location
		 */

		String sourceEcgPath = "";
		XmlPath xmlPath = new XmlPath(payload);

		final String requestType = xmlPath.get("**.findAll { it.name() == 'requestType' }");
		final String requestCategory = xmlPath.get("**.findAll { it.name() == 'requestCategory' }");
		final String direction = "INBOUND";
		String sourceParticipantId = "";
		final String requestorParticipantId = xmlPath.get("**.findAll { it.name() == 'sourceID' }");
		if (requestType.equals("Process")||requestType.equals("ReDelivery")) {
			sourceParticipantId = xmlPath.get("**.findAll { it.name() == 'sourceID' }");
		} else {
			sourceParticipantId = xmlPath.get("**.findAll { it.name() == 'destinationID' }");
		}
		final String targetParticipantId = "MC";
		final String materialType = xmlPath.get("**.findAll { it.name() == 'fulfillmentType' }");

		FileTransferDetailsUtil fileTransfer = new FileTransferDetailsUtil();
		sourceEcgPath = fileTransfer.getFileTransferPathForProcessRequest(requestType, requestCategory, direction,
				requestorParticipantId, sourceParticipantId, targetParticipantId, materialType);
		assertNotNull(sourceEcgPath);
		return sourceEcgPath;

	}

	public static String buildTargetFilePath(String payload) {
		/*
		 * Get DestinationId Build Destination ECG file location
		 */
		XmlPath xmlPath = new XmlPath(payload);
		String destinationId = xmlPath.get("**.findAll { it.name() == 'destinationID' }");

		assertNotNull(destinationId);

		return "/MissionControl/" + destinationId + "/";

	}

	public static void putFileAtSourceLocation(String sourceEcgPath, String record, String externalFilename) {

		JSch jsch = new JSch();
		Session session = null;
		try {
			session = jsch.getSession("auto-mc", "tmvitdmz01-lv.dmz.lab.shutterfly.com", 22);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setPassword("q19zo1W9");
			session.connect();

			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftpChannel = (ChannelSftp) channel;
			/*
			 * normalize folder path with regex expression
			 */
			sftpChannel.put(("src/test/resources/XMLPayload/BulkFiles/" + externalFilename),
					(sourceEcgPath + "/" + record + ".xml").replaceAll("/+", "/"));
			sftpChannel.exit();

		} catch (JSchException | SftpException e) {
			logger.error("Error stack trace while building source file path : ", e);
		} finally {

			if (session != null && session.isConnected()) {
				session.disconnect();
			}
		}
	}

}
