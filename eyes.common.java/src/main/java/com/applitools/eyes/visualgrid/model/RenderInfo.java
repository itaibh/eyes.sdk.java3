package com.applitools.eyes.visualgrid.model;

import com.applitools.eyes.Region;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class RenderInfo {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int width;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int height;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String sizeMode;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Region region;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private EmulationBaseInfo emulationInfo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VisualGridSelector selector;


    public RenderInfo(int width, int height, String sizeMode, Region region, VisualGridSelector selector, EmulationBaseInfo emulationInfo) {
        this.width = width;
        this.height = height;
        this.sizeMode = sizeMode;
        this.region = region;
        this.emulationInfo = emulationInfo;
        this.selector = selector;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getSizeMode() {
        return sizeMode;
    }

    public void setSizeMode(String sizeMode) {
        this.sizeMode = sizeMode;
    }

    @JsonProperty("region")
    public Map getRegion() {
        if (region == null) {
            return null;
        }
        HashMap<String, Integer> map = new HashMap<>();
        map.put("x", region.getLeft());
        map.put("y", region.getTop());
        map.put("width", region.getWidth());
        map.put("height", region.getHeight());
        return map;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public EmulationBaseInfo getEmulationInfo() {
        return emulationInfo;
    }

    public void setEmulationInfo(ChromeEmulationInfo emulationInfo) {
        this.emulationInfo = emulationInfo;
    }

    public VisualGridSelector getSelector() {
        return selector;
    }

    public void setSelector(VisualGridSelector selector) {
        this.selector = selector;
    }
}
