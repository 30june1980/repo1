package com.shutterfly.missioncontrolportal.pageobject;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.How;

import javax.annotation.Nonnull;

public class LoginPage {

    private WebDriver driver;

    @FindBy(how = How.CSS, using = "input[name=userName]")
    private WebElement userNameText;

    @FindBy(how = How.CSS, using = "input[name=password]")
    private WebElement passwordText;

    @FindBy(how = How.CSS, using = "button[id=login-btn]")
    private WebElement loginButton;

    public LoginPage(WebDriver edriver) {
        this.driver = edriver;
    }

    public void login(@Nonnull String url, @Nonnull String userName, @Nonnull String password) {
        driver.get(url);
        userNameText.sendKeys(userName);
        passwordText.sendKeys(password);
        loginButton.submit();
    }

}
