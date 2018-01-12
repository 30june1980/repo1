package com.shutterfly.missioncontrolportal.pageobject;

import com.shutterfly.missioncontrolportal.Utils.PageUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

import java.util.List;
import java.util.stream.Collectors;

public class PortalPage {

    private final String loaderXpath = "//div[@class='loader']";
    private final String nextLblId = "smc-pagination_next";
    private final String prevLblId = "smc-pagination_prev";
    private final String searchResultCountLblXpath = "//app-pagination/div/div[2]/span[2]/span";
    private final String activeUserNameLblXpath = "//text()[contains(.,'TestUserFirstName')]//ancestor::a[1]";
    private final String logoutLblXpath = "//a[text()='Logout']";

    private WebDriver driver;
    @FindBy(how = How.CSS, using = "img[alt='United Health Care logo']")
    private WebElement uhcLogoImg;
    @FindBy(how = How.CSS, using = "body > app-root > nav.nav.navbar.nav2 > div > ul > li")
    private List<WebElement> tabLblList;
    @FindBy(how = How.CSS, using = "body > app-root > div")
    private WebElement footerLbl;
    @FindBy(how = How.ID, using = "requestId")
    private WebElement requestIdTxt;
    @FindBy(how = How.ID, using = "smc_search")
    private WebElement searchBtn;

    public PortalPage(WebDriver edriver) {
        this.driver = edriver;
    }

    private WebElement getLoader() {
        return driver.findElement(By.xpath(loaderXpath));
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

    public void setRequestIdTxt(String requestId) {
        requestIdTxt.sendKeys(requestId);
    }

    public void clickOnSearchBtn() {
        searchBtn.click();
        PageUtils.waitForLoadingToComplete(driver, getLoader());
    }

    public void clickOnNextLbl() {
        getNextLbl().click();
        try {
            PageUtils.waitForLoadingToComplete(driver, getLoader());
        } catch (NoSuchElementException ignored) {
        }
    }

    public void clickOnBackLbl() {
        getPrevLbl().click();
        try {
            PageUtils.waitForLoadingToComplete(driver, getLoader());
        } catch (NoSuchElementException ignored) {
        }
    }

    public void clickOnLogout() {
        getActiveUserNameLbl().click();
        getLogoutLbl().click();
    }


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

    public List<String> getTabLblList() {
        return tabLblList.stream().map(x -> x.getText().trim()).collect(Collectors.toList());
    }

    public String getFooter() {
        return footerLbl.getText().toLowerCase().trim();
    }

    public String getUserName() {
        return getActiveUserNameLbl().getText();
    }

}
