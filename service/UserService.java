package com.stu.helloserver.service;

import com.stu.helloserver.common.Result;
import com.stu.helloserver.model.dto.UserDTO;
import com.stu.helloserver.model.vo.UserDetailVO;

public interface UserService {
    Result<String> register(UserDTO userDTO);
    Result<String> login(UserDTO userDTO);
    Result<String> getUserById(Long id);
    Result<Object> getUserPage(Integer pageNum, Integer pageSize);
    // 新增：获取用户详情（多表联查 + Redis缓存）
    Result<UserDetailVO> getUserDetail(Long userId);

    // 新增：更新用户扩展信息
    Result<String> updateUserInfo(Long userId, String realName, String phone, String address);

    // 新增：删除用户（同时删除缓存）
    Result<String> deleteUser(Long userId);
}