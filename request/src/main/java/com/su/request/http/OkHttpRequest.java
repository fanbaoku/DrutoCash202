package com.su.request.http;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.su.request.BuildConfig;
import com.su.request.RequestBase;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import okhttp3.CacheControl;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpMethod;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

/**
 * Created by su on 18-3-22.
 */

public class OkHttpRequest<T> implements ProgressListener {

    private static final String TAG = OkHttpRequest.class.getSimpleName();
    private static final MediaType CONTENT_TYPE = MediaType.get(
            "application/x-www-form-urlencoded");

    protected NetRequest<T> request;
    private ObservableEmitter<ProgressResult<T, Response>> oe;

    OkHttpRequest(NetRequest<T> request) {
        this.request = request;
    }

    private Headers createHeaders() {
        Headers.Builder builder = new Headers.Builder();
        Set<Map.Entry<String, String>> entrySet = request.getHeaderMap().entrySet();
        Iterator<Map.Entry<String, String>> it = entrySet.iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String value = entry.getValue();
            if (value == null) {
                Log.w(TAG, "header " + entry.getKey() + " is null");
            } else {
                builder.addUnsafeNonAscii(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    private RequestBody createFormBody() {
        FormBody.Builder builder = new FormBody.Builder();
        Set<Map.Entry<String, Object>> entrySet = request.getFormBodyMap().entrySet();
        Iterator<Map.Entry<String, Object>> it = entrySet.iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            Object value = entry.getValue();
            if (value instanceof String) {
                builder.add(entry.getKey(), (String) value);
            } else {
                builder.add(entry.getKey(), new Gson().toJson(value));
            }
        }
        return builder.build();
    }

    private RequestBody createMultipartBody(@NonNull MediaType type) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(type);
        Set<Map.Entry<String, Object>> entrySet = request.getFormBodyMap().entrySet();
        for (Map.Entry<String, Object> entry : entrySet) {
            Object value = entry.getValue();
            if (value instanceof String) {
                builder.addFormDataPart(entry.getKey(), (String) value);
            } else {
                builder.addFormDataPart(entry.getKey(), new Gson().toJson(value));
            }
        }

        Set<Map.Entry<String, MultipartFile>> fileSet = request.getMultipartMap().entrySet();
        for (Map.Entry<String, MultipartFile> entry : fileSet) {
            MultipartFile multipartFile = entry.getValue();
            File file = multipartFile.getFile();
            if (file != null && file.exists() && file.isFile()) {
                builder.addFormDataPart(multipartFile.getName(), multipartFile.getFileName(),
                        RequestBody.create(MediaType.parse(multipartFile.getMimeType()), file));
            } else {
                Toast.makeText(RequestBase.context, "file error: " + multipartFile,
                        Toast.LENGTH_LONG).show();
            }
        }
        return builder.build();
    }

    private RequestBody createBody(MediaType type) {
        if (!request.getMultipartMap().isEmpty()) {
            return createMultipartBody(type);
        }
        return RequestBody.create(type, new Gson().toJson(request.getFormBodyMap()));
    }

    private Request buildRequest() {
        request.checkParamsIsNull();
        Request.Builder builder = new Request.Builder()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .headers(createHeaders());
        RequestBody body = null;
        if (HttpMethod.requiresRequestBody(request.getMethod())) {
            if (TextUtils.isEmpty(request.getMediaType()) || CONTENT_TYPE.equals(
                    MediaType.parse(request.getMediaType()))) {
                body = createFormBody();
            } else {
                body = createBody(MediaType.parse(request.getMediaType()));
            }
            builder.url(request.getUrl());
        } else {
            builder.url(buildUrl());
        }
        builder.method(request.getMethod(), body);
        builder.tag(ProgressListenerConfig.class,
                new ProgressListenerConfig(this, request.isProgressRequest(),
                        request.isProgressResponse()));
        return builder.build();
    }

    private String buildUrl() {
        Uri old = Uri.parse(request.getUrl());
        int parametersSize = old.getQueryParameterNames().size();
        Set<Map.Entry<String, Object>> entrySet = request.getFormBodyMap().entrySet();
        Iterator<Map.Entry<String, Object>> it = entrySet.iterator();
        StringBuilder parameters = new StringBuilder();
        int count = 0;
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }

            String prefix;
            if (parametersSize == 0 && count == 0) {
                prefix = "?";
            } else {
                prefix = "&";
            }
            if (value instanceof String) {
                parameters.append(prefix + entry.getKey() + "=" + value);
            } else {
                parameters.append(prefix + entry.getKey() + "=" + new Gson().toJson(value));
            }
            count++;
        }
        return old.toString() + parameters;
    }

    private Response execute() throws IOException {
        Request request = buildRequest();
        OkHttpClient client;
        if (this.request.isProgressResponse()) {
            client = OkHttpClientHelper.getOkHttpDownloadInstance();
        } else {
            client = OkHttpClientHelper.getOkHttpInstance();
        }
        return client.newCall(request).execute();
    }

    private Response execute(OkHttpClient client, RequestBody requestBody) throws IOException {
        request.checkParamsIsNull();
        Request.Builder builder = new Request.Builder()
                .cacheControl(CacheControl.FORCE_NETWORK)
                .headers(createHeaders());
        if (HttpMethod.requiresRequestBody(request.getMethod())) {
            builder.url(request.getUrl());
        } else {
            builder.url(buildUrl());
        }
        builder.method(request.getMethod(), requestBody);
        builder.tag(ProgressListenerConfig.class,
                new ProgressListenerConfig(this, request.isProgressRequest(),
                        request.isProgressResponse()));
        Request request = builder.build();
        return client.newCall(request).execute();
    }

    private void download(Response response) {
        ResponseBody body = response.body();
        long contentLength = body.contentLength();
        if (contentLength == -1L) {
            if (oe != null && !oe.isDisposed()) {
                oe.onError(new IllegalStateException(
                        "download error. url: " + response.request().url()));
            }
            return;
        }

        BufferedSource source = body.source();
        BufferedSink sink = null;
        try {
            sink = Okio.buffer(Okio.sink(new File(request.getDownloadFilepath())));
            Buffer sinkBuffer = sink.buffer();

            long totalBytesRead = 0;
            int bufferSize = 8 * 1024;
            for (long bytesRead; (bytesRead = source.read(sinkBuffer, bufferSize)) != -1; ) {
                sink.emit();
                totalBytesRead += bytesRead;
                onProgress(totalBytesRead, contentLength, ProgressResult.NetStatus.RESPONSE);
            }
        } catch (IOException e) {
            Log.w(TAG, e);
        } finally {
            try {
                if (sink != null) {
                    sink.flush();
                    sink.close();
                }
                source.close();
            } catch (IOException e) {
                Log.w(TAG, "close download", e);
            }
        }
    }

    public Observable<ProgressResult<T, Response>> build() {
        String url = request.getUrl();
        if (TextUtils.isEmpty(url) || !URLUtil.isNetworkUrl(url)) {
            Toast.makeText(RequestBase.context, "ERROR URL: " + url, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Url error: " + url, new Throwable());
            return Observable.empty();
        }
        return Observable
                .create(new ObservableOnSubscribe<ProgressResult<T, Response>>() {
                    @Override
                    public void subscribe(ObservableEmitter<ProgressResult<T, Response>> oe) {
                        OkHttpRequest.this.oe = oe;
                        try {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "start request");
                            }
                            Response response = execute();
                            if (response.isSuccessful()) {
                                Log.d(TAG, "response");
                                if (request.isProgressResponse()) {
                                    Log.d(TAG, "download");
                                    download(response);
                                }
                                ProgressResult<T, Response> result = new ProgressResult<>(
                                        ProgressResult.NetStatus.OVER);
                                result.setResponse(response);
                                result.setRequest(request);
                                oe.onNext(result);
                            } else {
                                oe.onError(new NetException(NetException.NETWORK, request.getUrl(),
                                        "http code " + response.code()));
                            }
                        } catch (Exception ex) {
                            //https://github.com/ReactiveX/RxJava/issues/5083
                            // check if the interrupt is due to cancellation
                            // if so, no need to signal the InterruptedException
                            if (!oe.isDisposed()) {
                                if (ex instanceof IOException) {
                                    oe.onError(
                                            new NetException(NetException.NETWORK, request.getUrl(),
                                                    ex.getMessage(), ex));
                                } else if (ex instanceof NetException) {
                                    oe.onError(ex);
                                } else if (ex instanceof RuntimeException) {
                                    String content;
                                    if (BuildConfig.DEBUG) {
                                        //okhttp header中，key、value都不可以有中文/特殊字符
                                        content = "ERROR: " + request.getUrl() + "\nmethod: "
                                                + request.getMethod() + "\nheader: "
                                                + request.getHeaderMap();
                                    } else {
                                        content = "ERROR: " + request.getUrl();
                                    }
                                    oe.onError(
                                            new NetException(NetException.PARSER, request.getUrl(),
                                                    content, ex));
                                } else {
                                    oe.onError(ex);
                                }
                            } else {
                                Log.d(TAG, "rxJava cancel, ignore: " + ex.getMessage());
                            }
                        }
                        oe.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    public Observable<ProgressResult<T, Response>> build(OkHttpClient client,
            RequestBody requestBody) {
        String url = request.getUrl();
        if (TextUtils.isEmpty(url) || !URLUtil.isNetworkUrl(url)) {
            Log.e(TAG, "Url error: " + url, new Throwable());
            return Observable.empty();
        }
        return Observable
                .create(new ObservableOnSubscribe<ProgressResult<T, Response>>() {
                    @Override
                    public void subscribe(ObservableEmitter<ProgressResult<T, Response>> oe) {
                        OkHttpRequest.this.oe = oe;
                        try {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "start request");
                            }
                            Response response = execute(client, requestBody);
                            if (response.isSuccessful()) {
                                Log.d(TAG, "response");
                                if (request.isProgressResponse()) {
                                    Log.d(TAG, "download");
                                    download(response);
                                }
                                ProgressResult<T, Response> result = new ProgressResult<>(
                                        ProgressResult.NetStatus.OVER);
                                result.setResponse(response);
                                result.setRequest(request);
                                oe.onNext(result);
                            } else {
                                oe.onError(new NetException(NetException.NETWORK, request.getUrl(),
                                        "http code " + response.code()));
                            }
                        } catch (Exception ex) {
                            //https://github.com/ReactiveX/RxJava/issues/5083
                            // check if the interrupt is due to cancellation
                            // if so, no need to signal the InterruptedException
                            if (!oe.isDisposed()) {
                                if (ex instanceof IOException) {
                                    oe.onError(
                                            new NetException(NetException.NETWORK, request.getUrl(),
                                                    ex.getMessage(), ex));
                                } else if (ex instanceof NetException) {
                                    oe.onError(ex);
                                } else if (ex instanceof RuntimeException) {
                                    String content;
                                    if (BuildConfig.DEBUG) {
                                        //okhttp header中，key、value都不可以有中文/特殊字符
                                        content = "ERROR: " + request.getUrl() + "\nmethod: "
                                                + request.getMethod() + "\nheader: "
                                                + request.getHeaderMap();
                                    } else {
                                        content = "ERROR: " + request.getUrl();
                                    }
                                    oe.onError(
                                            new NetException(NetException.PARSER, request.getUrl(),
                                                    content, ex));
                                } else {
                                    oe.onError(ex);
                                }
                            } else {
                                Log.d(TAG, "rxJava cancel, ignore: " + ex.getMessage());
                            }
                        }
                        oe.onComplete();
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    @Override
    public void onProgress(long bytes, long contentLength, ProgressResult.NetStatus status) {
        // 进度回调
        if (oe != null && !oe.isDisposed()) {
            ProgressResult<T, Response> result = new ProgressResult<>(status);
            result.setBytes(bytes);
            result.setContentLength(contentLength);
            result.setRequest(request);
            oe.onNext(result);
        }
    }
}
