package com.shutterfly.missioncontrolportal.pageobject;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.stream.Collectors;

public class PortalPage {

    private final String loaderXpath = "//div[@class='loader']";
    private final String nextLblId = "smc-pagination_next";
    private final String prevLblId = "smc-pagination_prev";
    private final String searchResultCountLblXpath = "//app-pagination/div/div[2]/span[2]/span";
    private final String activeUserNameLblXpath = "//text()[contains(.,'TestUserFirstName')]//ancestor::a[1]";
    private final String logoutLblXpath = "//a[text()='Logout']";
    private final String dropDownOptionsXpath = "//*[@id=\"additional_fields\"]/div/div/div[1]/div/div/ng2-auto-complete";
    private WebDriver driver;

    @FindBy(how = How.CSS, using = "img[alt='United Health Care logo']")
    private WebElement uhcLogoImg;
    @FindBy(how = How.CSS, using = "body > app-root > nav.nav.navbar.nav2 > div > ul > li")
    private List<WebElement> tabLblList;
    @FindBy(how = How.CSS, using = "body > app-root > div")
    private WebElement footerLbl;

    // Search panel
    @FindBy(how = How.ID, using = "requestId")
    private WebElement requestIdTxt;
    @FindBy(how = How.ID, using = "from")
    private WebElement fromDateTxt;
    @FindBy(how = How.ID, using = "to")
    private WebElement toDateTxt;
    @FindBy(how = How.XPATH, using = "//div[2]/datatable-body-cell[1]")
    private List<WebElement> requestDatesTxt;
    @FindBy(how = How.XPATH, using = "//div[2]/datatable-body-cell[3]")
    private List<WebElement> requestIdsTxt;
    @FindBy(how = How.ID, using = "smc_search")
    private WebElement searchBtn;
    @FindBy(how = How.CSS, using = "app-navigating-back > h4 > a")
    private WebElement backBtn;


    // Search - Dynamic filters
    @FindBy(how = How.ID, using = "smc_view_additional_details")
    private WebElement additionalFiltersLbl;
    @FindBy(how = How.ID, using = "smc_input_additional_filters")
    private WebElement filterTypeDropdown;

    public PortalPage(WebDriver edriver) {
        this.driver = edriver;
    }

    // Dynamic web element functions
    private void waitForLoaderAvailability() {
        WebDriverWait wait = new WebDriverWait(driver, 10);
//        wait.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(loaderXpath))));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(loaderXpath)));
    }

    private WebElement getPrevLbl() {
        return driver.findElement(By.id(prevLblId));
    }

    private WebElement getActiveUserNameLbl() {
        return driver.findElement(By.xpath(activeUserNameLblXpath));
    }

    private WebElement getSearchResultCountLbl() {
        return driver.findElement(By.xpath(searchResultCountLblXpath));
    }

    private WebElement getNextLbl() {
        return driver.findElement(By.id(nextLblId));
    }

    private WebElement getLogoutLbl() {
        return driver.findElement(By.xpath(logoutLblXpath));
    }


    // Action functions
    public void setRequestIdTxt(String requestId) {
        requestIdTxt.sendKeys(requestId);
    }

    public void setFromDateTxt(String date) {
        fromDateTxt.sendKeys(date);
    }

    public void setToDateTxt(String date) {
        toDateTxt.sendKeys(date);
    }

    public void clickOnSearchBtn() {
        searchBtn.click();
        waitForLoaderAvailability();
    }

    public void clickOnBackBtn() {
        backBtn.click();
    }

    public void clickOnAdditionalFiltersLbl() {
        additionalFiltersLbl.click();
    }

    public void clickOnFilterDropdown() {
        filterTypeDropdown.click();
    }

    public void clickOnNextLbl() {
        getNextLbl().click();
        waitForLoaderAvailability();
    }

    public void clickOnBackLbl() {
        getPrevLbl().click();
        waitForLoaderAvailability();
    }

    public void clickOnLogout() {
        getActiveUserNameLbl().click();
        getLogoutLbl().click();
    }

    public void clickOnIthResult(int i) {
        requestDatesTxt.get(i).click();
    }


    // Utility functions
    public boolean isNextLblClickable() {
        try {
            getNextLbl().isDisplayed();
            return true;
        } catch (NoSuchElementException ne) {
            return false;
        }
    }

    public boolean isPrevLblClickable() {
        try {
            getPrevLbl().isDisplayed();
            return true;
        } catch (NoSuchElementException ne) {
            return false;
        }
    }

    public boolean areSearchResultsVisible() {
        return getSearchResultCountLbl().isDisplayed();
    }

    public int getSearchResultCount() {
        String[] tokens = getSearchResultCountLbl().getText().trim().split(" ");
        return Integer.parseInt(tokens[tokens.length - 1]);
    }

    public boolean isUhcLogoPresent() {
        return uhcLogoImg.isDisplayed();
    }

    public boolean isBackButtonDisplayed() {
        return backBtn.isDisplayed();
    }

    public List<String> getTabLblList() {
        return tabLblList.stream().map(x -> x.getText().trim()).collect(Collectors.toList());
    }

    public String getFooter() {
        return footerLbl.getText().toLowerCase().trim();
    }

    public String getUserName() {
        return getActiveUserNameLbl().getText();
    }

    public List<String> getRequestDates() {
        return requestDatesTxt.stream().map(requestDate -> requestDate.getText().trim()).collect(Collectors.toList());
    }

    public List<String> getRequestIds() {
        return requestIdsTxt.stream().map(requestId -> requestId.getText().trim()).collect(Collectors.toList());
    }

    public String getDropDownOptions() {
        return driver.findElement(By.xpath(dropDownOptionsXpath)).getText().trim();
    }

}
