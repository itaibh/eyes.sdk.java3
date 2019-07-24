package com.applitools.eyes.visualgrid.services;

import com.applitools.ICheckSettings;
import com.applitools.eyes.*;
import com.applitools.eyes.config.IConfigurationSetter;
import com.applitools.eyes.selenium.IConfigurationGetter;
import com.applitools.eyes.visualgrid.model.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public interface IEyesConnector {
    void log(String massage);

    IConfigurationSetter setProxy(AbstractProxySettings proxy);

    void setLogHandler(LogHandler logHandler);

    IConfigurationSetter setServerUrl(String serverUrl) throws URISyntaxException;

    URI getServerUrl();

    void open(IConfigurationGetter configProvider, String appName, String testName);

    TestResults close(boolean throwExceptionOn);

    TestResults abortIfNotClosed();

    IResourceFuture getResource(URL url, String userAgent);

    RenderingInfo getRenderingInfo();

    IPutFuture renderPutResource(RunningRender runningRender, RGridResource resource, String userAgent);

    List<RunningRender> render(RenderRequest... renderRequests);

    List<RenderStatusResults> renderStatusById(String... renderIds);

    IResourceFuture createResourceFuture(RGridResource rg);

    MatchResult matchWindow(String resultImageURL, String domLocation, ICheckSettings checkSettings,
                            List<? extends IRegion> regions, List<VisualGridSelector[]> regionSelectors, Location location,
                            String renderId, String source);

    void setRenderInfo(RenderingInfo renderingInfo);

    IConfigurationSetter setBatch(BatchInfo batchInfo);

    void setUserAgent(String userAgent);

    String getApiKey();

    IConfigurationSetter setApiKey(String apiKey);

    void setBranchName(String branchName);

    void setParentBranchName(String parentBranchName);

    void setDevice(String device);

    RectangleSize getDeviceSize();

    void setDeviceSize(RectangleSize deviceSize);

    RunningSession getSession();

    void addProperty(String name, String value);

    void clearProperties();
}
