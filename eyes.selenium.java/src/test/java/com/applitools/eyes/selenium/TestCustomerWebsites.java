package com.applitools.eyes.selenium;

import com.applitools.eyes.RectangleSize;
import com.applitools.eyes.Region;
import com.applitools.eyes.selenium.fluent.Target;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.testng.annotations.Factory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(TestListener.class)
@SuppressWarnings("SpellCheckingInspection")
public class TestCustomerWebsites extends TestSetup {

    @Factory(dataProvider = "dp", dataProviderClass = TestsDataProvider.class)
    public TestCustomerWebsites(Capabilities caps, String platform) {
        super.caps = caps;
        super.platform = platform;
        super.forceFPS = false;

        super.compareExpectedRegions = caps.getBrowserName().equalsIgnoreCase("chrome");

        testSuitName = "Test Customer Websites";
        testedPageUrl = null;
        testedPageSize = new RectangleSize(1266, 800);
    }

    @Test
    public void TestOrbis() {
        driver.get("https://www.orbis.com/jp/institutional/about-us/press-room");
        getEyes().check("Orbis Full Window", Target.window().fully()
                .layout(By.cssSelector("div[test-id=press-articles-0] > progressive-display > div"), By.className("carousel-row-wrapper")));
        setExpectedLayoutRegions(
                new Region(52, 2480, 1162, 44),
                new Region(0, 800, 1266, 425),
                new Region(52, 1275, 1162, 1205));
    }

    /*
    @Test
    public void TestCreditCards() {
        driver.get("https://creditcards.com/v2/zero-interest");
        //noinspection SpellCheckingInspection
        driver.findElement(By.cssSelector("p[model-tagular-uid='117']")).click();
        By selector = By.cssSelector("body > div.boxy > main > div.boxy__product-box.product-list > div:nth-child(2)");
        VisualGridEyes.check("region", Target.region(selector).fully());
    }
    */

    @Test
    public void TestAwwwards() {
        driver.get("https://www.awwwards.com/websites/single-page/");
        getEyes().check("test", Target.region(By.cssSelector("#content")).fully().ignore(By.cssSelector(".box-photo")).layout());
    }

    @Test
    public void TestNicorette() {
        driver.get("https://www.nicorette.es/productos");
        getEyes().check("Nicorette Full Window", Target.window().fully()
                .layout(By.cssSelector(".view-products > .view-content"))
                .ignore(By.cssSelector(".view-products > .view-content img")));
    }
}
