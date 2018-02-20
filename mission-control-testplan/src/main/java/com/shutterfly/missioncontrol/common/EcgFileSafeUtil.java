package com.shutterfly.missioncontrol.common;

import com.jcraft.jsch.*;
import com.shutterfly.missioncontrol.config.ConfigLoader;
import com.shutterfly.missioncontrol.util.Encryption;
import io.restassured.path.xml.XmlPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import static org.testng.Assert.assertNotNull;

public class EcgFileSafeUtil extends ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(EcgFileSafeUtil.class);

    private static final String LOCAL_PATH = "src/test/resources/XMLPayload/BulkFiles/";

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
        if (requestType.equals("Process") || requestType.equals("ReDelivery")) {
            sourceParticipantId = xmlPath.get("**.findAll { it.name() == 'sourceID' }");
        } else {
            sourceParticipantId = xmlPath.get("**.findAll { it.name() == 'destinationID' }");
        }
        final String targetParticipantId = "MC";
        final String materialType = xmlPath.get("**.findAll { it.name() == 'fulfillmentType' }");

        FileTransferDetailsUtil fileTransfer = new FileTransferDetailsUtil();
        sourceEcgPath = fileTransfer
                .getFileTransferPathForProcessRequest(requestType, requestCategory, direction,
                        requestorParticipantId, sourceParticipantId, targetParticipantId, materialType);
        assertNotNull(sourceEcgPath);
        return sourceEcgPath;

    }

    public static void putFileAtSourceLocation(String sourceEcgPath, String record,
                                               String externalFilename) {

        JSch jsch = new JSch();
        Session session = null;
        try {
            session = jsch.getSession("dgupta", "dsbsapp14-lv.internal.shutterfly.com", 22);
            session.setConfig("StrictHostKeyChecking", "no");
            SecretKey secretKey = Encryption.keyGenerator();
            try {
                session.setPassword(Encryption.decrypt(
                        "6QkeUtamWANCjylsQiKZEw==", secretKey
                ));
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;
            /*
             * normalize folder path with regex expression
             */
            sftpChannel.put((LOCAL_PATH + externalFilename),
                    ("/" + sourceEcgPath + "/" + record + ".xml").replaceAll("/+", "/"));
            sftpChannel.exit();

        } catch (JSchException | SftpException e) {
            logger.error("Error stack trace while building source file path : ", e);
        } finally {

            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    public static void updateAndPutFileAtSourceLocation(String sourceEcgPath, String requestId,
                                                        String externalFilename, String fileNameSuffix) {

        JSch jsch = new JSch();
        Session session = null;
        File tempFile = null;
        String destinationFileName = requestId + fileNameSuffix;

        try {

            session = jsch.getSession("dgupta", "dsbsapp14-lv.internal.shutterfly.com", 22);
            session.setConfig("StrictHostKeyChecking", "no");
            SecretKey secretKey = Encryption.keyGenerator();
            try {
                session.setPassword(Encryption.decrypt(
                        "6QkeUtamWANCjylsQiKZEw==", secretKey
                ));
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }

            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            ChannelSftp sftpChannel = (ChannelSftp) channel;

            Path sourcePath = Paths.get(LOCAL_PATH + externalFilename);
            tempFile = new File(destinationFileName);
            Path tempFilePath = Files.copy(sourcePath, tempFile.toPath());

            //edit file content
            Charset charset = StandardCharsets.UTF_8;
            String content = new String(Files.readAllBytes(tempFilePath), charset);
            content = content.replaceAll("VALID_REQUEST_101", requestId);
            Files.write(tempFilePath, content.getBytes(charset));

            sftpChannel.put(tempFilePath.toString(),
                    ("/" + sourceEcgPath + "/" + destinationFileName + ".xml").replaceAll("/+", "/"));
            sftpChannel.exit();

        } catch (JSchException | SftpException | IOException e) {
            logger.error("Error stack trace while building source file path : ", e);
        } finally {
            tempFile.delete();
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

}