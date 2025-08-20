package com.su.request.http.function

import com.su.request.http.ProgressResult
import java.io.File

import io.reactivex.functions.Consumer
import okhttp3.Response

abstract class ProgressConsumer : Consumer<ProgressResult<File, Response>> {
    protected var start: Boolean = false
    protected var lastProgress: Float = 0.toFloat()
}
