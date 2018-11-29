package com.applitools.eyes.rendering;

import com.applitools.eyes.*;
import com.applitools.eyes.visualGridClient.IEyesConnector;
import com.applitools.eyes.visualGridClient.IRenderingEyes;
import com.applitools.eyes.visualGridClient.RenderingGridManager;
import com.applitools.eyes.visualGridClient.data.*;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.GeneralUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class Eyes implements IRenderingEyes {

    private final Logger logger;
    private String serverUrl;
    private RenderingConfiguration renderingConfiguration;
    private RenderingGridManager renderingGridManager;
    private List<RunningTest> testList = new ArrayList<>();
    private final List<RunningTest> testsInOpenProcess = new ArrayList<>();
    private final List<RunningTest> testsInCloseProcess = new ArrayList<>();
    private LogHandler logHandler;
    private AtomicBoolean isEyesClosed = new AtomicBoolean(false);
    private IRenderingEyes.EyesListener listener;
    private AbstractProxySettings proxy;

    private String PROCESS_RESOURCES;
    private JavascriptExecutor jsExecutor;
    private RenderingInfo rendringInfo;
    private IEyesConnector eyesConnector;

    {
        try {
            PROCESS_RESOURCES = GeneralUtils.readToEnd(Eyes.class.getResourceAsStream("/processResources.js"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private RunningTest.RunningTestListener testListener = new RunningTest.RunningTestListener() {
        @Override
        public void onTaskComplete(Task task, RunningTest test) {
            switch (task.getType()) {
                case CLOSE:
                    boolean isEyesClosed = true;
                    for (RunningTest runningTest : testList) {
                        isEyesClosed &= runningTest.isTestClose();
                    }
                    Eyes.this.isEyesClosed.set(isEyesClosed);
                    synchronized (Eyes.this.testsInCloseProcess) {
                        Eyes.this.testsInOpenProcess.remove(test);
                    }
                    break;
                case OPEN:
                    synchronized (Eyes.this.testsInOpenProcess) {
                        Eyes.this.testsInOpenProcess.add(test);
                    }
            }

            if (task.getType() == Task.TaskType.CLOSE) {

            }
            if (Eyes.this.listener != null) {
                Eyes.this.listener.onTaskComplete(task, Eyes.this);
            }
        }
    };

    public Eyes(RenderingGridManager renderingGridManager, RenderingConfiguration renderingConfiguration, Logger logger) {
        ArgumentGuard.notNull(renderingConfiguration, "renderingConfiguration");
        this.renderingGridManager = renderingGridManager;
        this.renderingConfiguration = renderingConfiguration;
        this.logger = logger;
        if (this.renderingGridManager == null) {
            this.renderingGridManager = new RenderingGridManager(renderingConfiguration.getConcurrentSessions(), logger);
        }
    }

    public void open(WebDriver webDriver) {
        ArgumentGuard.notNull(webDriver, "webDriver");
        logger.verbose("enter");
        initDriver(webDriver);

        createEyesConnector();

        logger.verbose("initializing rendering info...");
        if (this.rendringInfo == null) {
            this.rendringInfo = eyesConnector.getRenderingInfo();
        }

        logger.verbose("getting all browsers info...");
        List<RenderingConfiguration.RenderBrowserInfo> browserInfos = renderingConfiguration.getBrowsersInfo();
        logger.verbose("creating test descriptors for each browser info...");
        for (RenderingConfiguration.RenderBrowserInfo browserInfo : browserInfos) {
            logger.verbose("creating test descriptor");
            RunningTest test = new RunningTest(this.proxy, eyesConnector, renderingConfiguration, browserInfo, testListener);
            this.testList.add(test);
        }

        logger.verbose(String.format("opening %d tests...", testList.size()));
        for (RunningTest runningTest : testList) {
            runningTest.open();
        }
        logger.verbose("calling renderingGridManager.open");
        this.renderingGridManager.open(this, rendringInfo);
        logger.verbose("done");
    }

    private void createEyesConnector() {
        logger.verbose("creating eyes server connector");
        if (this.eyesConnector != null) {
            return;
        }
        IEyesConnector eyesConnector = new EyesConnector();
        eyesConnector.setProxy(this.proxy);
        if (logHandler != null) {
            eyesConnector.setLogHandler(this.logHandler);
        }
        if (this.serverUrl != null) {
            try {
                eyesConnector.setServerUrl(serverUrl);
            } catch (URISyntaxException e) {
                GeneralUtils.logExceptionStackTrace(e);
            }
        }
        this.eyesConnector = eyesConnector;
    }

    private void initDriver(WebDriver webDriver) {
        if (webDriver instanceof JavascriptExecutor) {
            this.jsExecutor = (JavascriptExecutor) webDriver;
        }
    }


    public RunningTest getNextTestToOpen() {
        RunningTest currentBestTest = null;
        int currentBestMark = -1;
        for (RunningTest test : testList) {
            if (test.isTestOpen() || this.testsInOpenProcess.contains(test)) continue;
            if (test.getMark() > currentBestMark) {
                currentBestTest = test;
            }
        }
        synchronized (testsInOpenProcess) {
            this.testsInOpenProcess.add(currentBestTest);
        }
        return currentBestTest;
    }


    public RunningTest getNextTestToClose() {
        RunningTest test = null;
        for (RunningTest runningTest : testList) {
            if (!runningTest.isTestClose() && runningTest.isTestIsReadyToClose() && !this.testsInCloseProcess.contains(runningTest)) {
                test = runningTest;

            }
        }
        synchronized (testsInCloseProcess) {
            this.testsInCloseProcess.add(test);
        }
        return test;
    }

    public List<Future<TestResults>> close() {
        List<Future<TestResults>> futureList = new ArrayList<>();
        for (RunningTest runningTest : testList) {
            if (!runningTest.isTestClose()) {
                futureList.add(runningTest.close());
            }
        }
        this.renderingGridManager.close(this);
        return futureList;
    }

    @Override
    public RunningTest getNextTestToCheck() {
        return null;
    }

    public void setLogHandler(StdoutLogHandler logHandler) {
        this.logHandler = logHandler;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public boolean isEyesClosed() {
        boolean isEyesClosed = true;
        for (RunningTest runningTest : testList) {
            isEyesClosed = isEyesClosed && runningTest.isTestClose();
        }
        return isEyesClosed;
    }


    public void setListener(EyesListener listener) {
        this.listener = listener;
    }

    /**
     * Sets the proxy settings to be used by the rest client.
     * @param abstractProxySettings The proxy settings to be used by the rest client.
     *                              If {@code null} then no proxy is set.
     */
    public void setProxy(AbstractProxySettings abstractProxySettings) {
        this.proxy = abstractProxySettings;
    }

    public int getRunningTestCount() {
        return this.testList.size();
    }

    public void check(CheckRGSettings settings) {
        List<Task> taskList = new ArrayList<>();
        String script = (String) this.jsExecutor.executeAsyncScript("var callback = arguments[arguments.length - 1]; return (" + PROCESS_RESOURCES + ")().then(JSON.stringify).then(callback, function(err) {callback(err.stack || err.toString())})");
        for (RunningTest test : testList) {
            taskList.add(test.check());
        }
        this.renderingGridManager.check(settings, script, this.renderingConfiguration.getBrowsersInfo(), this.eyesConnector, taskList);
    }


}
