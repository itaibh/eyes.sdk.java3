package com.applitools.eyes.selenium;

import com.applitools.eyes.selenium.fluent.Target;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
public class TestScrollRootElement extends TestSetup {
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
    public TestScrollRootElement(Capabilities caps, String platform) {
        super.caps = caps;
        super.platform = platform;
        super.forceFPS = false;

        testSuitName = "SeleniumEyes Selenium SDK - Scroll Root Element";
        testedPageUrl = "https://applitools.github.io/demo/TestPages/SimpleTestPage/scrollablebody.html";
    }

    @Test
    public void TestCheckWindow_Body_Scroll(){
        eyes.setStitchMode(StitchMode.SCROLL);
        eyes.check("Body (Scroll stitching)", Target.window().scrollRootElement(By.tagName("body")).fully());
    }

    @Test
    public void TestCheckWindow_Html_Scroll(){
        eyes.setStitchMode(StitchMode.SCROLL);
        eyes.check("Html (Scroll stitching)", Target.window().scrollRootElement(By.tagName("html")).fully());
    }

    @Test
    public void TestCheckWindow_Body_Css(){
        eyes.setStitchMode(StitchMode.CSS);
        eyes.check("Body (Css stitching)", Target.window().scrollRootElement(By.tagName("body")).fully());
    }

    @Test
    public void TestCheckWindow_Html_Css(){
        eyes.setStitchMode(StitchMode.CSS);
        eyes.check("Html (Css stitching)", Target.window().scrollRootElement(By.tagName("html")).fully());
    }
}
