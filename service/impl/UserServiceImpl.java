package com.stu.helloserver.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stu.helloserver.common.Result;
import com.stu.helloserver.common.ResultCode;
import com.stu.helloserver.mapper.UserInfoMapper;
import com.stu.helloserver.mapper.UserMapper;
import com.stu.helloserver.model.dto.UserDTO;
import com.stu.helloserver.model.entity.User;
import com.stu.helloserver.model.entity.UserInfo;
import com.stu.helloserver.security.JwtUtil;
import com.stu.helloserver.service.UserService;
import com.stu.helloserver.model.vo.UserDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String CACHE_KEY_PREFIX = "user:detail:";

    @Override
    public Result<String> register(UserDTO userDTO) {
        // 1. 查询该用户名是否已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        User dbUser = userMapper.selectOne(queryWrapper);
        if (dbUser != null) {
            return Result.error(ResultCode.USER_HAS_EXISTED);
        }

        // 2. 组装实体对象（实际项目中密码需加密）
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(userDTO.getPassword());   // 明文存储仅用于演示，生产环境需加密

        // 3. 插入数据库
        userMapper.insert(user);

        return Result.success("注册成功！");
    }

    @Autowired
    private JwtUtil jwtUtil;   // 注入 JwtUtil

    @Override
    public Result<String> login(UserDTO userDTO) {
        // 1. 根据用户名查询数据库
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, userDTO.getUsername());
        User dbUser = userMapper.selectOne(queryWrapper);

        // 2. 校验用户是否存在
        if (dbUser == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        // 3. 校验密码
        if (!dbUser.getPassword().equals(userDTO.getPassword())) {
            return Result.error(ResultCode.PASSWORD_ERROR);
        }

        // 4. 生成 JWT
        String jwt = jwtUtil.generateToken(userDTO.getUsername());
        return Result.success(jwt);
    }

    @Override
    public Result<String> getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }
        return Result.success("查询成功，用户名为：" + user.getUsername());
    }

    @Override
    public Result<Object> getUserPage(Integer pageNum, Integer pageSize) {
        // 1. 创建分页对象（当前页码，每页条数）
        Page<User> pageParam = new Page<>(pageNum, pageSize);
        // 2. 执行分页查询（第二个参数为查询条件，传null表示查询全部）
        Page<User> resultPage = userMapper.selectPage(pageParam, null);
        // 3. 返回结果，Page 对象中已包含 records、total、pages 等元数据
        return Result.success(resultPage);
    }

    // ========== 任务7 新增方法 ==========

    @Override
    public Result<UserDetailVO> getUserDetail(Long userId) {
        String key = CACHE_KEY_PREFIX + userId;

        // 1. 先查缓存
        String json = redisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            try {
                UserDetailVO cachedVO = JSONUtil.toBean(json, UserDetailVO.class);
                return Result.success(cachedVO);
            } catch (Exception e) {
                // 缓存数据异常，删除脏缓存，继续查数据库
                redisTemplate.delete(key);
            }
        }

        // 2. 查数据库（多表联查）
        UserDetailVO detail = userInfoMapper.getUserDetail(userId);
        if (detail == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        // 3. 写入缓存，设置过期时间10分钟
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(detail), 10, TimeUnit.MINUTES);

        return Result.success(detail);
    }

    @Override
    @Transactional
    public Result<String> updateUserInfo(Long userId, String realName, String phone, String address) {
        // 检查用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        // 查询 user_info 记录是否存在
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUserId, userId);
        UserInfo userInfo = userInfoMapper.selectOne(wrapper);

        if (userInfo == null) {
            // 不存在则新增
            userInfo = new UserInfo();
            userInfo.setUserId(userId);
            userInfo.setRealName(realName);
            userInfo.setPhone(phone);
            userInfo.setAddress(address);
            userInfoMapper.insert(userInfo);
        } else {
            // 存在则更新
            userInfo.setRealName(realName);
            userInfo.setPhone(phone);
            userInfo.setAddress(address);
            userInfoMapper.updateById(userInfo);
        }

        // 操作数据库成功后，删除缓存
        String key = CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(key);

        return Result.success("更新成功");
    }

    @Override
    @Transactional
    public Result<String> deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(ResultCode.USER_NOT_EXIST);
        }

        // 删除扩展信息（如果存在）
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUserId, userId);
        userInfoMapper.delete(wrapper);

        // 删除主表用户
        userMapper.deleteById(userId);

        // 删除缓存
        String key = CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(key);

        return Result.success("删除成功");
    }
}