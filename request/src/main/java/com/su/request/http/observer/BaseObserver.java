package com.su.request.http.observer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.su.request.BuildConfig;
import com.su.request.RequestBase;
import com.su.request.http.NetException;
import com.su.request.http.ProgressResult;
import com.su.request.http.RequestLifecycleListener;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableHelper;
import io.reactivex.internal.util.EndConsumerHelper;

/**
 * Created by su on 18-3-21.
 */

@SuppressLint("StaticFieldLeak")
public class BaseObserver<T> implements Observer<T>, Disposable {
    private static final String TAG = BaseObserver.class.getSimpleName();
    private static final Toast sFailureToast = Toast.makeText(RequestBase.context, "Internet Error, please internet settings", Toast.LENGTH_LONG);
    private static final Toast sErrorToast = Toast.makeText(RequestBase.context, "Internet Error, please retry", Toast.LENGTH_LONG);
    private static final Toast sParseErrorToast = Toast.makeText(RequestBase.context, "", Toast.LENGTH_LONG);
    private static final Toast sRequestErrorToast = Toast.makeText(RequestBase.context, "Request params are not illegal", Toast.LENGTH_LONG);

    private final List<BaseObserver<?>> mObservers;

    /**
     * copy from {@link io.reactivex.observers.DisposableObserver}
     **/
    final AtomicReference<Disposable> upstream = new AtomicReference<>();

    public BaseObserver(List<BaseObserver<?>> observers) {
        this.mObservers = observers;
    }

    @Override
    public void onSubscribe(Disposable d) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onSubscribe: " + d);
        }
        EndConsumerHelper.setOnce(this.upstream, d, getClass());
        if (mObservers != null) {
            mObservers.add(this);
        }
    }

    @Override
    public void onNext(T t) {
        if (BuildConfig.DEBUG) {
            if (t instanceof ProgressResult) {
                ProgressResult<?, ?> result = (ProgressResult<?, ?>) t;
                if (result.getType() == ProgressResult.NetStatus.OVER) {
                    Log.d(TAG, "onNext: " + t);
                }
            } else {
                Log.d(TAG, "onNext: " + t);
            }
        }
    }

    @Override
    public void onError(Throwable e) {
        if (e instanceof NetException) {
            processException((NetException) e);
        } else {
            if (observerToastable() && RequestLifecycleListener.Companion.shouldToast()) {
                sErrorToast.show();
            } else {
                Log.w(TAG, "ignore error toast");
            }
        }

        if (BuildConfig.DEBUG) {
            Toast.makeText(RequestBase.context, "DEBUG: " + e.toString(), Toast.LENGTH_LONG).show();
            Log.w(TAG, "onError", e);
        }

        if (mObservers != null) {
            mObservers.remove(this);
        }
    }

    @Override
    public void onComplete() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onComplete");
        }
        if (mObservers != null) {
            mObservers.remove(this);
        }
    }

    private void processException(NetException e) {
        switch (e.getErrorCode()) {
            case NetException.NETWORK:
                if (observerToastable() && RequestLifecycleListener.Companion.shouldToast()) {
                    if (isNetworkAvailable()) {
                        sErrorToast.show();
                    } else {
                        sFailureToast.show();
                    }
                } else {
                    Log.w(TAG, "ignore network toast");
                }
                break;
            case NetException.PARSER:
                if (observerToastable() && RequestLifecycleListener.Companion.shouldToast()) {
                    sParseErrorToast.setText("parse failed\n" + "url: " + e.getUrl());
                    sParseErrorToast.show();
                } else {
                    Log.w(TAG, "ignore parse toast");
                }
                break;
            case NetException.BUILD_REQUEST:
                if (observerToastable() && RequestLifecycleListener.Companion.shouldToast()) {
                    sRequestErrorToast.show();
                } else {
                    Log.w(TAG, "ignore error toast");
                }
                break;
            default:
                break;
        }
    }

    // 网络错误时，是否弹toast
    public boolean observerToastable() {
        return true;
    }

    /**
     * 判断当前网络是否可用
     */
    private static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) RequestBase.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable();
    }

    @Override
    public void dispose() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "dispose");
        }
        DisposableHelper.dispose(upstream);
    }

    @Override
    public boolean isDisposed() {
        return upstream.get() == DisposableHelper.DISPOSED;
    }
}
