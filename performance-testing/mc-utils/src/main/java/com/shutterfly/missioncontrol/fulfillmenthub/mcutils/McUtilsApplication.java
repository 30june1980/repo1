package com.shutterfly.missioncontrol.fulfillmenthub.mcutils;

import com.shutterfly.missioncontrol.fulfillmenthub.mcutils.config.FtpConfiguration.FtpGateway;
import java.io.File;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class McUtilsApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context =
				new SpringApplicationBuilder(McUtilsApplication.class)
						.run(args);
		FtpGateway gateway = context.getBean(FtpGateway.class);
		gateway.sendToFtp(new File("/home/nitin/jmeter.log"));
	}
}
