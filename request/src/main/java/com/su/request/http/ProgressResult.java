package com.su.request.http;

/**
 * Created by su on 18-3-22.
 */

public class ProgressResult<T, R> {
    private NetStatus type;
    private long bytes;
    private long contentLength;
    private R response;
    private NetRequest<T> request;

    public NetRequest<T> getRequest() {
        return request;
    }

    public void setRequest(NetRequest<T> request) {
        this.request = request;
    }

    public ProgressResult(NetStatus type) {
        this.type = type;
    }

    public R getResponse() {
        return response;
    }

    public void setResponse(R response) {
        this.response = response;
    }

    public NetStatus getType() {
        return type;
    }

    public void setType(NetStatus type) {
        this.type = type;
    }

    public long getBytes() {
        return bytes;
    }

    public void setBytes(long bytes) {
        this.bytes = bytes;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    @Override
    public String toString() {
        return "ProgressResult{" +
                "type=" + type +
                ", bytes=" + bytes +
                ", contentLength=" + contentLength +
                ", response=" + response +
                ", request=" + request +
                '}';
    }

    public enum NetStatus {
        REQUEST, RESPONSE, OVER
    }
}
