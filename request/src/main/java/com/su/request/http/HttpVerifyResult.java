package com.su.request.http;

/**
 * Http响应基类
 *
 * @param <T>
 */
public class HttpVerifyResult<T> extends HttpResult<T> {
    private long expire;

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "data=" + getExploited() +
                ", resCode=" + getTotal() +
                ", error='" + getSwell() + '\'' +
                ", expire='" + expire + '\'' +
                '}';
    }
}
