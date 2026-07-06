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
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Result<Map<String, Object>> login(User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", loginUser.getUsername());
        User user = this.getOne(queryWrapper);

        if (user == null) {
            return Result.error("用户不存在");
        }

        String md5Password = SecureUtil.md5(loginUser.getPassword());
        if (!user.getPassword().equals(md5Password)) {
            return Result.error("密码错误");
        }

        return Result.success(buildLoginData(user));
    }

    @Override
    public Result<Map<String, Object>> loginWithGithub(String githubId, String login, String name, String avatar, String email) {
        if (!StringUtils.hasText(githubId) || !StringUtils.hasText(login)) {
            return Result.error("GitHub 用户信息不完整");
        }

        String username = "github_" + githubId;
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        User user = this.getOne(queryWrapper);

        if (user == null) {
            user = new User();
            user.setUsername(username);
            user.setPassword(SecureUtil.md5("github:" + githubId));
            user.setName(StringUtils.hasText(name) ? name : login);
            user.setAvatar(avatar);
            user.setEmail(email);
            user.setRole("STUDENT");
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setIsDeleted(0);
            user.setLeaderStatus(0);

            if (!this.save(user)) {
                return Result.error("GitHub 用户创建失败");
            }
        } else {
            user.setName(StringUtils.hasText(name) ? name : login);
            user.setAvatar(avatar);
            if (StringUtils.hasText(email)) {
                user.setEmail(email);
            }
            user.setUpdateTime(LocalDateTime.now());
            this.updateById(user);
        }

        return Result.success(buildLoginData(user));
    }

    @Override
    public Result<User> getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user != null) {
            user.setPassword(null);
            return Result.success(user);
        }
        return Result.error("用户不存在");
    }

    private Map<String, Object> buildLoginData(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("role", user.getRole());
        data.put("username", user.getUsername());
        data.put("name", user.getName());
        data.put("avatar", user.getAvatar());
        data.put("id", user.getId());

        return data;
    }
}
