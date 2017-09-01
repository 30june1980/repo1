package com.shutterfly.missioncontrol.common;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.shutterfly.missioncontrol.config.ConfigLoader;

import io.restassured.path.xml.XmlPath;

public class EcgFileSafeUtil extends ConfigLoader {

	public static String buildSourceFilePath(String payload) throws IOException {

		/*
		 * Get Source Id Build source ECG file location
		 */
		XmlPath xmlPath = new XmlPath(payload);
		String sourceId = xmlPath.get("**.findAll { it.name() == 'sourceID' }");
		assertNotNull(sourceId);
		String sourceEcgPath = "/MissionControl/" + sourceId + "/";
		return sourceEcgPath;

	}

	public static String buildTargetFilePath(String payload) throws IOException {
		/*
		 * Get DestinationId Build Destination ECG file location
		 */
		XmlPath xmlPath = new XmlPath(payload);
		String destinationId = xmlPath.get("**.findAll { it.name() == 'destinationID' }");
		assertNotNull(destinationId);
		String targetEcgPath = "/MissionControl/" + destinationId + "/";
		return targetEcgPath;

	}

	public static void putFileAtSourceLocation(String sourceEcgPath, String destinationEcgPath, String record,
			String externalFilename) {

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
			sftpChannel.put(("src/test/resources/XMLPayload/BulkFiles/" + externalFilename),
					(sourceEcgPath + record + ".xml"));
			/*
			 * sftpChannel.get("/MissionControl/ACET/bulkfile_all_valid.xml",
			 * "localfile.txt");
			 */
			sftpChannel.exit();

		} catch (JSchException e) {
			e.printStackTrace();
		} catch (SftpException e) {
			e.printStackTrace();
		} finally {
			session.disconnect();
		}
	}

}
