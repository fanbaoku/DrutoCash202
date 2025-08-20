package com.su.request.http.function;

import android.view.Gravity;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.su.request.RequestBase;
import com.su.request.http.HttpResult;
import com.su.request.http.NetException;
import com.su.request.http.NetResponse;

import io.reactivex.functions.Function;

/**
 * Created by su on 18-3-21.
 */

public abstract class BusinessFunction<T, E> implements Function<NetResponse<T>, E> {
    private boolean mDowntime;
    private int mErrorCode;
    private String mErrorMessage = "";

    private NetResponse<T> mResponse;

    @Override
    public E apply(NetResponse<T> response) throws Exception {
        mResponse = response;
        getErrorCode(response.getResult());
        mDowntime = mErrorCode == 699;
        getErrorMessage(response.getResult());
        processErrorCode(mResponse);
        return onSuccess(response.getResult());
    }

    public abstract E onSuccess(T httpResult) throws Exception;

    private void getErrorCode(T response) {
        if (response instanceof HttpResult) {
            HttpResult<?> httpResult = (HttpResult<?>) response;
            mErrorCode = httpResult.getTotal();
        } else if (response instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) response;
            JsonElement codeElement = jsonObject.get("code");
            if (codeElement != null) {
                mErrorCode = jsonObject.get("code").getAsInt();
            }
        }
    }

    protected int getErrorCode() {
        return mErrorCode;
    }

    private void getErrorMessage(T response) {
        if (response instanceof HttpResult) {
            HttpResult<?> httpResult = (HttpResult<?>) response;
            mErrorMessage = httpResult.getSwell();
        } else if (response instanceof JsonObject) {
            JsonObject jsonObject = (JsonObject) response;
            JsonElement msgElement = jsonObject.get("msg");
            if (msgElement != null) {
                mErrorMessage = jsonObject.get("msg").getAsString();
            }
        }
    }

    protected String getErrorMessage() {
        return mErrorMessage;
    }

    protected void processErrorCode(NetResponse<T> response) {
        if (isDowntime()) {
            throw new NetException(NetException.DOWNTIME, response.getRequest().getUrl(), "");
        }
    }

    protected void toastErrorMessage(int gravity) {
        if (!isDowntime() && mErrorCode != 302) {
            Toast toast = Toast.makeText(RequestBase.context, "", Toast.LENGTH_LONG);
            toast.setGravity(gravity, 0, 0);
            toast.setText(mErrorMessage);
            toast.show();
        }
    }

    protected NetResponse<T> getResponse() {
        return mResponse;
    }

    protected void toastErrorMessage() {
        toastErrorMessage(Gravity.CENTER);
    }

    protected boolean isDowntime() {
        return mDowntime;
    }
}
