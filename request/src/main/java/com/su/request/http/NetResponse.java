package com.su.request.http;

/**
 * Created by su on 18-3-21.
 */

public class NetResponse<T> {

    private NetRequest<T> request;
    private T result;

    public NetResponse(NetRequest<T> request, T result) {
        this.request = request;
        this.result = result;
    }

    public NetRequest<T> getRequest() {
        return request;
    }

    public void setRequest(NetRequest<T> request) {
        this.request = request;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

}
