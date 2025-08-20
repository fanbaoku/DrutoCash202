package com.druto.loan.cash.drutocash.ui.base

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.webkit.WebView
import com.druto.loan.cash.drutocash.utils.ToastUtil.showToast

/**
 * https://issuetracker.google.com/issues/141132133
 * https://stackoverflow.com/questions/41025200/android-view-inflateexception-error-inflating-class-android-webkit-webview/49024931#49024931
 */
class LollipopFixedWebView : WebView {
    constructor(context: Context) : super(getFixedContext(context))

    constructor(context: Context, attrs: AttributeSet?) : super(
        getFixedContext(context), attrs
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        getFixedContext(context), attrs, defStyleAttr
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        getFixedContext(context), attrs, defStyleAttr, defStyleRes
    )

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        privateBrowsing: Boolean
    ) : super(
        getFixedContext(context), attrs, defStyleAttr, privateBrowsing
    )

    override fun setOverScrollMode(mode: Int) {
        try {
            super.setOverScrollMode(mode)
        } catch (e: Exception) {
            // try to avoid android.webkit.WebViewFactory$MissingWebViewPackageException
            // https://stackoverflow.com/questions/46048912/android-failed-to-load-webview-provider-no-webview-installed
            Log.w(TAG, e)
            if (e.message != null && e.message!!.contains("webview")) {
                post {
                    val context = context
                    showToast(context, "Something wrong with your WebView!")
                    if (context is Activity) {
                        context.finish()
                    }
                }
            } else {
                throw RuntimeException(e)
            }
        }
    }

    companion object {
        val TAG: String = LollipopFixedWebView::class.java.simpleName

        fun getFixedContext(context: Context): Context {
            return context.createConfigurationContext(Configuration())
        }
    }
}
