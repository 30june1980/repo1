package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.config;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;

/**
 * Created by Shweta on 05-10-2017.
 */
@Service
@RefreshScope
@Slf4j
public class SFTPService {

  @Autowired
  SFTPHelper sftpHelper;

  @Value("${mc.sftp.ecg.timeout.ms}")
  int ecgTimeout;

  @Value("${mc.ftp.username}")
  private String username;

  @Value("${mc.ftp.password}")
  private String password;

  @Value("${mc.ftp.host}")
  private String host;

  @Value("${mc.sftp.port}")
  private int port;

  public void downloadFile(String remoteFilePathAndName, String localFilePathAndName) {
    String sftpUri = getSftpUri(remoteFilePathAndName);

    if (Objects.nonNull(sftpUri)) {
      FileObject localFileObject = null;
      FileObject remoteFileHandleObject = null;

      try {
        File localFile = new File(localFilePathAndName);
        localFileObject = sftpHelper.resolveFile(localFile.getAbsolutePath());

        remoteFileHandleObject = sftpHelper.resolveFile(sftpUri, getFileSystemOptions());

        // Download file from server
        log.info("Downloading file from server: {}", localFile.getName());
        localFileObject.copyFrom(remoteFileHandleObject, Selectors.SELECT_SELF);
        log.info("File download successful");

      } catch (Exception e) {
        log.error("Exception occurred",e);
      } finally {
        closeResourcesQuietly(localFileObject, remoteFileHandleObject);
      }
    }
  }

  public void uploadFile(String localFilePathAndName, String remoteFilePathAndName) {
    String sftpUri = getSftpUri(remoteFilePathAndName);

    if (Objects.nonNull(sftpUri)) {
      FileObject localFileObject = null;
      FileObject remoteFileHandleObject = null;

      try {
        File localFile = new File(localFilePathAndName);
        localFileObject = sftpHelper.resolveFile(localFile.getAbsolutePath());

        remoteFileHandleObject = sftpHelper.resolveFile(sftpUri, getFileSystemOptions());

        // Upload file to server
        log.info("Uploading file to server: {}", remoteFileHandleObject.getName());
        remoteFileHandleObject.copyFrom(localFileObject, Selectors.SELECT_SELF);
        log.info("File upload successful");

      } catch (FileSystemException e) {
        log.error("FileSystemException occurred",e);
      } finally {
        closeResourcesQuietly(localFileObject, remoteFileHandleObject);
      }
    }
  }

  private FileSystemOptions getFileSystemOptions() throws FileSystemException {
    FileSystemOptions opts;

    opts = new FileSystemOptions();
    SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
    SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);
    SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, ecgTimeout);

    return opts;
  }

  private void closeResourcesQuietly(FileObject... fileObjects) {
    Stream.of(fileObjects).forEach(this::closeFileObjectQuietly);
  }

  private void closeFileObjectQuietly(FileObject fileObject) {
    if (Objects.nonNull(fileObject)) {
      try {
        fileObject.close();
        sftpHelper.close(fileObject);
      } catch (Exception e) {
        log.error("Exception while closing FileObject ", e);
      }
    }
  }

  private String getSftpUri(String remoteFilePathAndName) {
    URI uri=null;
    String uriStr = null;

    String user = new String(Base64Utils.decode(username.getBytes()));
    String password = new String((Base64Utils.decode(this.password.getBytes())));
    String userInfo = user + ":" + password;

    try {
      uri = new URI("sftp", userInfo, host, port, remoteFilePathAndName,
          null, null);
    } catch (URISyntaxException e) {
      log.error("UriSyntaxException caught",e);
    }

    if (Objects.nonNull(uri)) {
      uriStr = uri.toString();
    }
    return uriStr;
  }

}
