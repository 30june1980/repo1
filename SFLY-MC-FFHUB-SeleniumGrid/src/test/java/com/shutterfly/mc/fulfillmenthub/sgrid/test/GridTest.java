package com.shutterfly.mc.fulfillmenthub.sgrid.test;

import com.shutterfly.mc.fulfillmenthub.sgrid.core.StartGrid;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.Reporter;
import org.testng.annotations.Test;

/**
 * Created by kedar on 03-10-2016.
 */
public class GridTest extends StartGrid {

    @Test(priority = 0)
    public void openApplicationHomePage(){
        driver().get("http://www.thetestroom.com/webapp");
        Assert.assertEquals((driver().getTitle()),"Zoo Adoption | Home");
        Reporter.log("Passed : Correct App Home Page open");
    }
    @Test(priority = 1)
    public void userNavigatesToContactPage() throws InterruptedException {
        try {
            driver().findElement(By.xpath(".//*[@id='contact_link']")).click();
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertEquals((driver().getTitle()),"Contact");
        Reporter.log("Passed : Contact Page is open");
    }
    @Test(priority = 2)
    public void userNavigatesBackApplicationToHomePage(){
        driver().findElement(By.xpath(".//*[@id='home_link']")).click();
        Assert.assertEquals((driver().getTitle()),"Zoo Adoption | Home");
        Reporter.log("Passed : Navigated back to HomePage");
    }
}
