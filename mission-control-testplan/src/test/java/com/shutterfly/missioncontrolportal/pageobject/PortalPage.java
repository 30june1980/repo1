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

    private WebDriver driver;
    @FindBy(how = How.CSS, using = "img[alt='United Health Care logo']")
    private WebElement uhcLogoImg;
    @FindBy(how = How.XPATH, using = "//ul/div/div[1]/div/a")
    private WebElement activeUserNameLbl;
    @FindBy(how = How.CSS, using = "body > app-root > nav.nav.navbar.nav2 > div > ul > li")
    private List<WebElement> tabLblList;
    @FindBy(how = How.CSS, using = "body > app-root > div")
    private WebElement footerLbl;
    @FindBy(how = How.ID, using = "requestId")
    private WebElement requestIdTxt;
    @FindBy(how = How.ID, using = "smc_search")
    private WebElement searchBtn;
    @FindBy(how = How.CSS, using = "h4 > div > div:nth-child(1)")
    private WebElement searchResultCountLbl;
    @FindBy(how = How.XPATH, using = "//a[text()='Logout']")
    private WebElement logoutLbl;

    public PortalPage(WebDriver edriver) {
        this.driver = edriver;
    }

    private WebElement getLoader() {
        return driver.findElement(By.xpath(loaderXpath));
    }

    private WebElement getNextLbl() {
        return driver.findElement(By.id(nextLblId));
    }

    private WebElement getPrevLbl() {
        return driver.findElement(By.id(prevLblId));
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
        PageUtils.waitForLoadingToComplete(driver, getLoader());
    }

    public void clickOnBackLbl() {
        getPrevLbl().click();
        PageUtils.waitForLoadingToComplete(driver, getLoader());
    }

    public void clickOnLogout() {
        activeUserNameLbl.click();
        logoutLbl.click();
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
        return searchResultCountLbl.isDisplayed();
    }

    public int getSearchResultCount() {
        return Integer.parseInt(searchResultCountLbl.getText().trim().split(" ")[0]);
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
        return activeUserNameLbl.getText();
    }


}
