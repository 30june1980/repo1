package com.shutterfly.missioncontrol.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class ListenerTest implements ITestListener {

    private static final Logger logger = LoggerFactory.getLogger(ListenerTest.class);

    private String caseId;
    private String runId;
    private String className;
    private String simpleClassName;
    private String passedTestScreenshotPath;
    private String failedTestScreenshotPath;
    private String formattedDate;

    public void onTestStart(ITestResult result) {
        className = result.getMethod().getTestClass().getName();
        simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");
        ZonedDateTime localDate = ZonedDateTime.now();
        formattedDate = localDate.format(dtf);
    }

    public void onTestSuccess(ITestResult result) {
        System.out.println("class Name - " + simpleClassName + " and Method Name -" + result.getName() + " has been executed and passed");
        String tc = formattedDate + "_" + simpleClassName + "_" + result.getName();
        if (result.getTestContext().getCurrentXmlTest().getParameter("Screenshot").equalsIgnoreCase("Yes")) {
            try {
                passedTestScreenshotPath = GetScreenshot.capturePassed(simpleClassName, tc);
            } catch (IOException exception) {
                logger.error("Failed to capture screenshot", exception);
            }
            passedTestScreenshotPath = passedTestScreenshotPath.replace("/opt/shutterfly/jenkins/workspace/testjobs/Mission Control tests/sbs-mc-fhub-test/",
                    "http://sbscon01-lv.internal.shutterfly.com:8080/job/testjobs/job/Mission%20Control%20tests/job/sbs-mc-fhub-test/ws/");
            Reporter.log("<a href=" + passedTestScreenshotPath + "><img src=" + passedTestScreenshotPath + " style=width:100px;height:100px;/>" + " Passed Screenshotlink" + "</a><br/>");
        }
        /* Test Rail Update */
        try {
            caseId = System.getProperty("caseId");
            List<String> caseidlist = Arrays.asList(caseId.split(","));
            if (caseId.length() > 2 && caseidlist.size() == 1) {
                TestRailAPI.testRailAddResultAPI(1, "Test case has been Passed", runId, caseId);
            } else if (caseId.length() > 2 && caseidlist.size() > 1) {
                TestRailAPI.testRailMultipleCaseAPI(1, "Test case has been Passed", runId, caseidlist);
            }
        } catch (NullPointerException e) {
            System.out.println("CaseId for this test is null , Please Update this line in test case System.setProperty(\"caseId\", TCid.toString())");
        }
    }

    public void onTestFailure(ITestResult result) {
        System.out.println("class Name - " + simpleClassName + " and Method Name -" + result.getName() + " has been executed and Failed");
        try {
            failedTestScreenshotPath = GetScreenshot.captureFailed(simpleClassName, formattedDate + "_" + simpleClassName + "_" + result.getName());
        } catch (IOException exception) {
            logger.error("Failed to capture screenshot", exception);
        }
        failedTestScreenshotPath = failedTestScreenshotPath.replace("/opt/shutterfly/jenkins/workspace/" +
                "testjobs/Mission Control tests/sbs-mc-fhub-test/",
                "http://sbscon01-lv.internal.shutterfly.com:8080/job/testjobs/job/Mission%20Control%20tests/job/sbs-mc-fhub-test/ws/");
        Reporter.log("<a href=" + failedTestScreenshotPath + "><img src=" + failedTestScreenshotPath +
                " style=width:100px;height:100px;/>" + " Failed Screenshotlink" + "</a><br/>");
        /* Test Rail Update */
        try {
            caseId = System.getProperty("caseId");
            List<String> caseidlist = Arrays.asList(caseId.split(","));
            if (caseId.length() > 2 && caseidlist.size() == 1) {
                TestRailAPI.testRailAddResultAPI(2, "Test case has been Failed", runId, caseId);
            } else if (caseId.length() > 2 && caseidlist.size() > 1) {
                TestRailAPI.testRailMultipleCaseAPI(2, "Test case has been Failed", runId, caseidlist);
            }
        } catch (NullPointerException exception) {
            logger.error("CaseId for this test is null , Please Update this line in test case System.setProperty" +
                    "(\"caseId\", TCid.toString())", exception);
        }
    }

    @Override
    public void onTestSkipped(ITestResult iTestResult) {

    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {

    }

    public void onStart(ITestContext context) {
        runId = System.getProperty("runId");
    }

    @Override
    public void onFinish(ITestContext iTestContext) {

    }

}