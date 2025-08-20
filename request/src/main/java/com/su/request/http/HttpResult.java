package com.su.request.http;

/**
 * Http响应基类
 *
 * @param <T>
 */
public class HttpResult<T> {
    public static final int CODE_SUCCESS = 0;
    private int hum;
    private String inverse;
    private T silently;

    public T getSilently() {
        return silently;
    }

    public void setSilently(T silently) {
        this.silently = silently;
    }

    public int getHum() {
        return hum;
    }

    public void setHum(int hum) {
        this.hum = hum;
    }

    public String getInverse() {
        return inverse;
    }

    public void setInverse(String inverse) {
        this.inverse = inverse;
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "exploited=" + silently +
                ", total=" + hum +
                ", swell='" + inverse + '\'' +
                '}';
    }
}
