package com.example.mdtoword.pojo;

import lombok.Data;

/**
 * 统一响应结果类
 * 用于封装API响应数据
 */
@Data
public class Result<T> {
    // 状态码常量
    public static final Integer SUCCESS_CODE = 200;
    public static final Integer BAD_REQUEST_CODE = 400;
    public static final Integer UNAUTHORIZED_CODE = 401;
    public static final Integer FORBIDDEN_CODE = 403;
    public static final Integer NOT_FOUND_CODE = 404;
    public static final Integer INTERNAL_SERVER_ERROR_CODE = 500;
    
    // 默认消息常量
    public static final String SUCCESS_MESSAGE = "操作成功";
    public static final String ERROR_MESSAGE = "操作失败";
    public static final String UNAUTHORIZED_MESSAGE = "未授权";
    public static final String FORBIDDEN_MESSAGE = "禁止访问";
    public static final String NOT_FOUND_MESSAGE = "资源不存在";
    public static final String INTERNAL_SERVER_ERROR_MESSAGE = "服务器内部错误";
    
    private Integer code; // 状态码：200成功，400请求错误，401未授权，500服务器错误
    private String message; // 提示信息
    private T data; // 响应数据
    private Boolean success; // 是否成功

    /**
     * 私有构造方法
     */
    private Result() {
    }

    /**
     * 成功返回结果（无数据）
     */
    public static <T> Result<T> success() {
        return success(null, SUCCESS_MESSAGE);
    }

    /**
     * 成功返回结果
     * @param data 返回的数据
     */
    public static <T> Result<T> success(T data) {
        return success(data, SUCCESS_MESSAGE);
    }

    /**
     * 成功返回结果
     * @param data 返回的数据
     * @param message 提示信息
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(SUCCESS_CODE);
        result.setMessage(message);
        result.setData(data);
        result.setSuccess(true);
        return result;
    }

    /**
     * 失败返回结果
     * @param message 提示信息
     */
    public static <T> Result<T> error(String message) {
        return error(BAD_REQUEST_CODE, message);
    }

    /**
     * 失败返回结果
     * @param code 状态码
     * @param message 提示信息
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setSuccess(false);
        return result;
    }

    /**
     * 未授权返回结果
     */
    public static <T> Result<T> unauthorized() {
        return unauthorized(UNAUTHORIZED_MESSAGE);
    }

    /**
     * 未授权返回结果
     * @param message 提示信息
     */
    public static <T> Result<T> unauthorized(String message) {
        return error(UNAUTHORIZED_CODE, message);
    }

    /**
     * 禁止访问返回结果
     */
    public static <T> Result<T> forbidden() {
        return forbidden(FORBIDDEN_MESSAGE);
    }

    /**
     * 禁止访问返回结果
     * @param message 提示信息
     */
    public static <T> Result<T> forbidden(String message) {
        return error(FORBIDDEN_CODE, message);
    }

    /**
     * 资源不存在返回结果
     */
    public static <T> Result<T> notFound() {
        return notFound(NOT_FOUND_MESSAGE);
    }

    /**
     * 资源不存在返回结果
     * @param message 提示信息
     */
    public static <T> Result<T> notFound(String message) {
        return error(NOT_FOUND_CODE, message);
    }

    /**
     * 服务器内部错误返回结果
     */
    public static <T> Result<T> internalServerError() {
        return internalServerError(INTERNAL_SERVER_ERROR_MESSAGE);
    }

    /**
     * 服务器内部错误返回结果
     * @param message 提示信息
     */
    public static <T> Result<T> internalServerError(String message) {
        return error(INTERNAL_SERVER_ERROR_CODE, message);
    }

    /**
     * 参数验证失败返回结果
     * @param message 提示信息
     */
    public static <T> Result<T> badRequest(String message) {
        return error(BAD_REQUEST_CODE, message);
    }
}