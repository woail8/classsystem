package com.campuslearning.server.common;

import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 服务端全局异常处理器，统一包装成约定响应格式。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException ex) {
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getAllErrors().isEmpty()
                ? "参数校验失败"
                : ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<?> handleBindException(BindException ex) {
        String message = ex.getAllErrors().isEmpty()
                ? "参数绑定失败"
                : ex.getAllErrors().get(0).getDefaultMessage();
        return ApiResponse.fail(400, message);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception ex) {
        return ApiResponse.fail(500, ex.getMessage() == null ? "服务端内部错误" : ex.getMessage());
    }
}
