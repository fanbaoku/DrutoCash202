package com.su.request.http;

/**
 * Created by su on 18-3-21.
 */

public class NetException extends RuntimeException {

    public static final int NETWORK = 1;
    public static final int PARSER = 2;
    public static final int BUILD_REQUEST = 4;
    public static final int LOGOUT = 5;
    public static final int DOWNTIME = 6;

    private int errorType = -1;
    private String url;

    public NetException(int errorType, String url, String message, Throwable e) {
        super(message, e);
        this.errorType = errorType;
        this.url = url;
    }

    public NetException(int errorType, String url, String message) {
        super(message);
        this.errorType = errorType;
        this.url = url;
    }

    public int getErrorCode() {
        return errorType;
    }

    @Override
    public String toString() {
        return getErrorHint() + "\nurl: " + url + "\n" + super.toString();
    }

    public String getUrl() {
        return url;
    }

    public String getErrorHint() {
        String hint = "";
        switch (errorType) {
            case NETWORK:
                hint = "Internet Error";
                break;
            case PARSER:
                hint = "Parse Failed";
                break;
            case BUILD_REQUEST:
                hint = "Build Request Error";
                break;
            case LOGOUT:
                hint = "Logout";
                break;
            case DOWNTIME:
                hint = "Downtime";
                break;
            default:
                break;

        }
        return hint;
    }
}
