package com.su.request.http;

/**
 * Http响应基类
 *
 * @param <T>
 */
public class HttpResult<T> {
    public static final int CODE_SUCCESS = 0;
    private int total;
    private String swell;
    private T exploited;

    public T getExploited() {
        return exploited;
    }

    public void setExploited(T exploited) {
        this.exploited = exploited;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getSwell() {
        return swell;
    }

    public void setSwell(String swell) {
        this.swell = swell;
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "exploited=" + exploited +
                ", total=" + total +
                ", swell='" + swell + '\'' +
                '}';
    }
}
