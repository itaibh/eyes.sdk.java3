/*
 * Applitools SDK for Selenium integration.
 */
package com.applitools.eyes;

import com.applitools.eyes.capture.AppOutputProvider;
import com.applitools.eyes.capture.AppOutputWithScreenshot;
import com.applitools.eyes.config.IConfigurationGetter;
import com.applitools.eyes.fluent.GetFloatingRegion;
import com.applitools.eyes.fluent.GetRegion;
import com.applitools.eyes.fluent.ICheckSettingsInternal;
import com.applitools.eyes.visualgrid.model.IGetFloatingRegionOffsets;
import com.applitools.eyes.visualgrid.model.MutableRegion;
import com.applitools.eyes.visualgrid.model.VisualGridSelector;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.GeneralUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MatchWindowTask {

    private static final int MATCH_INTERVAL = 500; // Milliseconds
    private EyesScreenshot lastScreenshot = null;
    private Region lastScreenshotBounds;
    private int defaultRetryTimeout;

    protected Logger logger;
    protected IServerConnector serverConnector;
    protected RunningSession runningSession;
    protected AppOutputProvider appOutputProvider;
    protected MatchResult matchResult;
    protected EyesBase eyes;

    protected MatchWindowTask() {
    }

    /**
     * @param logger            A logger instance.
     * @param serverConnector   Our gateway to the agent
     * @param runningSession    The running session in which we should match the window
     * @param retryTimeout      The default total time to retry matching (ms).
     * @param eyes              An EyesBase object.
     * @param appOutputProvider A callback for getting the application output when performing match.
     */
    public MatchWindowTask(Logger logger, IServerConnector serverConnector,
                           RunningSession runningSession, int retryTimeout,
                           EyesBase eyes, AppOutputProvider appOutputProvider) {
        ArgumentGuard.notNull(serverConnector, "serverConnector");
        ArgumentGuard.notNull(runningSession, "runningSession");
        ArgumentGuard.greaterThanOrEqualToZero(retryTimeout, "retryTimeout");
        ArgumentGuard.notNull(appOutputProvider, "appOutputProvider");

        this.logger = logger;
        this.serverConnector = serverConnector;
        this.runningSession = runningSession;
        this.defaultRetryTimeout = retryTimeout;
        this.eyes = eyes;
        this.appOutputProvider = appOutputProvider;
    }

    /**
     * @param logger          A logger instance.
     * @param serverConnector Our gateway to the agent
     * @param runningSession  The running session in which we should match the window
     * @param retryTimeout    The default total time to retry matching (ms).
     */
    public MatchWindowTask(Logger logger, IServerConnector serverConnector,
                           RunningSession runningSession, int retryTimeout,
                           EyesBase eyes) {
        ArgumentGuard.notNull(serverConnector, "serverConnector");
        ArgumentGuard.notNull(runningSession, "runningSession");
        ArgumentGuard.greaterThanOrEqualToZero(retryTimeout, "retryTimeout");

        this.logger = logger;
        this.serverConnector = serverConnector;
        this.runningSession = runningSession;
        this.defaultRetryTimeout = retryTimeout;
        this.eyes = eyes;
        this.appOutputProvider = null;
    }

    /**
     * Creates the match model and calls the server connector matchWindow method.
     *
     * @param userInputs         The user inputs related to the current appOutput.
     * @param appOutput          The application output to be matched.
     * @param tag                Optional tag to be associated with the match (can be {@code null}).
     * @param ignoreMismatch     Whether to instruct the server to ignore the match attempt in case of a mismatch.
     * @param imageMatchSettings The settings to use.
     * @return The match result.
     */
    public MatchResult performMatch(List<Trigger> userInputs,
                                    AppOutputWithScreenshot appOutput,
                                    String tag, boolean ignoreMismatch,
                                    ICheckSettingsInternal checkSettingsInternal,
                                    ImageMatchSettings imageMatchSettings,
                                    EyesBase eyes, String source)
    {
        EyesScreenshot screenshot = appOutput.getScreenshot(checkSettingsInternal);

        collectSimpleRegions(checkSettingsInternal, imageMatchSettings, eyes, screenshot);
        collectFloatingRegions(checkSettingsInternal, imageMatchSettings, eyes, screenshot);

        String agentSetupStr = "";
        if (eyes != null) {
            Object agentSetup = eyes.getAgentSetup();
            ObjectMapper jsonMapper = new ObjectMapper();
            try {
                agentSetupStr = jsonMapper.writeValueAsString(agentSetup);
            } catch (JsonProcessingException e) {
                GeneralUtils.logExceptionStackTrace(logger, e);
            }

        }
        return performMatch(userInputs, appOutput, tag, ignoreMismatch, imageMatchSettings, agentSetupStr,
                null, source);
    }

    /**
     * Creates the match model and calls the server connector matchWindow method.
     *
     * @param appOutput          The application output to be matched.
     * @param tag                Optional tag to be associated with the match (can be {@code null}).
     * @param ignoreMismatch     Whether to instruct the server to ignore the match attempt in case of a mismatch.
     * @param imageMatchSettings The settings to use.
     * @param renderId           Visual Grid's renderId.
     * @param source             The tested page URL or tested app name.
     * @return The match result.
     */
    public MatchResult performMatch(AppOutputWithScreenshot appOutput,
                                    String tag, boolean ignoreMismatch,
                                    ICheckSettingsInternal checkSettingsInternal,
                                    ImageMatchSettings imageMatchSettings,
                                    List<? extends IRegion> regions,
                                    List<VisualGridSelector[]> regionSelectors,
                                    EyesBase eyes, String renderId, String source)
    {
        EyesScreenshot screenshot = appOutput.getScreenshot(checkSettingsInternal);
        String agentSetupStr = (String) eyes.getAgentSetup();

        collectRegions(imageMatchSettings, regions, regionSelectors);

        return performMatch(new ArrayList<Trigger>(), appOutput, tag, ignoreMismatch, imageMatchSettings, agentSetupStr,
                renderId, source);
    }

    private MatchResult performMatch(List<Trigger> userInputs,
                                     AppOutputWithScreenshot appOutput,
                                     String tag, boolean ignoreMismatch,
                                     ImageMatchSettings imageMatchSettings,
                                     String agentSetupStr, String renderId,
                                     String source)
    {
        // Prepare match data.
        MatchWindowData.Options options = new MatchWindowData.Options(tag, userInputs.toArray(new Trigger[0]),
                ignoreMismatch, false, false, false, imageMatchSettings, source, renderId);

        MatchWindowData data = new MatchWindowData(userInputs.toArray(new Trigger[0]),appOutput.getAppOutput(), tag,
                ignoreMismatch, options, agentSetupStr, renderId);


        // Perform match.
        return serverConnector.matchWindow(runningSession, data);
    }

    private static void collectRegions(ImageMatchSettings imageMatchSettings, List<? extends IRegion> regions, List<VisualGridSelector[]> regionSelectors) {
        if (regions == null) return;

        int currentCounter = 0;
        int currentTypeIndex = 0;
        int currentTypeRegionCount = regionSelectors.get(0).length;

        List<List<MutableRegion>> mutableRegions = new ArrayList<>();
        mutableRegions.add(new ArrayList<MutableRegion>()); // Ignore Regions
        mutableRegions.add(new ArrayList<MutableRegion>()); // Layout Regions
        mutableRegions.add(new ArrayList<MutableRegion>()); // Strict Regions
        mutableRegions.add(new ArrayList<MutableRegion>()); // Content Regions
        mutableRegions.add(new ArrayList<MutableRegion>()); // Floating Regions
        mutableRegions.add(new ArrayList<MutableRegion>()); // Target Element Location

        for (IRegion region : regions) {
            boolean canAddRegion = false;
            while (!canAddRegion) {
                currentCounter++;
                if (currentCounter > currentTypeRegionCount) {
                    currentTypeIndex++;
                    currentTypeRegionCount = regionSelectors.get(currentTypeIndex).length;
                    currentCounter = 0;
                } else {
                    canAddRegion = true;
                }
            }
            MutableRegion mr = new MutableRegion(region);
            mutableRegions.get(currentTypeIndex).add(mr);
        }

        Location location = Location.ZERO;

        // If target element location available
        if (mutableRegions.get(5).size() > 0)
        {
            location = mutableRegions.get(5).get(0).getLocation();
        }

        imageMatchSettings.setIgnoreRegions(filterEmptyEntries(mutableRegions.get(0), location));
        imageMatchSettings.setLayoutRegions(filterEmptyEntries(mutableRegions.get(1), location));
        imageMatchSettings.setStrictRegions(filterEmptyEntries(mutableRegions.get(2), location));
        imageMatchSettings.setContentRegions(filterEmptyEntries(mutableRegions.get(3), location));

        List<FloatingMatchSettings> floatingMatchSettings = new ArrayList<>();
        for (int i = 0; i < regionSelectors.get(4).length; i++) {
            MutableRegion mr = mutableRegions.get(4).get(i);
            if (mr.getArea() == 0) continue;
            VisualGridSelector vgs = regionSelectors.get(4)[i];

            if (vgs.getCategory() instanceof IGetFloatingRegionOffsets) {
                IGetFloatingRegionOffsets gfr = (IGetFloatingRegionOffsets) vgs.getCategory();
                FloatingMatchSettings fms = new FloatingMatchSettings(
                        mr.getLeft(),
                        mr.getTop(),
                        mr.getWidth(),
                        mr.getHeight(),
                        gfr.getMaxUpOffset(),
                        gfr.getMaxDownOffset(),
                        gfr.getMaxLeftOffset(),
                        gfr.getMaxRightOffset()
                );
                floatingMatchSettings.add(fms);
            }
        }
        imageMatchSettings.setFloatingRegions(floatingMatchSettings.toArray(new FloatingMatchSettings[0]));
    }

    private static MutableRegion[] filterEmptyEntries(List<MutableRegion> list, Location location) {
        for (int i = list.size() - 1; i >= 0; i--)
        {
            MutableRegion mutableRegion = list.get(i);
            if (mutableRegion.getArea() == 0){
                list.remove(i);
            }
            else{
                mutableRegion.offset(-location.getX(), -location.getY());
            }
        }
        return list.toArray(new MutableRegion[0]);
    }


    private static void collectSimpleRegions(ICheckSettingsInternal checkSettingsInternal,
                                              ImageMatchSettings imageMatchSettings, EyesBase eyes,
                                              EyesScreenshot screenshot) {
        imageMatchSettings.setIgnoreRegions(collectSimpleRegions(eyes, screenshot, checkSettingsInternal.getIgnoreRegions()));
        imageMatchSettings.setStrictRegions(collectSimpleRegions(eyes, screenshot, checkSettingsInternal.getStrictRegions()));
        imageMatchSettings.setLayoutRegions(collectSimpleRegions(eyes, screenshot, checkSettingsInternal.getLayoutRegions()));
        imageMatchSettings.setContentRegions(collectSimpleRegions(eyes, screenshot, checkSettingsInternal.getContentRegions()));
    }

    private static Region[] collectSimpleRegions(EyesBase eyes,
                                                           EyesScreenshot screenshot, GetRegion[] regionProviders) {
        List<List<Region>> mutableRegions = new ArrayList<>();
        for (GetRegion regionProvider : regionProviders) {
            mutableRegions.add(regionProvider.getRegions(eyes, screenshot));
        }

        List<Region> allRegions = new ArrayList<>();
        for (List<Region> mutableRegion : mutableRegions) {
            allRegions.addAll(mutableRegion);
        }
        return allRegions.toArray(new Region[0]);
    }

    private static void collectFloatingRegions(ICheckSettingsInternal checkSettingsInternal,
                                               ImageMatchSettings imageMatchSettings, EyesBase eyes,
                                               EyesScreenshot screenshot) {
        List<FloatingMatchSettings> floatingRegions = new ArrayList<>();
        for (GetFloatingRegion floatingRegion : checkSettingsInternal.getFloatingRegions()) {
            List<FloatingMatchSettings> regions = floatingRegion.getRegions(eyes, screenshot);
            floatingRegions.addAll(regions);
        }
        imageMatchSettings.setFloatingRegions(floatingRegions.toArray(new FloatingMatchSettings[0]));

    }

    /**
     * Repeatedly obtains an application snapshot and matches it with the next
     * expected output, until a match is found or the timeout expires.
     *
     * @param userInputs             User input preceding this match.
     * @param region                 Window region to capture.
     * @param tag                    Optional tag to be associated with the match (can be {@code null}).
     * @param shouldRunOnceOnTimeout Force a single match attempt at the end of the match timeout.
     * @param ignoreMismatch         Whether to instruct the server to ignore the match attempt in case of a mismatch.
     * @param checkSettingsInternal  The settings to use.
     * @param retryTimeout           The amount of time to retry matching in milliseconds or a
     *                               negative value to use the default retry timeout.
     * @return Returns the results of the match
     */
    public MatchResult matchWindow(Trigger[] userInputs,
                                   Region region, String tag,
                                   boolean shouldRunOnceOnTimeout,
                                   boolean ignoreMismatch,
                                   ICheckSettingsInternal checkSettingsInternal,
                                   int retryTimeout,
                                   String source) {

        if (retryTimeout < 0) {
            retryTimeout = defaultRetryTimeout;
        }

        logger.verbose(String.format("retryTimeout = %d", retryTimeout));

        EyesScreenshot screenshot = takeScreenshot(userInputs, region, tag,
                shouldRunOnceOnTimeout, ignoreMismatch, checkSettingsInternal, retryTimeout, source);

        if (ignoreMismatch) {
            return matchResult;
        }

        updateLastScreenshot(screenshot);
        updateBounds(region);

        return matchResult;
    }

    private void collectSimpleRegions(ICheckSettingsInternal checkSettingsInternal,
                                      ImageMatchSettings imageMatchSettings,
                                      EyesScreenshot screenshot) {

        imageMatchSettings.setIgnoreRegions(collectRegions(checkSettingsInternal.getIgnoreRegions(), screenshot, false));
        imageMatchSettings.setLayoutRegions(collectRegions(checkSettingsInternal.getLayoutRegions(), screenshot, true));
        imageMatchSettings.setStrictRegions(collectRegions(checkSettingsInternal.getStrictRegions(), screenshot, true));
        imageMatchSettings.setContentRegions(collectRegions(checkSettingsInternal.getContentRegions(), screenshot, true));
    }

    private Region[] collectRegions(GetRegion[] regionProviders, EyesScreenshot screenshot, boolean adjustLocation) {

        List<Region> regions = new ArrayList<>();
        for (GetRegion regionProvider : regionProviders) {
            try {
                regions.addAll(regionProvider.getRegions(eyes, screenshot));
            } catch (OutOfBoundsException ex) {
                logger.log("WARNING - region was out of bounds.");
            }
        }
        return regions.toArray(new Region[0]);
    }

    /**
     * Build match settings by merging the check settings and the default match settings.
     *
     * @param checkSettingsInternal the settings to match the image by.
     * @param screenshot            the Screenshot wrapper object.
     * @return Merged match settings.
     */
    public ImageMatchSettings createImageMatchSettings(ICheckSettingsInternal checkSettingsInternal, EyesScreenshot screenshot, EyesBase eyesBase) {
        ImageMatchSettings imageMatchSettings = null;
        if (checkSettingsInternal != null) {

            MatchLevel matchLevel = checkSettingsInternal.getMatchLevel();
            IConfigurationGetter configGetter = eyes.getConfigGetter();
            if (matchLevel == null) {
                matchLevel = configGetter.getDefaultMatchSettings().getMatchLevel();
            }

            imageMatchSettings = new ImageMatchSettings(matchLevel, null, false);

            Boolean ignoreCaret = checkSettingsInternal.getIgnoreCaret();
            if (ignoreCaret == null) {
                ignoreCaret = configGetter.getDefaultMatchSettings().getIgnoreCaret();
            }

            imageMatchSettings.setIgnoreCaret(ignoreCaret);

            collectSimpleRegions(checkSettingsInternal, imageMatchSettings, screenshot);
            collectFloatingRegions(checkSettingsInternal, imageMatchSettings, eyesBase, screenshot);
            imageMatchSettings.setEnablePatterns(checkSettingsInternal.isEnablePatterns());
            imageMatchSettings.setUseDom(checkSettingsInternal.isUseDom());
            imageMatchSettings.setIgnoreDisplacements(checkSettingsInternal.isIgnoreDisplacements() != null ? checkSettingsInternal.isIgnoreDisplacements() : configGetter.getIgnoreDisplacements());
        }
        return imageMatchSettings;
    }
    /**
     * Build match settings by merging the check settings and the default match settings.
     *
     * @param checkSettingsInternal the settings to match the image by.
     * @return Merged match settings.
     */
    public static ImageMatchSettings createImageMatchSettings(ICheckSettingsInternal checkSettingsInternal, EyesBase eyes)
    {
        ImageMatchSettings imageMatchSettings = null;
        if (checkSettingsInternal != null)
        {
            MatchLevel matchLevel = checkSettingsInternal.getMatchLevel() != null ? checkSettingsInternal.getMatchLevel() : eyes.getConfigGetter().getDefaultMatchSettings().getMatchLevel();
            imageMatchSettings = new ImageMatchSettings(matchLevel, null, checkSettingsInternal.isUseDom() != null ? checkSettingsInternal.isUseDom() : false );
            imageMatchSettings.setIgnoreCaret(checkSettingsInternal.getIgnoreCaret()!= null ? checkSettingsInternal.getIgnoreCaret() : eyes.getConfigGetter().getIgnoreCaret());
            imageMatchSettings.setEnablePatterns(checkSettingsInternal.isEnablePatterns());
            imageMatchSettings.setIgnoreDisplacements(checkSettingsInternal.isIgnoreDisplacements() != null ? checkSettingsInternal.isIgnoreDisplacements() : eyes.getConfigGetter().getIgnoreDisplacements() );
        }
        return imageMatchSettings;
    }

    private EyesScreenshot takeScreenshot(Trigger[] userInputs, Region region, String tag,
                                          boolean shouldMatchWindowRunOnceOnTimeout,
                                          boolean ignoreMismatch, ICheckSettingsInternal checkSettingsInternal,
                                          int retryTimeout, String source) {
        long elapsedTimeStart = System.currentTimeMillis();
        EyesScreenshot screenshot;

        // If the wait to load time is 0, or "run once" is true,
        // we perform a single check window.
        if (0 == retryTimeout || shouldMatchWindowRunOnceOnTimeout) {

            if (shouldMatchWindowRunOnceOnTimeout) {
                GeneralUtils.sleep(retryTimeout);
            }
            screenshot = tryTakeScreenshot(userInputs, region, tag, ignoreMismatch, checkSettingsInternal, source);
        } else {
            screenshot = retryTakingScreenshot(userInputs, region, tag, ignoreMismatch, checkSettingsInternal,
                    retryTimeout, source);
        }

        double elapsedTime = (System.currentTimeMillis() - elapsedTimeStart) / 1000;
        logger.verbose(String.format("Completed in %.2f seconds", elapsedTime));
        //matchResult.setScreenshot(screenshot);
        return screenshot;
    }

    private EyesScreenshot retryTakingScreenshot(Trigger[] userInputs, Region region, String tag, boolean ignoreMismatch,
                                                 ICheckSettingsInternal checkSettingsInternal, int retryTimeout,
                                                 String source) {
        // Start the retry timer.
        long start = System.currentTimeMillis();

        EyesScreenshot screenshot = null;

        long retry = System.currentTimeMillis() - start;

        // The match retry loop.
        while (retry < retryTimeout) {

            // Wait before trying again.
            GeneralUtils.sleep(MATCH_INTERVAL);

            screenshot = tryTakeScreenshot(userInputs, region, tag, true, checkSettingsInternal, source);

            if (matchResult.getAsExpected()) {
                break;
            }

            retry = System.currentTimeMillis() - start;
        }

        // if we're here because we haven't found a match yet, try once more
        if (!matchResult.getAsExpected()) {
            screenshot = tryTakeScreenshot(userInputs, region, tag, ignoreMismatch, checkSettingsInternal, source);
        }
        return screenshot;
    }

    private EyesScreenshot tryTakeScreenshot(Trigger[] userInputs, Region region, String tag,
                                             boolean ignoreMismatch, ICheckSettingsInternal checkSettingsInternal,
                                             String source) {
        AppOutputWithScreenshot appOutput = appOutputProvider.getAppOutput(region, lastScreenshot, checkSettingsInternal);
        EyesScreenshot screenshot = appOutput.getScreenshot(checkSettingsInternal);
        ImageMatchSettings matchSettings = createImageMatchSettings(checkSettingsInternal, screenshot, eyes);
        matchResult = performMatch(Arrays.asList(userInputs), appOutput, tag, ignoreMismatch, checkSettingsInternal,
                matchSettings, eyes, source);
        return screenshot;
    }

    private void updateLastScreenshot(EyesScreenshot screenshot) {
        if (screenshot != null) {
            lastScreenshot = screenshot;
        }
    }

    private void updateBounds(Region region) {
        if (region.isSizeEmpty()) {
            if (lastScreenshot == null) {
                // We set an "infinite" image size since we don't know what the screenshot size is...
                lastScreenshotBounds = new Region(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
            } else {
                BufferedImage image = lastScreenshot.getImage();
                lastScreenshotBounds = new Region(0, 0, image.getWidth(), image.getHeight());
            }
        } else {
            lastScreenshotBounds = region;
        }
    }

    public Region getLastScreenshotBounds() {
        return lastScreenshotBounds;
    }

}
