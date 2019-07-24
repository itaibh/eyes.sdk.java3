package com.applitools.eyes;

/**
 * The environment in which the application under test is executing.
 */
public class AppEnvironment {
    private String inferred;
    private String os;
    private String hostingApp;
    private RectangleSize displaySize;
    private String deviceInfo;
    private String osInfo;
    private String hostingAppInfo;

    /**
     * Creates a new AppEnvironment instance.
     */
    public AppEnvironment() {
    }

    /**
     * Creates a new AppEnvironment instance.
     *
     * @param inferred the inferred environment information.
     */
    public AppEnvironment(String inferred) {
        this.inferred = inferred;
    }


    /**
     * Creates a new AppEnvironment instance.
     *
     * @param os          the OS hosting the application under test or {@code null} if                    unknown.
     * @param hostingApp  the application hosting the application under test or {@code null}                    * if unknown.
     * @param displaySize the display size of the application or {@code null} if unknown.
     */
    public AppEnvironment(String os, String hostingApp,
                          RectangleSize displaySize) {
        setOs(os);
        setHostingApp(hostingApp);
        setDisplaySize(displaySize);
    }

    /**
     * Creates a new AppEnvironment instance.
     *
     * @param os             the OS hosting the application under test or {@code null} if                    unknown.
     * @param hostingApp     the application hosting the application under test or {@code null}                    if unknown.
     * @param displaySize    the display size of the application or {@code null} if unknown.
     * @param deviceInfo     specifies
     * @param osInfo         the os info
     * @param hostingAppInfo the hosting app info
     */
    public AppEnvironment(String os, String hostingApp, RectangleSize displaySize, String deviceInfo, String osInfo, String hostingAppInfo) {
        this.os = os;
        this.hostingApp = hostingApp;
        this.displaySize = displaySize;
        this.deviceInfo = deviceInfo;
        this.osInfo = osInfo;
        this.hostingAppInfo = hostingAppInfo;
    }

    /**
     * Gets inferred.
     *
     * @return the information inferred from the execution environment or {@code null} if no information could be inferred.
     */
    public String getInferred() {
        return inferred;
    }

    /**
     * Sets inferred.
     *
     * @param inferred -  the inferred environment information.
     */
    public void setInferred(String inferred) {
        this.inferred = inferred;
    }

    /**
     * Gets os.
     *
     * @return the OS hosting the application under test or {@code null} if unknown.
     */
    public String getOs() {
        return os;
    }

    /**
     * Sets os.
     *
     * @param os -  the OS hosting the application under test or {@code null} if           unknown.
     */
    public void setOs(String os) {
        this.os = os;
    }

    /**
     * Gets hosting app.
     *
     * @return the application hosting the application under test or {@code null} if unknown.
     */
    @SuppressWarnings("UnusedDeclaration")
    public String getHostingApp() {
        return hostingApp;
    }

    /**
     * Sets hosting app.
     *
     * @param hostingApp -  the application hosting the application under test or {@code null}                   if unknown.
     */
    public void setHostingApp(String hostingApp) {
        this.hostingApp = hostingApp;
    }

    /**
     * Gets display size.
     *
     * @return the display size of the application or {@code null} if unknown.
     */
    public RectangleSize getDisplaySize() {
        return displaySize;
    }

    /**
     * Sets display size.
     *
     * @param size -  the display size of the application or {@code null} if unknown.
     */
    public void setDisplaySize(RectangleSize size) {
        this.displaySize = size;
    }

    @Override
    public String toString() {
        return "[os = " + (os == null ? "?" : "'" + os + "'") + " hostingApp = "
                + (hostingApp == null ? "?" : "'" + hostingApp + "'")
                + " displaySize = " + displaySize + "]";
    }

    /**
     * Gets the device info (not part of test signature)
     *
     * @return the device info
     */
    public String getDeviceInfo() {
        return deviceInfo;
    }

    /**
     * Sets the device info (not part of test signature)
     *
     * @param deviceInfo the device info
     */
    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo =  deviceInfo != null  ? deviceInfo + " (Chrome emulation)" : "Desktop";
    }

    /**
     * Gets the device info (not part of test signature)
     *
     * @return the os info
     */
    public String getOsInfo() {
        return osInfo;
    }

    /**
     * Sets the os info (not part of test signature)
     *
     * @param osInfo the os info
     */
    public void setOsInfo(String osInfo) {
        this.osInfo = osInfo;
    }

    /**
     * Gets the hosting app info info (not part of test signature)
     *
     * @return the hosting app info
     */
    public String getHostingAppInfo() {
        return hostingAppInfo;
    }

    /**
     * Sets the hosting app info info (not part of test signature)
     *
     * @param hostingAppInfo the hosting app
     */
    public void setHostingAppInfo(String hostingAppInfo) {
        this.hostingAppInfo = hostingAppInfo;
    }
}