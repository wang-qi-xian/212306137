package com.stu.helloserver.controller;

import com.stu.helloserver.common.Result;
import com.stu.helloserver.entry.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    // 1. 查询用户（GET）
    @GetMapping("/{id}")
    public Result<String> getUser(@PathVariable("id") Long id) {
        String data = "查询成功，正在返回 ID 为 " + id + " 的用户信息";
        return Result.success(data);
    }

    // 2. 新增用户（POST）- 接收 JSON 数据
    @PostMapping
    public Result<String> createUser(@RequestBody String userInfo) {
        // 模拟创建用户
        String data = "创建用户成功，信息：" + userInfo;
        return Result.success(data);
    }

    // 3. 全量更新用户（PUT）
    @PutMapping("/{id}")
    public Result<String> updateUser(@PathVariable("id") Long id, @RequestBody String userInfo) {
        // 模拟更新
        String data = "更新用户 " + id + " 成功";
        return Result.success(data);
    }


    // 4. 删除用户（DELETE）
    @DeleteMapping("/{id}")
    public Result<String> deleteUser(@PathVariable("id") Long id) {
        // 模拟删除
        String data = "删除用户 " + id + " 成功";
        return Result.success(data);
    }
}