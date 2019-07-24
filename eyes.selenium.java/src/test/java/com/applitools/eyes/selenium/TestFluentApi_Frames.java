package com.applitools.eyes.selenium;

import com.applitools.ICheckSettings;
import com.applitools.eyes.CoordinatesType;
import com.applitools.eyes.FloatingMatchSettings;
import com.applitools.eyes.Region;
import com.applitools.eyes.selenium.fluent.Target;

import org.openqa.selenium.*;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.List;

@Listeners(TestListener.class)
public class TestFluentApi_Frames extends TestSetup {

    @Factory(dataProvider = "dp", dataProviderClass = TestsDataProvider.class)
    public TestFluentApi_Frames(Capabilities caps, String platform) {
        super.caps = caps;
        super.platform = platform;
        super.forceFPS = false;

        super.compareExpectedRegions = caps.getBrowserName().equalsIgnoreCase("chrome");
        testSuitName = "Eyes Selenium SDK - Fluent API";
        testedPageUrl = "https://applitools.github.io/demo/TestPages/FramesTestPage/";
    }

    @Test
    public void TestCheckFrame_Fully_Fluent() {
        getEyes().check("Fluent - Full Frame", Target.frame("frame1").fully());
    }

    @Test
    public void TestCheckFrame_Fluent() {
        getEyes().check("Fluent - Frame", Target.frame("frame1"));
    }

    @Test
    public void TestCheckFrameInFrame_Fully_Fluent() {
        getEyes().check("Fluent - Full Frame in Frame", Target.frame("frame1")
                .frame("frame1-1")
                .fully());
    }

    @Test
    public void TestCheckRegionInFrame_Fluent() {
        getEyes().check("Fluent - Region in Frame", Target.frame("frame1")
                .region(By.id("inner-frame-div"))
                .fully());
    }

    @Test
    public void TestCheckRegionInFrameInFrame_Fluent() {
        getEyes().check("Fluent - Region in Frame in Frame", Target.frame("frame1")
                .frame("frame1-1")
                .region(By.tagName("img"))
                .fully()
        );
    }

    @Test
    public void TestCheckRegionInFrame2_Fluent() {
        getEyes().check("Fluent - Inner frame div 1", Target.frame("frame1")
                .region(By.id("inner-frame-div"))
                .fully()
                .timeout(5000)
                .ignore(new Region(50, 50, 100, 100)));

        getEyes().check("Fluent - Inner frame div 2", Target.frame("frame1")
                .region(By.id("inner-frame-div"))
                .fully()
                .ignore(new Region(50, 50, 100, 100))
                .ignore(new Region(70, 170, 90, 90)));

        getEyes().check("Fluent - Inner frame div 3", Target.frame("frame1")
                .region(By.id("inner-frame-div"))
                .fully()
                .timeout(5000));

        getEyes().check("Fluent - Inner frame div 4", Target.frame("frame1")
                .region(By.id("inner-frame-div"))
                .fully());

        getEyes().check("Fluent - Full frame with floating region", Target.frame("frame1")
                .fully()
                .layout()
                .floating(25, new Region(200, 200, 150, 150)));
    }

    @Test
    public void TestCheckRegionInFrame3_Fluent() {
        getEyes().check("Fluent - Full frame with floating region", Target.frame("frame1")
                .fully()
                .layout()
                .floating(25, new Region(200, 200, 150, 150)));
    }

    @Test
    public void TestCheckRegionByCoordinateInFrameFully_Fluent() {
        getEyes().check("Fluent - Inner frame coordinates", Target.frame("frame1")
                .region(new Region(30, 40, 400, 1200))
                .fully());
    }

    @Test
    public void TestCheckRegionByCoordinateInFrame_Fluent() {
        getEyes().check("Fluent - Inner frame coordinates", Target.frame("frame1")
                .region(new Region(30, 40, 400, 1200)));
    }
    ////////////

    @Test
    public void TestCheckFrameInFrame_Fully_Fluent2() {
        getEyes().check("Fluent - Window", Target.window()
                .fully()
        );

        getEyes().check("Fluent - Full Frame in Frame 2", Target.frame("frame1")
                .frame("frame1-1")
                .fully());
    }

    @Test
    public void TestCheckLongIFrameModal() {
        Eyes eyes = getEyes();
        StitchMode originalStitchMode = eyes.getStitchMode();
        eyes.setStitchMode(StitchMode.SCROLL);
        driver.findElement(By.id("stretched")).click();
        WebElement frame = driver.findElement(By.cssSelector("#modal2 iframe"));
        driver.switchTo().frame(frame);
        WebElement element = driver.findElement(By.tagName("html"));
        Dimension size = element.getSize();
        Point location = element.getLocation();
        Rectangle elementRect = new Rectangle(location, size);
        Region rect;
        List<ICheckSettings> targets = new ArrayList<>();
        for (int i = location.getY(), c = 1; i < location.getY() + size.getHeight(); i += 5000, c++) {
            if ((elementRect.getY() + elementRect.getHeight()) > i + 5000) {
                rect = new Region(location.getX(), i, size.getWidth(), 5000);
            } else {
                rect = new Region(location.getX(), i, size.getWidth(), elementRect.getY() + elementRect.getHeight() - i);
            }
            targets.add(Target.region(rect));
            //eyes_.Check("Long IFrame Modal #" + c, Target.Region(rect).Fully());
        }
        eyes.check(targets.toArray(new ICheckSettings[0]));
        eyes.setStitchMode(originalStitchMode);
    }

}