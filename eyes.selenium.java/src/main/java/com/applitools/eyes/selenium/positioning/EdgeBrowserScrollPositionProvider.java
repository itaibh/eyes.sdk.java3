package com.applitools.eyes.selenium.positioning;

import com.applitools.eyes.IEyesJsExecutor;
import com.applitools.eyes.Location;
import com.applitools.eyes.Logger;
import org.openqa.selenium.WebElement;

public class EdgeBrowserScrollPositionProvider extends ScrollPositionProvider {

    public EdgeBrowserScrollPositionProvider(Logger logger, IEyesJsExecutor executor, WebElement scrollRootElement) {
        super(logger, executor, scrollRootElement);
    }

    @Override
    public Location setPosition(Location location) {
        logger.verbose(String.format("setting position of %s to %s", scrollRootElement, location));
        Object position = executor.executeScript(String.format("window.scrollTo(%d,%d);" +
                        "return (window.scrollX+';'+window.scrollY);",
                location.getX(), location.getY()),
                scrollRootElement);
        return parseLocationString(position);
    }

    @Override
    public Location getCurrentPosition() {
        Object position = executor.executeScript("return (window.scrollX+';'+window.scrollY);", scrollRootElement);
        return parseLocationString(position);
    }
}
