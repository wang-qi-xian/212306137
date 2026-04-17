package com.stu.helloserver.controller;

import com.stu.helloserver.common.Result;
import com.stu.helloserver.model.dto.UserDTO;
import com.stu.helloserver.model.vo.UserDetailVO;
import com.stu.helloserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 注册接口
    @PostMapping
    public Result<String> register(@RequestBody UserDTO userDTO) {
        return userService.register(userDTO);
    }

    // 登录接口
    @PostMapping("/login")
    public Result<String> login(@RequestBody UserDTO userDTO) {
        return userService.login(userDTO);
    }

    // 根据 id 查询用户信息
    @GetMapping("/{id}")
    public Result<String> getUser(@PathVariable("id") Long id) {
        return userService.getUserById(id);
    }

    // 分页查询用户列表
// GET /api/users/page?pageNum=1&pageSize=5
    @GetMapping("/page")
    public Result<Object> getUserPage(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize) {
        return userService.getUserPage(pageNum, pageSize);
    }

    // 查询用户详情（多表联查 + Redis缓存）
    @GetMapping("/{id}/detail")
    public Result<UserDetailVO> getUserDetail(@PathVariable("id") Long userId) {
        return userService.getUserDetail(userId);
    }

    // 更新用户扩展信息
    @PutMapping("/{id}/info")
    public Result<String> updateUserInfo(@PathVariable("id") Long userId,
                                         @RequestParam(required = false) String realName,
                                         @RequestParam(required = false) String phone,
                                         @RequestParam(required = false) String address) {
        return userService.updateUserInfo(userId, realName, phone, address);
    }

    // 删除用户
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable("id") Long userId) {
        return userService.deleteUser(userId);
    }
}