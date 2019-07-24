package com.applitools.eyes.selenium;

import com.applitools.eyes.RectangleSize;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
public class TestEkb extends TestSetup {

    private Eyes eyes;

    @Override
    public void setEyes(Eyes eyes) {
        this.eyes = eyes;
    }

    @Override
    public Eyes getEyes(){
        return this.eyes;
    }

    @Factory(dataProvider = "dp", dataProviderClass = TestsDataProvider.class)
    public TestEkb(Capabilities caps, String platform) {
        super.caps = caps;
        super.platform = platform;
        super.forceFPS = false;
        super.testedPageSize = new RectangleSize(1024, 900);

        testSuitName = "SeleniumEyes Selenium SDK - Applitools EKB";
        testedPageUrl = "https://www.applitools.com/docs/topics/working-with-test-batches/working-with-test-batches.html";
    }

    @Test
    public void TestEKBPage(){
        //VisualGridEyes.checkRegion(By.cssSelector("div.horizontal-page"), "EKB - Working with test batches", true);
        eyes.checkRegion(By.cssSelector(".docs.page-container"), "EKB - Working with test batches", true);
//        VisualGridEyes.check("EKB - Working with test batches",
//                Target.region(By.cssSelector(".docs.page-container"))
//                      .fully()
//                      //.scrollRootElement(By.cssSelector("div.horizontal-page"))
//        );
    }
}
