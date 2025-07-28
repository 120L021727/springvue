package com.example.mdtoword.pojo;

import lombok.Data;

/**
 * 统一响应结果类
 * 用于封装API响应数据
 */
@Data
public class Result<T> {
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
     * 成功返回结果
     * @param data 返回的数据
     */
    public static <T> Result<T> success(T data) {
        return success(data, "操作成功");
    }

    /**
     * 成功返回结果
     * @param data 返回的数据
     * @param message 提示信息
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
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
        return error(400, message);
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
    public static <T> Result<T> unauthorized(String message) {
        return error(401, message);
    }
}