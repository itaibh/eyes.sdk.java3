package com.applitools.eyes.selenium.rendering;

import org.openqa.selenium.WebElement;

public class WebElementRegion {
    private WebElement element;
    private Object region;

    public WebElementRegion(WebElement element, Object region) {
        this.element = element;
        this.region = region;
    }

    public WebElement getElement() {
        return element;
    }

    public Object getRegion() {
        return region;
    }
}
