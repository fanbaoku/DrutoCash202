package com.su.request.http;

import com.su.request.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.internal.Util;
import okhttp3.internal.http.HttpMethod;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.logging.LoggingEventListener;

/**
 * Created by su on 18-3-21.
 */

public class OkHttpClientHelper extends OkHttpClient {

    private static final HttpLoggingInterceptor.Level LOG_LEVEL = HttpLoggingInterceptor.Level.BODY;
    public static final String TAG = OkHttpClientHelper.class.getSimpleName();
    private static final OkHttpClient sClient;
    private static final OkHttpClient sDownloadClient;

    private OkHttpClientHelper() {}

    static {
        OkHttpClient client = new OkHttpClient();
        Builder builder = client.newBuilder()
                .protocols(Util.immutableListOf(Protocol.HTTP_1_1, Protocol.HTTP_2)) //just for http1.1
                .connectTimeout(30L, TimeUnit.SECONDS)
                .readTimeout(30L, TimeUnit.SECONDS)
                .writeTimeout(30L, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG) {
            builder.eventListenerFactory(new LoggingEventListener.Factory());
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(LOG_LEVEL);
            builder.addNetworkInterceptor(logging);
        }
        builder.addNetworkInterceptor(chain -> {
            Request request = chain.request();
            ProgressListenerConfig config = request.tag(ProgressListenerConfig.class);
            if (config != null && config.isProgressRequest() && HttpMethod.requiresRequestBody(request.method())) {
                // 重新构造request
                request = request
                        .newBuilder()
                        .method(request.method(), new ProgressRequestBody(request.body(), config.getListener()))
                        .build();
            }
            return chain.proceed(request);
        });
        sClient = builder.build();

        OkHttpClient downloadClient = new OkHttpClient();
        Builder downloadBuilder = downloadClient.newBuilder()
                .protocols(Util.immutableListOf(Protocol.HTTP_1_1, Protocol.HTTP_2)) //just for http1.1
                .connectTimeout(30L, TimeUnit.SECONDS)
                .readTimeout(30L, TimeUnit.SECONDS)
                .writeTimeout(30L, TimeUnit.SECONDS);
        downloadBuilder.addNetworkInterceptor(chain -> {
            Request request = chain.request();
            ProgressListenerConfig config = request.tag(ProgressListenerConfig.class);
            if (config != null && config.isProgressRequest() && HttpMethod.requiresRequestBody(request.method())) {
                // 重新构造request
                request = request
                        .newBuilder()
                        .method(request.method(), new ProgressRequestBody(request.body(), config.getListener()))
                        .build();
            }
            return chain.proceed(request);
        });
        sDownloadClient = downloadBuilder.build();
    }

    public static OkHttpClient getOkHttpInstance() {
        return sClient;
    }

    public static OkHttpClient getOkHttpDownloadInstance() {
        return sDownloadClient;
    }
}
