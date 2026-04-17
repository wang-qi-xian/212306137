package com.stu.helloserver.model.vo;

import lombok.Data;
import java.io.Serializable;

@Data
public class UserDetailVO implements Serializable {
    private Long userId;      // 对应 sys_user.id
    private String username;  // 用户名
    private String realName;  // 真实姓名
    private String phone;     // 手机号
    private String address;   // 地址
}