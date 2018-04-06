package com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.config;

import java.util.Objects;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

/**
 * Created by Shweta on 05-10-2017.
 */
@Component
@Scope(scopeName = "prototype", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
public class SFTPHelper {

  private StandardFileSystemManager mgr;

  @PostConstruct
  public void init() {
    this.mgr = new StandardFileSystemManager();
    try {
      mgr.init();
    } catch (FileSystemException e) {
      log.error("Could not initialize FileSystemManager.", e);
    }
  }

  public void close(FileObject fileObject) {
    log.info("Closing file system manager");
    if (Objects.nonNull(fileObject) && Objects.nonNull(fileObject.getFileSystem())) {
      mgr.closeFileSystem(fileObject.getFileSystem());
    }
    mgr.close();
  }

  public FileObject resolveFile(String uri) throws FileSystemException {
    return this.mgr.resolveFile(uri);
  }

  public FileObject resolveFile(String uri, FileSystemOptions fileSystemOptions)
      throws FileSystemException {
    return this.mgr.resolveFile(uri, fileSystemOptions);
  }
}