package com.su.request.http.observer;

import android.app.Activity;
import android.app.ProgressDialog;

import java.util.List;

import io.reactivex.disposables.Disposable;

/**
 * Created by su on 18-3-21.
 */

public class ProgressObserver<T> extends BaseObserver<T> {

    private Activity mActivity;
    private ProgressDialog mDialog;
    private boolean mCancelable;

    public ProgressObserver(List<BaseObserver<?>> observers, Activity activity) {
        this(observers, activity, true);
    }

    public ProgressObserver(List<BaseObserver<?>> observers, Activity activity, boolean cancelable) {
        super(observers);
        mActivity = activity;
        mCancelable = cancelable;
    }

    @Override
    public void onSubscribe(Disposable d) {
        super.onSubscribe(d);
        mDialog = new ProgressDialog(mActivity);
        mDialog.setCancelable(mCancelable);
        mDialog.show();
    }

    @Override
    public void onComplete() {
        super.onComplete();
        hideDialog();
    }

    @Override
    public void dispose() {
        super.dispose();
        hideDialog();
    }

    @Override
    public void onError(Throwable e) {
        super.onError(e);
        hideDialog();
    }

    private void hideDialog() {
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
}
