package com.shutterfly.missioncontrolportal.pageobject;

import com.shutterfly.missioncontrolportal.Utils.PageUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

import java.util.List;
import java.util.stream.Collectors;

public class PortalPage {

    private WebDriver driver;

    @FindBy(how = How.CSS, using = "img[alt='United Health Care logo']")
    @CacheLookup
    private WebElement uhcLogoImg;

    @FindBy(how = How.XPATH, using = "//ul/div/div[1]/div/a")
    @CacheLookup
    private WebElement activeUserNameLbl;

    @FindBy(how = How.CSS, using = "body > app-root > nav.nav.navbar.nav2 > div > ul > li")
    @CacheLookup
    private List<WebElement> tabLblList;

    @FindBy(how = How.CSS, using = "body > app-root > div")
    @CacheLookup
    private WebElement footerLbl;

    @FindBy(how = How.ID, using = "requestId")
    @CacheLookup
    private WebElement requestIdTxt;

    @FindBy(how = How.ID, using = "smc_search")
    @CacheLookup
    private WebElement submitBtn;

    private WebElement loader;

    private WebElement searchResultCountLbl;

    private WebElement logoutLbl;

    public PortalPage(WebDriver edriver) {
        this.driver = edriver;
    }

    public String getUserName() {
        if (PageUtils.isWebElementPresent(activeUserNameLbl)) {
            return activeUserNameLbl.getText();
        }
        throw new RuntimeException("Active user name not found on the page");
    }

    public WebElement getActiveUserNameLbl() {
        activeUserNameLbl = driver.findElement(By.xpath("//ul/div/div[1]/div/a"));
        return activeUserNameLbl;
    }

    public List<String> getTabLblList() {
        if (PageUtils.areWebElementsPresent(tabLblList)) {
            return tabLblList.stream().map(x -> x.getText().trim()).collect(Collectors.toList());
        }
        throw new RuntimeException("One or more tabs not found on the page");
    }

    public String getFooterLbl() {
        if (PageUtils.isWebElementPresent(footerLbl)) {
            return footerLbl.getText().toLowerCase().trim();
        }
        throw new RuntimeException("Footer not found on the page");
    }

    private WebElement getLoader() {
        loader = driver.findElement(By.xpath("//div[@class='loader']"));
        return loader;
    }

    private WebElement getLogoutLbl() {
        logoutLbl = driver.findElement(By.xpath("//a[text()='Logout']"));
        return logoutLbl;
    }

    public boolean isUhcLogoPresent() {
        return PageUtils.isWebElementPresent(uhcLogoImg);
    }

    public void setRequestIdTxt(String requestId) {
        if (PageUtils.isWebElementPresent(requestIdTxt)) {
            requestIdTxt.sendKeys(requestId);
            return;
        }
        throw new RuntimeException("RequestId input not found on the page");
    }

    public void clickSubmitBtn() {
        if (PageUtils.isWebElementPresent(submitBtn)) {
            submitBtn.click();
            PageUtils.waitForLoadingToComplete(driver, getLoader());
            return;
        }
        throw new RuntimeException("Submit button not found on the page");
    }

    public void clickOnLogout() {
        if (PageUtils.isWebElementPresent(getActiveUserNameLbl())) {
            activeUserNameLbl.click();
            getLogoutLbl().click();
            return;
        }
        throw new RuntimeException("Logout button not found on the page");
    }

    public boolean areSearchResultsVisible() {
        searchResultCountLbl = driver.findElement(By.cssSelector("h4 > div > div:nth-child(1)"));
        return PageUtils.isWebElementPresent(searchResultCountLbl);
    }

}
