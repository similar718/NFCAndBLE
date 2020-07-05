package com.clc.baselibs.rxjava2;

public class QzdsException extends Exception {

    private String message;
    private String code;

    public QzdsException(String message, String code) {
        super(message);
        this.code = code;
        this.message=message;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;

    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;

    }
}
