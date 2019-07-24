package com.applitools.eyes;

public interface IDownloadListener <T>{
        void onDownloadComplete(T downloadedString, String contentType);
        void onDownloadFailed();
}
