package com.applitools;

public interface IResourceUploadListener {
    void onUploadComplete(boolean isUploadedSuccessfully);
    void onUploadFailed();
}
