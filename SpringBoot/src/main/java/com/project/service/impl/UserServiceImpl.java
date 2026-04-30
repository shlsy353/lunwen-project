package com.project.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.entity.User;
import com.project.mapper.UserMapper;
import com.project.service.IUserService;
import com.project.util.JwtUtil;
import com.project.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result<Map<String, Object>> login(User loginUser) {
        // Query user by username
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginUser.getUsername());
        User user = this.getOne(queryWrapper);

        if (user == null) {
            return Result.error("用户不存在");
        }

        // Verify password (MD5)
        String md5Password = SecureUtil.md5(loginUser.getPassword());
        if (!user.getPassword().equals(md5Password)) {
            return Result.error("密码错误");
        }

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("role", user.getRole());
        data.put("username", user.getUsername());
        data.put("name", user.getName());
        data.put("avatar", user.getAvatar());
        data.put("id", user.getId());

        return Result.success(data);
    }

    @Override
    public Result<User> getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user != null) {
            user.setPassword(null); // Don't return password
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }
}
