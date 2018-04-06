package com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.config;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.sftp.Sftp;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.util.Base64Utils;

@Configuration
@Slf4j
public class FtpConfiguration {

  @Value("${mc.ftp.username}")
  private String username;

  @Value("${mc.ftp.password}")
  private String password;

  @Value("${mc.ftp.host}")
  private String host;

  @Value("${mc.ftp.port}")
  private int port;

  public SessionFactory<LsEntry> defaultSftpSessionFactory() {
    String user = new String(Base64Utils.decode(username.getBytes()));
    String password = new String((Base64Utils.decode(this.password.getBytes())));
    DefaultSftpSessionFactory defaultSftpSessionFactory = new DefaultSftpSessionFactory();
    defaultSftpSessionFactory.setHost(host);
    defaultSftpSessionFactory.setPort(port);
    defaultSftpSessionFactory.setAllowUnknownKeys(true);
    defaultSftpSessionFactory.setUser(user);
    defaultSftpSessionFactory.setPassword(password);
    return defaultSftpSessionFactory;
  }

  @Bean
  public IntegrationFlow sftpOutboundFlow() {
    return IntegrationFlows.from("toSftpChannel")
        .handle(Sftp.outboundAdapter(defaultSftpSessionFactory(), FileExistsMode.REPLACE)
            .useTemporaryFileName(true)
            .autoCreateDirectory(true)
            .remoteDirectory("/")
            .fileNameExpression("headers['" + FileHeaders.FILENAME + "']")).get();
  }

  @MessagingGateway
  public interface SftpGateway {

    @Gateway(requestChannel = "toSftpChannel")
    void send(byte[] data, @Header(name = FileHeaders.FILENAME) String filePath);

  }

}