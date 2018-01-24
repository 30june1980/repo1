package com.shutterfly.missioncontrolportal.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

public class SegmentPage {

    private WebDriver driver;

    private String newSegmentXpath = "//span[text()='%s']";

    @FindBy(how = How.XPATH, using = "//button[text()='ADD BUSINESS SEGMENT']")
    private WebElement addBusinessSegmentBtn;

    @FindBy(how = How.XPATH, using = "//input[@placeholder='Enter a Name for the New Business Segment']")
    private WebElement businessSegmentNameTxt;

    @FindBy(how = How.XPATH, using = "//button[contains(text(),'Save')]")
    private WebElement saveBtn;

    @FindBy(how = How.XPATH, using = "//input[@placeholder='Enter ID']")
    private WebElement segmentIdTxt;

    @FindBy(how = How.XPATH, using = "//app-segments/div/div[2]/div[2]/div[2]/div/input")
    private WebElement startDateTxt;

    @FindBy(how = How.XPATH, using = "//app-segments/div/div[2]/div[2]/div[3]/div/input")
    private WebElement endDateTxt;

    @FindBy(how = How.XPATH, using = "//text()[contains(.,'SAVE BUSINESS SEGMENT')]/ancestor::button[1]")
    private WebElement saveSegmentBtn;

    public WebElement getSavedSegment(String segment) {
        return driver.findElement(By.xpath(String.format(newSegmentXpath, segment)));
    }

    public SegmentPage(WebDriver eDriver) {
        this.driver = eDriver;
    }

    // action methods
    public void clickOnAddSegmentBtn() {
        addBusinessSegmentBtn.click();
    }

    public void clickOnSaveBtn() {
        saveBtn.click();
    }

    public void clickOnSaveSegmentBtn() {
        saveSegmentBtn.click();
    }

    public void setSegmentName(String segmentName) {
        businessSegmentNameTxt.sendKeys(segmentName);
    }

    public void setSegmentId(String segmentId) {
        segmentIdTxt.sendKeys(segmentId);
    }

    public void setStartDate(String startDate) {
        startDateTxt.clear();
        startDateTxt.sendKeys(startDate);
    }

    public void setEndDate(String endDate) {
        endDateTxt.sendKeys(endDate);
    }

}
