package com.applitools.eyes.selenium;

import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.selenium.fluent.Target;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
@SuppressWarnings("SpellCheckingInspection")
public class TestAcme extends TestSetup {

    @Factory(dataProvider = "dp", dataProviderClass = TestsDataProvider.class)
    public TestAcme(Capabilities caps, String platform) {
        super.caps = caps;
        super.platform = platform;

        testSuitName = "Test Acme";
        testedPageUrl = "file:///C:/temp/fluentexample/Account%20-%20ACME.html";
        testedPageSize = new RectangleSize(1024, 768);
    }

    @Test
    public void Test(){
        getEyes().check("main window with table",
                Target.window()
                        .fully()
                        .ignore(By.className("toolbar"))
                        .layout(By.id("orders-list-desktop"), By.className("snapshot-topic"), By.id("results-count"))
                        .strict()
        );
    }

    @Test
    public void TestAcmeLogin() {
        driver.get("https://afternoon-savannah-68940.herokuapp.com/#");
        WebElement username = driver.findElement(By.id("username"));
        username.click();
        username.sendKeys("adamC");
        WebElement password = driver.findElement(By.id("password"));
        username.click();
        password.sendKeys("MySecret123?");
        (getEyes()).check(
                Target.region(username),
                Target.region(password)
        );
    }
}
