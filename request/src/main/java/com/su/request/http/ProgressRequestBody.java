package com.su.request.http;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Created by su on 18-3-22.
 */

public class ProgressRequestBody extends RequestBody {

    private RequestBody requestBody;

    private BufferedSink bufferedSink;

    private ProgressListener progressListener;

    ProgressRequestBody(RequestBody requestBody, ProgressListener progressListener) {
        this.requestBody = requestBody;
        this.progressListener = progressListener;
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink));
        }
        //写入
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            //当前写入字节数
            long bytesWritten;
            //总字节长度，避免多次调用contentLength()方法
            long contentLength;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0L) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength();
                }
                //
                //增加当前写入的字节数
                bytesWritten += byteCount;
                //回调
                if (progressListener != null) {
                    progressListener.onProgress(bytesWritten, contentLength, ProgressResult.NetStatus.REQUEST);
                }
            }
        };
    }
}
