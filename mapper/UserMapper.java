package com.stu.helloserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stu.helloserver.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 自定义方法：根据用户名查询用户
    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User selectByUsername(String username);
}