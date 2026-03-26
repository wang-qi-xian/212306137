package com.stu.helloserver.common;

public enum ResultCode {
    // 基础状态码
    SUCCESS(200, "操作成功"),
    ERROR(500, "系统繁忙，请稍后再试"),

    // 权限相关
    TOKEN_INVALID(401, "登录凭证已缺失或过期，请重新登录"),

    // 用户注册相关
    USER_HAS_EXISTED(4001, "该用户名已被注册"),

    // 用户登录相关
    USER_NOT_EXIST(4002, "该用户不存在"),
    PASSWORD_ERROR(4003, "账号或密码错误");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}