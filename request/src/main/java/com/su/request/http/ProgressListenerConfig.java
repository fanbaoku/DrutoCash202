package com.su.request.http;


import androidx.annotation.Nullable;

/**
 * Created by su on 18-3-22.
 */

public class ProgressListenerConfig {

    private ProgressListener listener;
    private boolean progressRequest;
    private boolean progressResponse;

    ProgressListenerConfig(@Nullable ProgressListener listener, boolean progressRequest, boolean progressResponse) {
        this.listener = listener;
        this.progressRequest = progressRequest;
        this.progressResponse = progressResponse;
    }

    public ProgressListener getListener() {
        return listener;
    }

    public void setListener(ProgressListener listener) {
        this.listener = listener;
    }

    public boolean isProgressRequest() {
        return progressRequest;
    }

    public void setProgressRequest(boolean progressRequest) {
        this.progressRequest = progressRequest;
    }

    public boolean isProgressResponse() {
        return progressResponse;
    }

    public void setProgressResponse(boolean progressResponse) {
        this.progressResponse = progressResponse;
    }

    @Override
    public String toString() {
        return "ProgressListenerConfig{" +
                "listener=" + listener +
                ", progressRequest=" + progressRequest +
                ", progressResponse=" + progressResponse +
                '}';
    }
}
