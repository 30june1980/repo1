/**
 *
 */
package com.shutterfly.missioncontrol.config;

import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Diptman Gupta
 */
public class ConfigLoader {

    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

    protected static Properties config = null;
    private static Properties properties = null;
    protected static WebDriver driver = null;

    public static void basicConfigNonWeb() {

        config = new Properties();
        try {
            config.load(ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            logger.error("Unable to load config property file: " + e);
        }

        properties = new Properties();
        try {
            properties.load(ConfigLoader.class.getClassLoader().getResourceAsStream("elements.properties"));
        } catch (IOException e) {
            logger.error("Unable to load element property file: " + e);
        }

    }
}