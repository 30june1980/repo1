package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.config;

import java.io.File;
import lombok.Getter;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.ftp.Ftp;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.ftp.session.DefaultFtpsSessionFactory;
import org.springframework.util.Base64Utils;

@Configuration
public class FtpConfiguration {

  @Value("${mc.ftp.remote.dir}")
  private String remoteDirectory;

  @Value("${mc.ftp.username}")
  private String username;

  @Value("${mc.ftp.password}")
  private String password;

  @Value("${mc.ftp.host}")
  private String host;

  @Value("${mc.ftp.port}")
  private int port;

  @Value("${mc.local.file.to.upload}")
  @Getter
  private String localFilePathToUpload;

  @Bean
  public SessionFactory<FTPFile> ftpSessionFactory() {
    DefaultFtpsSessionFactory sf = new DefaultFtpsSessionFactory();
    sf.setHost(host);
    sf.setPort(port);
    sf.setUsername(new String(Base64Utils.decode(username.getBytes())));
    sf.setPassword(new String((Base64Utils.decode(password.getBytes()))));
    return new CachingSessionFactory<>(sf);
  }

  @Bean
  public IntegrationFlow ftpOutboundFlow() {
    return IntegrationFlows.from("toMcFtpChannel")
        .handle(Ftp.outboundAdapter(ftpSessionFactory(), FileExistsMode.REPLACE)
            .useTemporaryFileName(true)
            .autoCreateDirectory(true)
            .fileNameExpression("headers['" + FileHeaders.FILENAME + "']")
            .remoteDirectory(remoteDirectory)).get();
  }

  @MessagingGateway
  public interface FtpGateway {

    @Gateway(requestChannel = "toMcFtpChannel")
    void sendToFtp(File file);
  }



}