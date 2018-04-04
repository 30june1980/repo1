package com.shutterfly.missioncontrol.listener;

import com.shutterfly.missioncontrol.config.ConfigLoaderWeb;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GetScreenshot extends ConfigLoaderWeb {

    public static String captureFailed(String testClassName, String screenshotName) throws IOException {
        File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = LocalDate.now().format(dateTimeFormatter);
        String destination = buildScreenshotName(formattedDate, testClassName, screenshotName);
        FileUtils.copyFile(source, new File(destination));
        return destination;
    }

    public static String capturePassed(String testClassName, String screenshotName) throws IOException {
        File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = LocalDate.now().format(dateTimeFormatter);
        String destination = buildScreenshotName(formattedDate, testClassName, screenshotName);
        FileUtils.copyFile(source, new File(destination));
        return destination;
    }

    private static String buildScreenshotName(String formattedDate, String testClassName, String screenshotName) {
        return new StringBuilder().append(System.getProperty("user.dir")).append(File.separator)
                .append("test-output").append(File.separator)
                .append("Screenshot").append(File.separator)
                .append(formattedDate).append(File.separator)
                .append("FailedScreenshot").append(File.separator)
                .append(testClassName).append(File.separator)
                .append(screenshotName).append(".png").toString();
    }
}
