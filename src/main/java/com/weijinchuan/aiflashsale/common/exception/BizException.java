package com.weijinchuan.aiflashsale.common.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BizException extends RuntimeException {

    /**
     * 业务异常码
     */
    private final Integer code;

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}