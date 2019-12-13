package com.meari.echoshow.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * http响应消息体
 *
 * @author zyp
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebResult<T> {
    private static final int SUCCESS = 1001;
    private static final int FAILURE = 1002;
    private static final int PARAMETER_IS_EMPTY = 1003;
    private static final int ILLEGAL_REQUEST = 1004;
    private static final int DATA_NOT_EXIST = 1007;

    private String messageId;
    private int code;
    private String msg;
    private T data;

    private WebResult(int code, T data) {
        this.code = code;
        this.data = data;
    }

    private WebResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private WebResult(int code) {
        this.code = code;
    }

    public static WebResult success(Message message) {
        WebResult<Object> result = new WebResult<>(SUCCESS, message.getData());
        result.messageId = message.getMessageId();
        return result;
    }

    public static <T> WebResult<T> dataNotExist(String messageId) {
        WebResult<T> result = new WebResult<>(DATA_NOT_EXIST);
        result.messageId = messageId;
        return result;
    }

    public String getMessageId() {
        return messageId;
    }

    public int getCode() {
        return code;
    }

    public int getResultCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
