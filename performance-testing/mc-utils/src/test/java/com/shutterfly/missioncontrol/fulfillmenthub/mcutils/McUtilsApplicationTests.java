package com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils;

import com.shutterfly.missioncontrolservices.fulfillmenthub.mcutils.config.FtpConfiguration.SftpGateway;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class McUtilsApplicationTests {

	@Autowired
	private SftpGateway sftpGateway;

	@Test
	public void contextLoads() throws InterruptedException {
		Thread.sleep(50000);
	}

}
