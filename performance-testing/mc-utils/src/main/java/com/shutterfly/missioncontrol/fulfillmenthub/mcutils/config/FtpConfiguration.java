package com.shutterfly.missioncontrol.fulfillmenthub.mcutils.config;

import com.jcraft.jsch.ChannelSftp.LsEntry;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
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

  @Value("${mc.local.download.dir}")
  private String localDownloadFolder;

  @Value("${mc.remote.file.event.listen.regex.filter}")
  private String regexFilter;

  @Value("${mc.remote.file.event.listen.dir}")
  private String remoteListenDir;

  @Value("${mc.remote.file.event.listen.delay}")
  private long delay;

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

  @Bean
  public IntegrationFlow sftpInboundFlow() {
    return IntegrationFlows
        .from(s -> s.sftp(defaultSftpSessionFactory())
                .preserveTimestamp(true)
                .remoteDirectory(remoteListenDir)
                .regexFilter(regexFilter)
                .localDirectory(new File(localDownloadFolder)),
            e -> e.id("sftpInboundAdapter")
                .autoStartup(true)
                .poller(Pollers.fixedDelay(delay)))
        .handle(m -> log
            .info("Downloaded file. Headers: {}", m.getHeaders().toString()))
        .get();
  }

  @MessagingGateway
  public interface SftpGateway {

    @Gateway(requestChannel = "toSftpChannel")
    void send(byte[] data, @Header(name = FileHeaders.FILENAME) String filePath);

  }

}