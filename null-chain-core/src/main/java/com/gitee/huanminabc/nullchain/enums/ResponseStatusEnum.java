package com.gitee.huanminabc.nullchain.enums;

import lombok.Getter;

/**
 * @program: java-huanmin-utils
 * @description:
 * @author: huanmin
 * @create: 2025-03-20 11:03
 **/
@Getter
public enum ResponseStatusEnum {
    SUCCESS(200, "操作成功"),
    FAILED(500, "操作失败"),
    NOT_FOUND(404, "未找到"),
    BAD_REQUEST(400, "错误请求"),
    FORBIDDEN(403, "禁止访问"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    INVALID_INPUT(1001, "无效输入"),
    RESOURCE_NOT_FOUND(1007, "资源未找到"),
    OPERATION_FAILED(1008, "操作失败"),
    TIMEOUT(1009, "超时"),
    DATA_VALIDATION_FAILED(1013, "数据验证失败");

    private final int code;
    private final String message;

    ResponseStatusEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    //通过code获取message
    public static String getMessage(int code) {
        for (ResponseStatusEnum status : ResponseStatusEnum.values()) {
            if (status.getCode() == code) {
                return status.getMessage();
            }
        }
        return null;
    }
    //通过code获取枚举
    public static ResponseStatusEnum getEnum(int code) {
        for (ResponseStatusEnum status : ResponseStatusEnum.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }

}
