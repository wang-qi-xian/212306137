package com.stu.helloserver.service.impl;

import com.stu.helloserver.common.Result;
import com.stu.helloserver.common.ResultCode;
import com.stu.helloserver.model.dto.UserDTO;
import com.stu.helloserver.service.UserService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    // 模拟数据库存储
    private static final Map<String, String> userDb = new HashMap<>();

    @Override
    public Result<String> register(UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();

        // 校验用户名是否已存在
        if (userDb.containsKey(username)) {
            return Result.error(ResultCode.USER_HAS_EXISTED);
        }

        // 存储用户（实际应加密密码，此处仅为演示）
        userDb.put(username, password);

        // 模拟生成 Token（实际应使用 JWT 等）
        String token = UUID.randomUUID().toString();
        return Result.success(token);
    }

    @Override
    public Result<String> login(UserDTO userDTO) {
        String username = userDTO.getUsername();
        String password = userDTO.getPassword();

        // 校验用户是否存在
        if (!userDb.containsKey(username)) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        // 校验密码
        String storedPwd = userDb.get(username);
        if (!storedPwd.equals(password)) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        // 登录成功，返回 Token
        String token = UUID.randomUUID().toString();
        return Result.success(token);
    }
}