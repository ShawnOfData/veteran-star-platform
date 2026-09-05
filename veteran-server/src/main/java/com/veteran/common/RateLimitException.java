package com.veteran.common;

/**
 * 频率限制异常 — 触发于接口调用过频
 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }

    public RateLimitException() {
        super("操作过于频繁，请稍后再试");
    }
}
