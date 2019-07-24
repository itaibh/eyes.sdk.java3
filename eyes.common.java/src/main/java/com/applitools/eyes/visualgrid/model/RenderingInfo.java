package com.applitools.eyes.visualgrid.model;

public class RenderingInfo {

    private String serviceUrl = null;
    private String accessToken = null;
    private String resultsUrl = null;

    public RenderingInfo(String serviceUrl, String accessToken, String resultsUrl) {
        this.serviceUrl = serviceUrl;
        this.accessToken = accessToken;
        this.resultsUrl = resultsUrl;
    }

    public RenderingInfo() {
    }

    public String getServiceUrl() {
        return serviceUrl;
    }


    public String getAccessToken() {
        return accessToken;
    }


    public String getResultsUrl() {
        return resultsUrl;
    }

}
