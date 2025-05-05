package com.ray.archive.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一API响应结构
 * @param <T> 响应数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "API响应结构")
public class ApiResponse<T> {

    @ApiModelProperty(value = "响应码", example = "200", required = true)
    private Integer code;

    @ApiModelProperty(value = "响应消息", example = "请求成功")
    private String message;

    @ApiModelProperty(value = "响应数据")
    private T data;

    /**
     * 创建成功响应，默认状态码200
     * @param data 响应数据
     * @return API响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "请求成功", data);
    }

    /**
     * 创建成功响应，默认状态码200，自定义消息
     * @param message 响应消息
     * @param data 响应数据
     * @return API响应对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * 创建错误响应
     * @param code 错误码
     * @param message 错误消息
     * @return API响应对象
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 创建错误响应，包含数据
     * @param code 错误码
     * @param message 错误消息
     * @param data 错误数据
     * @return API响应对象
     */
    public static <T> ApiResponse<T> error(Integer code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }
} 