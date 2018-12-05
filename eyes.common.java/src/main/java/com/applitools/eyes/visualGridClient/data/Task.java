package com.applitools.eyes.visualGridClient.data;

import com.applitools.ICheckSettings;
import com.applitools.ICheckSettingsInternal;
import com.applitools.eyes.Logger;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.visualGridClient.IEyesConnector;
import com.applitools.utils.GeneralUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class Task implements Callable<TestResults> {


    private static AtomicBoolean isThrown = new AtomicBoolean(false);
    private final Logger logger;


    public enum TaskType {OPEN, CHECK, CLOSE, ABORT}


    private TestResults testResults;
    private IEyesConnector eyesConnector;

    private TaskType type;
    private RenderStatusResults renderResult;
    private TaskListener runningTestListener;
    private RenderingConfiguration.RenderBrowserInfo browserInfo;
    private RenderingConfiguration configuration;
    private ICheckSettings checkSettings;

    interface TaskListener {

        void onTaskComplete(Task task);

        void onTaskFailed(Exception e);

    }


    public Task(TestResults testResults, IEyesConnector eyesConnector, TaskType type, RenderingConfiguration.RenderBrowserInfo browserInfo,
                RenderingConfiguration configuration, Logger logger, TaskListener runningTestListener, ICheckSettings checkSettings) {
        this.testResults = testResults;
        this.eyesConnector = eyesConnector;
        this.type = type;
        this.runningTestListener = runningTestListener;
        this.browserInfo = browserInfo;
        this.configuration = configuration;
        this.logger = logger;
        this.checkSettings = checkSettings;
    }

    public RenderingConfiguration.RenderBrowserInfo getBrowserInfo() {
        return browserInfo;
    }

    public TaskType getType() {
        return type;
    }


    @Override
    public TestResults call() throws Exception {
        try {
            testResults = null;
            switch (type) {
                case OPEN:
                    System.out.println("Task.run opening task");
                    eyesConnector.open(configuration);
                    break;
                case CHECK:
                    eyesConnector.matchWindow(renderResult.getImageLocation(), checkSettings);

                    break;
                case CLOSE:
                    testResults = eyesConnector.close(configuration.isThrowExceptionOn());
                    break;
                case ABORT:
                    eyesConnector.abortIfNotClosed();
            }
            //call the callback
            this.runningTestListener.onTaskComplete(this);
            return testResults;
        } catch (Exception e) {
            GeneralUtils.logExceptionStackTrace(logger, e);
            this.runningTestListener.onTaskFailed(e);
        }
        return null;
    }

    public IEyesConnector getEyesConnector() {
        return eyesConnector;
    }

    private static boolean isThrown() {
        return Task.isThrown.get();
    }

    public void setRenderResult(RenderStatusResults renderResult) {
        this.renderResult = renderResult;
    }

    public boolean isTaskReadyToCheck() {
        return this.renderResult != null;
    }
}

