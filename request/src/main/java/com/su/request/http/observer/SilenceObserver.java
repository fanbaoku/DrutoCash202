package com.su.request.http.observer;

import android.annotation.SuppressLint;
import android.util.Log;

import com.su.request.BuildConfig;

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
public class SilenceObserver<T> implements Observer<T>, Disposable {
    private static final String TAG = SilenceObserver.class.getSimpleName();

    private final List<SilenceObserver<?>> mObservers;

    /**
     * copy from {@link io.reactivex.observers.DisposableObserver}
     **/
    final AtomicReference<Disposable> upstream = new AtomicReference<>();

    public SilenceObserver() {
        this(null);
    }

    public SilenceObserver(List<SilenceObserver<?>> observers) {
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
            Log.d(TAG, "onNext: " + t);
        }
    }

    @Override
    public void onError(Throwable e) {
        if (BuildConfig.DEBUG) {
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
