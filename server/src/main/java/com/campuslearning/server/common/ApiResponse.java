package com.campuslearning.server.common;

/**
 * 服务端统一响应结构。
 *
 * @param <T> data 字段类型
 */
public class ApiResponse<T> {

    private Integer code;
    private T data;
    private String message;

    public ApiResponse() {
    }

    public ApiResponse(Integer code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, data, "success");
    }

    public static ApiResponse<?> fail(Integer code, String message) {
        return new ApiResponse<>(code, null, message);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
